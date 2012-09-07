
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
