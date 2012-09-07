
package com.btoddb.trellis.actor;

import java.util.List;
import java.util.Map;

import com.btoddb.trellis.common.PersistentKey;

/**
 * Actor's wanting custom persistence must provide a class implementing this interface and register it using
 * {@link TrellisPersistenceLoader}.
 * 
 * <p/>
 * Loads actors on-demand via the ServiceLoader facility. As an actor
 * "Service Provider" you should follow instructions here, {@link http
 * ://download.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html}. The
 * service type is, {@link TrellisActor}. {@link TrellisActor#getActorName()} must
 * return a unique name across all actors in the grid.
 * 
 **/
public interface TrellisPersistenceProvider
{
	void init();

	void shutdown();

	void save(PersistentKey key, Object value);

	Map<PersistentKey, Object> load(PersistentKey[] keyArr);

	Map<PersistentKey, Object> load(List<PersistentKey> keyList);

	KeyspaceColFamKey getKeyspaceColFamKey();
}
