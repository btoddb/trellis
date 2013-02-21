
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

package com.btoddb.trellis.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.GetNodesResult;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.TrellisRequest;
import com.btoddb.trellis.common.TrellisSession;
import com.btoddb.trellis.common.TransportSession;
import com.btoddb.trellis.common.TrellisConfig;
import com.google.common.base.Splitter;

@Service("gridCluster")
public class TrellisCluster
{
	private static final Logger logger = LoggerFactory.getLogger(TrellisCluster.class);

	private static final AtomicLong gridSessIdGen = new AtomicLong();

	@Autowired
	private ConnectionMgr connMgr;

	@Autowired
	private TrellisSessionMgr gridSessMgr;

	@Resource(name = "roundRobinSelectionStrategy")
	private HostSelectionStrategy hostSelectionStrategy;

	@Value("$gridProps{hosts}")
	private String hostsStr;

	@Value("$gridProps{port}")
	private int port;

	@Autowired
	private NodeDiscoveryService nodeDiscoverySrvc;

	private NodeDiscovery nodeDiscovery;
	private List<String> originalHostList;

	@Value("$gridProps{start-node-discovery}")
	private boolean startNodeDiscovery = true;

	@Autowired
	private TrellisConfig trellisConfig;

	public TrellisCluster()
	{}

	public TrellisCluster(TrellisConfig trellisConfig)
	{
		this.trellisConfig = trellisConfig;
	}

	public TrellisSession sendRequestToGridAsync(TrellisRequest req)
	{
		String hostName = hostSelectionStrategy.selectNext();
		return sendRequestToGridAsync(req, hostName);
	}

	private TrellisSession sendRequestToGridAsync(TrellisRequest req, String hostName)
	{
		TrellisSession sessParent = new TrellisSession(null);
		return sendRequestToGridAsync(req, hostName, sessParent);
	}

	public TrellisSession sendRequestToGridAsync(TrellisRequest req, String hostName,
			TrellisSession sessParent)
	{
		TrellisSession gridSession = createGridSession(hostName, sessParent);
		req.setSessionId(gridSession.getSessionId());
		gridSession.setRequest(req);
		return sendRequestToGridAsync(gridSession);
	}

	public TrellisSession sendRequestToGridAsync(TrellisSession gridSession)
	{
		logger.debug("sending request to server async : session ID = " + gridSession.getSessionId());
		gridSession.setRemoteCall(true);
		gridSession.setRequestQueuedTimeInNanos(System.nanoTime());
		gridSession.getTransportSession().write(gridSession.getRequest());
		return gridSession;
	}

	private TrellisSession createGridSession(String hostName, TrellisSession sessParent)
	{
		TransportSession transSess = connMgr.getConnection(hostName, port);

		TrellisSession gridSession = new TrellisSession(transSess, getNextGridSessionId());
		if (null != sessParent)
		{
			sessParent.addSessionChild(gridSession);
		}

		transSess.setGridSession(gridSession);

		logger.debug("saving new session ID = " + gridSession.getSessionId());
		gridSessMgr.save(gridSession);
		return gridSession;
	}

	public long getNextGridSessionId()
	{
		return gridSessIdGen.incrementAndGet();
	}

	/**
	 * Perform necessary actions to startup a new trellis cluster instance. The
	 * cluster should be shared amongst all threads and this method should only be
	 * called once for the entire application.
	 */
	@PostConstruct
	public void init()
	{
		initOriginalHosts();
		hostSelectionStrategy.setHostList(originalHostList);

		// BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(100,
		// true);
		// Executor executor = new ThreadPoolExecutor(4, 4,
		// 20, TimeUnit.SECONDS, queue, new ThreadFactory()
		// {
		// ThreadFactory defThreadFactory = Executors.defaultThreadFactory();
		//
		// @Override
		// public Thread newThread(Runnable theObj)
		// {
		// Thread theThread = defThreadFactory.newThread(theObj);
		// theThread.setName("my-exec-" + theThread.getName());
		// return theThread;
		// }
		// }, new ThreadPoolExecutor.AbortPolicy());
		// connector.getFilterChain().addLast(
		// "executor",
		// new ExecutorFilter(executor));

		if (startNodeDiscovery)
		{
			nodeDiscovery = new NodeDiscovery();
			nodeDiscovery.start();
		}
	}

