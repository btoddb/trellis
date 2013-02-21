
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
