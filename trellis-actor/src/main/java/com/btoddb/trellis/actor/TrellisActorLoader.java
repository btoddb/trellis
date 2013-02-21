
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

/**
 * Actors must have a class that implements this interface for defining the actor's class, name, dependencies, and an
 * optional persistence provider.
 * 
 * <p/>
 * Loads actors on-demand via the ServiceLoader facility. As an actor "Service Provider" you should follow instructions
 * here, {@link http ://download.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html}. See trellis-actor-example
 * and README.txt for help.
 */
public interface TrellisActorLoader
{

	/**
	 * Provides the unique name used to identify this actor. The name must not conflict with any other actor name in the
	 * Trellis cluster.
	 * 
	 * @return
	 */
	String getActorName();

	/**
	 * Provides the class name for this actor.  Trellis will instantiate when needed.
	 * @return
	 */
	Class<? extends TrellisActor> getActorClass();

	// Map<Class<?>, Class<? extends TrellisSerializer<?>>> getSerializerClasses();

	Class<?>[] getSingletonDependencies();

	Class<? extends TrellisPersistenceProvider> getPersistenceProvider();

	KeyspaceColFamKey[] getColumnFamilyNames();

	Class<? extends TrellisDataTranslator> getDataTranslator();

}
