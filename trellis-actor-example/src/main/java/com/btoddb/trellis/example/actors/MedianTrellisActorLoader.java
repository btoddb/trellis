
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

package com.btoddb.trellis.example.actors;

import com.btoddb.trellis.actor.KeyspaceColFamKey;
import com.btoddb.trellis.actor.TrellisActor;
import com.btoddb.trellis.actor.TrellisActorLoader;
import com.btoddb.trellis.actor.TrellisDataTranslator;
import com.btoddb.trellis.actor.TrellisPersistenceProvider;

/**
 * Example of a {@link TrellisActorLoader} for the {@link MedianTrellisActor} actor.
 * 
 */
public class MedianTrellisActorLoader implements TrellisActorLoader
{

	@Override
	public String getActorName()
	{
		return MedianTrellisActor.ACTOR_NAME;
	}

	@Override
	public Class<? extends TrellisActor> getActorClass()
	{
		return MedianTrellisActor.class;
	}
	
//	@Override
//	public Map<Class<?>, Class<? extends TrellisSerializer<?>>> getSerializerClasses()
//	{
//		Map<Class<?>, Class<? extends TrellisSerializer<?>>> map = new LinkedHashMap<Class<?>, Class<? extends TrellisSerializer<?>>>();
//		return map;
//	}

	@Override
	public Class<?>[] getSingletonDependencies()
	{
		return new Class[] { DataPointComparator.class };
	}

	@Override
	public Class<? extends TrellisPersistenceProvider> getPersistenceProvider()
	{
		return MedianPersistenceProvider.class;
	}

	@Override
	public KeyspaceColFamKey[] getColumnFamilyNames()
	{
		return new KeyspaceColFamKey[] { new KeyspaceColFamKey(MedianPersistenceProvider.KEYSPACE_NAME,
				MedianPersistenceProvider.CF_MEDIAN) };
	}

	@Override
	public Class<? extends TrellisDataTranslator> getDataTranslator()
	{
		return DataPointTranslator.class;
	}

}
