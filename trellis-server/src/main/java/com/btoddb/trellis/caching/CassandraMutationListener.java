
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

package com.btoddb.trellis.caching;

import java.util.List;

import javax.management.MBeanServer;

import org.apache.cassandra.notify.ExternalCacheEventListener;
import org.apache.cassandra.notify.MutationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.actor.TrellisDataTranslator;
import com.btoddb.trellis.server.srvcloaders.ActorServiceLoader;

public class CassandraMutationListener implements ExternalCacheEventListener
{
	private static Logger logger = LoggerFactory.getLogger(CassandraMutationListener.class);

	private MBeanServer mBeanServer;
	private TrellisCacheProvider cacheProvider;
	private ActorServiceLoader actorLoader;

	public CassandraMutationListener()
	{}

	public void start()
	{}

	public void shutdown()
	{}

	/**
	 * 
	 * @see org.apache.cassandra.notify.ExternalCacheEventListener#mutationNotification(List)
	 */
	@Override
	public void mutationNotification(List<MutationEvent> eventList)
	{
		if (null == eventList || eventList.isEmpty())
		{
			logger.error("event list from Cassandra mutation notification is empty - should not be");
			return;
		}

		for (MutationEvent event : eventList)
		{
			TrellisDataTranslator cassDataTranslator = actorLoader
					.getCassDataTransInstance(event.getKeyspaceName(), event.getColumnFamilyName());
			if (null == cassDataTranslator)
			{
				// nothing to do since no mapping from colfam to translator
				return;
			}

//			byte[] key = ;
			CacheKey cacheKey = new CacheKey(event.getKeyspaceName(), event.getColumnFamilyName(), com.btoddb.trellis.common.Utils
					.safeByteBufferArrayAccess(event.getKey().key));

			Object obj = cacheProvider.get(cacheKey);
			if (null != obj)
			{
				TrellisCacheLock lock = cacheProvider.lock(cacheKey, true, 0);
				try
				{
					// get again now that we're in sync section
					obj = cacheProvider.get(cacheKey);
					
					// only update if cache is not empty
					if (null != obj)
					{
						Object row = cassDataTranslator.translateRow(cacheKey.getArr(), event.getColumnFamily()
								.getSortedColumns(), obj);
						cacheProvider.put(cacheKey, row);
					}
				}
				finally
				{
					cacheProvider.unlock(lock);
				}
			}
		}
	}

	public void setmBeanServer(MBeanServer mBeanServer)
	{
		this.mBeanServer = mBeanServer;
	}

	public void setCacheProvider(TrellisCacheProvider cacheProvider)
	{
		this.cacheProvider = cacheProvider;
	}

	public void setActorLoader(ActorServiceLoader actorLoader)
	{
		this.actorLoader = actorLoader;
	}

}
