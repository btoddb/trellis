
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

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.btoddb.trellis.actor.TrellisPersistenceProvider;
import com.btoddb.trellis.common.PersistentKey;
import com.btoddb.trellis.common.Stat;
import com.btoddb.trellis.common.StopWatchInNanos;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.TrellisRemoteException;
import com.btoddb.trellis.common.TrellisRequest;
import com.btoddb.trellis.common.TrellisResponse;
import com.btoddb.trellis.common.TrellisSession;
import com.btoddb.trellis.common.Utils;
import com.btoddb.trellis.server.ActorDescriptor;
import com.btoddb.trellis.server.ActorManagementService;
import com.btoddb.trellis.server.persistence.StorageManager;

//@ManagedResource(objectName="trellis-server:type=stages,name=dispatch-request", description="Dispatch request to actor")
@ManagedResource
public class DispatchRequestStage extends StageController
{
	private static final Logger logger = LoggerFactory.getLogger(DispatchRequestStage.class);

	private BlockingQueue<TrellisSession> outQueue;
	private ActorManagementService actorMgr;
	private StorageManager storageMgr;

	public DispatchRequestStage()
	{}

	@Override
	protected void process(TrellisSession gridSession)
	{
		TrellisRequest req = gridSession.getRequest();

		String actorName = req.getActorName();

		ActorDescriptor ad = actorMgr.getActorDescriptorByName(actorName);
		if (null == ad)
		{
			throw new TrellisException("No actor named, " + actorName + ", has been registered with grid");
		}

		logger.debug("dispatching request to actor, {} : session ID = {}", actorName, req.getSessionId());

		TrellisPersistenceProvider persistenceProvider = ad.getPersistenceProvider();
		Map<PersistentKey, Object> data = null;
		Object retData;

		if (null != persistenceProvider)
		{
			try
			{
				PersistentKey[] persistentKeyArr = createPersistenKeyArray(req.getKeyList(), null);
				if (null != persistenceProvider)
				{
					data = storageMgr.get(persistenceProvider, persistentKeyArr);
				}
			}
			catch (Throwable e)
			{
				logger.error("exception while reading data, propagating to client", e);
				retData = new TrellisRemoteException(e);
			}
		}

		try
		{
			StopWatchInNanos sw = new StopWatchInNanos().start();
			Stat stat = new Stat("actor-" + actorName, 0);
			sw.stop();
			retData = ad.getActor().execute(req.getData(), data, gridSession.getStats());
			stat.addSample(1, sw.getDuratinInMicros());
			
			gridSession.getStats().addStat(stat);
			getMsgStatsMBean().addRollingSample("actor-" + actorName, sw.getDuratinInMicros());
		}
		catch (Throwable e)
		{
			logger.error("exception while executing actor, propagating to client", e);
			retData = new TrellisRemoteException(e);
		}

		// the GridSession object does not have the correct session ID. we
		// need the session ID sent from the client, not the one generated
		// by the sever
		TrellisResponse res = new TrellisResponse(req.getKey(), req.getSessionId(), retData);
		gridSession.setResponse(res);

		outQueue.offer(gridSession);
	}

	private PersistentKey[] createPersistenKeyArray(List<ByteBuffer> keyList, List<ByteBuffer> colList)
	{
		if (null != keyList)
		{
			PersistentKey[] keyArr = new PersistentKey[keyList.size()];
			int index = 0;
			for (ByteBuffer bb : keyList)
			{
				keyArr[index++] = new PersistentKey(Utils.safeByteBufferArrayAccess(bb), colList);
			}
			return keyArr;
		}
		else
		{
			return null;
		}
	}

	public void setOutQueue(BlockingQueue<TrellisSession> outQueue)
	{
		this.outQueue = outQueue;
	}

	public void setActorMgr(ActorManagementService actorMgr)
	{
		this.actorMgr = actorMgr;
	}

	public void setStorageMgr(StorageManager storageMgr)
	{
		this.storageMgr = storageMgr;
	}
}