	/**
	 * Wait for response from grid.
	 * 
	 * @param gridSession
	 * @param startTime
	 * @param maxWaitDuration
	 */
	public void waitForResponse(TrellisSession gridSession, long startTime, long maxWaitDuration)
	{
		while (!gridSession.isResponseReady()
				&& maxWaitDuration > (System.currentTimeMillis() - startTime))
		{
			assert null != gridSession.getParent() : "GridSession.getParent should not be null";
			synchronized (gridSession.getParent())
			{
				try
				{
					gridSession.getParent().wait(500);
				}
				catch (InterruptedException e)
				{
					Thread.interrupted();
				}
			}
		}
	}

	private void initOriginalHosts()
	{
		originalHostList = new LinkedList<String>();
		if (null == hostsStr || hostsStr.isEmpty())
		{
			throw new TrellisException("Must set hosts string - comma separated list of hostnames");
		}

		Iterable<String> iterable = Splitter.on(',').trimResults().omitEmptyStrings()
				.split(hostsStr);
		for (String host : iterable)
		{
			try
			{
				originalHostList.add(InetAddress.getByName(host).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				throw new TrellisException("exception when parsing hosts list", e);
			}
		}

		if (originalHostList.isEmpty())
		{
			throw new TrellisException("No hosts supplied.  initialize host list parameter properly");
		}
	}

	/**
	 * Perform necessary actions to gracefully shutdown the client.
	 */
	@PreDestroy
	public void shutdown()
	{
		if (null != nodeDiscovery)
		{
			nodeDiscovery.shutdown();
		}

		connMgr.shutdown();
		gridSessMgr.shutdown();
	}

	public void setHostsStr(String hostsStr)
	{
		this.hostsStr = hostsStr;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setHostSelectionStrategy(HostSelectionStrategy hostSelectionStrategy)
	{
		this.hostSelectionStrategy = hostSelectionStrategy;
	}

	public boolean isStartNodeDiscovery()
	{
		return startNodeDiscovery;
	}

	public void setStartNodeDiscovery(boolean startNodeDiscovery)
	{
		this.startNodeDiscovery = startNodeDiscovery;
	}

	// ------------------------------

	/**
	 * Runs periodically to grab the live/unreachable nodes from cluster.
	 * 
	 */
	public class NodeDiscovery implements Runnable
	{
		private Thread theThread;
		private volatile boolean stopProcessing = false;

		public void start()
		{
			theThread = new Thread(this);
			theThread.setName(this.getClass().getSimpleName());
			theThread.start();
		}

		@Override
		public void run()
		{
			// try
			// {
			// Thread.sleep(1000);
			// }
			// catch (InterruptedException e1)
			// {
			// Thread.interrupted();
			// }

			while (!stopProcessing)
			{
				try
				{
					if (null == trellisConfig || trellisConfig.isTrellisInitializedAndReady())
					{
						discoverGridNodes();
					}
				}
				catch (Throwable e)
				{
					logger.error("exception while discovering nodes", e);
				}

				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e)
				{
					Thread.interrupted();
				}
			}
		}

		public void shutdown()
		{
			stopProcessing = true;
			if (null != theThread)
			{
				theThread.interrupt();
			}
		}

		//
		// asks grid for its node registration
		//
		private void discoverGridNodes()
		{
			GetNodesResult nodes = nodeDiscoverySrvc.execute();
			if (null != nodes)
			{
				logger.debug("discovered " + nodes.getLiveHosts() + " live nodes, yay!");
				hostSelectionStrategy.setHostList(new ArrayList<String>(nodes.getLiveHosts()));
			}
			else
			{
				logger.error("since cannot discover any nodes, will keep previous list of nodes");
				return;
			}

			// unreachableHosts = new
			// HashSet<String>(nodes.getUnreachableHosts());
		}

	}

	public void setNodeDiscoverySrvc(NodeDiscoveryService nodeDiscoverySrvc)
	{
		this.nodeDiscoverySrvc = nodeDiscoverySrvc;
	}

	public void setConnMgr(ConnectionMgr connMgr)
	{
		this.connMgr = connMgr;
	}

	public void setGridSessMgr(TrellisSessionMgr gridSessMgr)
	{
		this.gridSessMgr = gridSessMgr;
	}

}
