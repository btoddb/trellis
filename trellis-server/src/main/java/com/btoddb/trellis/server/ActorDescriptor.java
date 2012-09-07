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
