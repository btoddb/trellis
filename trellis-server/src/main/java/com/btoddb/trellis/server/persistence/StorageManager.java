
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

package com.btoddb.trellis.server.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.actor.KeyspaceColFamKey;
import com.btoddb.trellis.actor.TrellisPersistenceProvider;
import com.btoddb.trellis.caching.TrellisCacheProvider;
import com.btoddb.trellis.common.PersistentKey;

public class StorageManager
{
	private static Logger logger = LoggerFactory.getLogger(StorageManager.class);
	
	private TrellisCacheProvider cacheProvider;

	public Map<PersistentKey, Object> get(TrellisPersistenceProvider provider,
			PersistentKey[] keyArr)
	{
		Map<PersistentKey, Object> retData = new HashMap<PersistentKey, Object>();

		KeyspaceColFamKey ksColFamKey = provider.getKeyspaceColFamKey();

		// check cache first
		List<PersistentKey> neededList = new ArrayList<PersistentKey>();
		for (PersistentKey key : keyArr)
		{
			Object cacheObj = cacheProvider.get(ksColFamKey.getKeyspaceName(),
					ksColFamKey.getColFamName(), key.getRowKeyAsByteArray());
			if (null != cacheObj)
			{
				retData.put(key, cacheObj);
			}
			else
			{
				neededList.add(key);
			}
		}

		// get rest from persistence provider
		Map<PersistentKey, Object> persObjMap = Collections.emptyMap();
		if (!neededList.isEmpty())
		{
			logger.debug( "need " + neededList.size() + " keys from storage out of " + keyArr.length + " keys");
			persObjMap = provider.load(neededList);
		}
		else {
			logger.debug( "cache provided all " + keyArr.length + " data objects");
		}
		
		// save new data from storage in cache
		for (Entry<PersistentKey, Object> entry : persObjMap.entrySet())
		{
			cacheProvider.put(ksColFamKey.getKeyspaceName(), ksColFamKey.getColFamName(), entry
					.getKey().getRowKeyAsByteArray(), entry.getValue());
		}

		retData.putAll(persObjMap);
		return retData;
	}

	public void setCacheProvider(TrellisCacheProvider cacheProvider)
	{
		this.cacheProvider = cacheProvider;
	}
}
