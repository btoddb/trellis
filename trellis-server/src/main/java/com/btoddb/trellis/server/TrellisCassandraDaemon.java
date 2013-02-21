
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

package com.btoddb.trellis.server;

import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.ExternalCacheManager;
import org.apache.cassandra.thrift.CassandraDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.btoddb.trellis.caching.CassandraMutationListener;
import com.btoddb.trellis.caching.TrellisCacheProvider;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.server.srvcloaders.ActorServiceLoader;

public class TrellisCassandraDaemon extends CassandraDaemon
{
	private static final Logger logger = LoggerFactory.getLogger(TrellisCassandraDaemon.class);

	private static boolean stopProcessing = false;
	private static Object waitMonitor = new Object();

	AbstractApplicationContext springContext;
	private TrellisServer gridServer;
	private TrellisCacheProvider cacheProvider;

	@Override
	protected void startServer()
	{
		super.startServer();
	}

	@Override
	protected void stopServer()
	{
		if (null != springContext)
		{
			springContext.close();
		}
		super.stopServer();
	}

	private void initSpring() throws Throwable
	{
		try
		{
			springContext = new ClassPathXmlApplicationContext("trellis-server-services.xml");
			springContext.registerShutdownHook();
		}
		catch (Throwable e)
		{
			throw new TrellisException("exception initializing spring context", e);
		}

	}

	private void startMutationListener()
	{
		try
		{
			ExternalCacheManager.init(springContext.getBean(CassandraMutationListener.class), true, true, true, true,
					true, true);
		}
		catch (ConfigurationException e)
		{
			throw new TrellisException("exception while initializing Cassandra mutation listener", e);
		}
	}

	private void startGrid()
	{
		gridServer = springContext.getBean(TrellisServer.class);
		gridServer.startGrid();
	}

	private void shutdown()
	{
		stopProcessing = true;
		synchronized (waitMonitor)
		{
			waitMonitor.notifyAll();
		}
	}

	public static void main(String[] args)
	{
		logger.info("***************************");
		logger.info("* Starting Trellis server *");
		logger.info("***************************");

		TrellisCassandraDaemon daemon = new TrellisCassandraDaemon();

		try
		{
			daemon.initSpring();
			daemon.startCache();
			daemon.initServiceLoaders();
			daemon.startMutationListener();
			daemon.activate();
			daemon.startGrid();
		}
		catch (Throwable e)
		{
			logger.error("exception while starting Trellis - shutting down", e);
			daemon.shutdown();
		}

		while (!stopProcessing)
		{
			synchronized (waitMonitor)
			{
				try
				{
					waitMonitor.wait(1000);
				}
				catch (InterruptedException e)
				{
					Thread.interrupted();
				}
			}
		}

		daemon.deactivate();

		if (null != daemon.cacheProvider)
		{
			daemon.cacheProvider.shutdown();
		}
	}

	private void initServiceLoaders()
	{
		ActorServiceLoader actorSrvcLoader = springContext.getBean(ActorServiceLoader.class);
		actorSrvcLoader.init();
	}

	private void startCache()
	{
		cacheProvider = (TrellisCacheProvider) springContext.getBean("trellisCache");
		cacheProvider.start();
	}

}
