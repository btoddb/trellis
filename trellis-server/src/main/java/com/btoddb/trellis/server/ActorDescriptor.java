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

import com.btoddb.trellis.actor.TrellisActor;
import com.btoddb.trellis.actor.TrellisPersistenceProvider;

public class ActorDescriptor
{
	private final String name;
	private final TrellisActor actor;
	private final TrellisPersistenceProvider persistenceProvider;
	
	public ActorDescriptor(String name, TrellisActor actor, TrellisPersistenceProvider persistenceProvider)
	{
		this.name = name;
		this.actor = actor;
		this.persistenceProvider = persistenceProvider;
	}

	public TrellisActor getActor()
	{
		return actor;
	}

	public String getName()
	{
		return name;
	}

	public TrellisPersistenceProvider getPersistenceProvider()
	{
		return persistenceProvider;
	}

	
}
