
/*
 * Copyright 2013 B. Todd Burruss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btoddb.trellis.server.stage;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.btoddb.trellis.cassandra.NodeManagement;
import com.btoddb.trellis.client.TrellisCluster;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.TrellisRequest;
import com.btoddb.trellis.common.TrellisSession;
import com.btoddb.trellis.server.TrellisSessionIdGenerator;

@ManagedResource(objectName="trellis-server:type=stages,name=receive-request", description="Receive request from wire")
public class ReceiveRequestStage extends StageController
{
	private static final Logger logger = LoggerFactory.getLogger(ReceiveRequestStage.class);

	private static final int MAX_FORWARDS = 3;

	private TrellisSessionIdGenerator sessIdGen;
	private TrellisCluster gridCluster;
	private NodeManagement nodeMgmt;
	private Queue<TrellisSession> outQueue;

	/**
	 * After marshalling, this is the entry point for receiving a request. Every
	 * request is assigned a session (even multi-key requests) and routed to the
	 * appropriate node(s).
	 * 
	 */
	@Override
	public void process(TrellisSession gridSession)
	{
		TrellisRequest req = gridSession.getRequest();

		logger.debug("received request on server : session ID = {}, actor-name = {}",
				req.getSessionId(), req.getActorName());

		// if request has a key collection and a keyspace name, then route
		// request to proper node(s)
		if (null != req.getKeyList() && null != req.getKeyspaceName())
		{
			routeRequest(gridSession);
		}
		// no keys, so handle locally
		else if (null == req.getKeyList() && null == req.getKeyspaceName())
		{
			outQueue.add(gridSession);
		}
		else
		{
			throw new TrellisException("Key collection and keyspace name must both be empty or set");
		}
	}

	//
	// Using the request's key list, forward requests to other nodes or add to
	// local processing queue.
	//
	private void routeRequest(TrellisSession sessParent)
	{
		TrellisRequest origReq;
		Map<String, List<ByteBuffer>> hostToKeysMap;

		//
		// generate a map of host -> keys so we only send one request to the
		// other hosts (or local) containing only the keys that are local
		// to that host
		//

		try
		{
			origReq = sessParent.getRequest();
			hostToKeysMap = nodeMgmt.generateHostToKeysMap(origReq.getKeyspaceName(),
					origReq.getKeyList());
		}
		catch (Throwable e)
		{
			handleException(sessParent, e, true);
			throw new TrellisException(
					"exception while generating host-to-keys map for routing request", e);
		}

		//
		// create a session group to track all the sessions about to be created.
		// one session per routed request. a routed request could be local or
		// remote,
		// and could have 1 or more keys.
		//

		sessParent.setNumExpectedChildren(hostToKeysMap.size());

		try
		{
			for (Entry<String, List<ByteBuffer>> entry : hostToKeysMap.entrySet())
			{
				if (nodeMgmt.isNodeLocal(InetAddress.getByName(entry.getKey())))
				{
					routeLocally(sessParent, origReq, entry.getValue());
				}
				else
				{
					routeRemotely(sessParent, origReq, entry.getKey(), entry.getValue());
				}
			}
		}
		catch (TrellisException e)
		{
			handleException(sessParent, e, true);
			throw e;
		}
		catch (Throwable e)
		{
			handleException(sessParent, e, true);
			throw new TrellisException("exception while routing requests to proper nodes", e);
		}
	}

	//
	// create a new request for each key and put in dispatch queue. the session
	// group will
	// tie all back together in finalize stage, along with remote requests. this
	// should give
	// us the maximum parallelism.
	//
	private void routeLocally(TrellisSession parentSession, TrellisRequest origReq,
			List<ByteBuffer> keyList)
	{
		// adjust the total request count. one request has already been added
		// for _all_ local requests, so we only increase if more than one local
		// request
		parentSession.adjustNumberOfRequests(keyList.size() - 1);

		for (ByteBuffer bb : keyList)
		{
			TrellisSession childSession = new TrellisSession(null, sessIdGen.getNextId());
			TrellisRequest newReq = new TrellisRequest(childSession.getSessionId(),
					origReq.getActorName(), origReq.getData(), origReq.isInstrument());
			newReq.setKeyspaceName(origReq.getKeyspaceName());
			newReq.setKeyList(Collections.singletonList(bb));
			childSession.setRequest(newReq);
			parentSession.addSessionChild(childSession);

			// add to local actor dispatch queue
			outQueue.add(childSession);
		}
	}

	private TrellisSession routeRemotely(TrellisSession parentSession, TrellisRequest origReq,
			String hostName, List<ByteBuffer> keyList)
	{
		if (MAX_FORWARDS <= origReq.getForwardCount())
		{
			throw new TrellisException("request has been forwarded the maximum number of times, "
					+ MAX_FORWARDS + ", cannot fulfill request");
		}

		// clone request
		TrellisRequest remoteReq = new TrellisRequest(0, origReq.getActorName(), origReq.getData(),
				origReq.isInstrument());
		remoteReq.setKeyspaceName(origReq.getKeyspaceName());
		remoteReq.setKeyList(keyList);

		remoteReq.incrementForwardCount();
		TrellisSession childSession = gridCluster.sendRequestToGridAsync(remoteReq, hostName,
				parentSession);
		return childSession;
	}

	public void setNodeMgmt(NodeManagement nodeMgmt)
	{
		this.nodeMgmt = nodeMgmt;
	}

	public void setGridCluster(TrellisCluster gridCluster)
	{
		this.gridCluster = gridCluster;
	}

	private void handleException(TrellisSession gridSession, Throwable e, boolean sendResponse)
	{
		logger.error("exception while processing request", e);
	}

	public void setOutQueue(Queue<TrellisSession> outQueue)
	{
		this.outQueue = outQueue;
	}

	public void setSessIdGen(TrellisSessionIdGenerator sessIdGen)
	{
		this.sessIdGen = sessIdGen;
	}
}
