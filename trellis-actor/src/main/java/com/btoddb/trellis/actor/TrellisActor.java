
package com.btoddb.trellis.actor;

import java.nio.ByteBuffer;
import java.util.Map;

import com.btoddb.trellis.common.PersistentKey;
import com.btoddb.trellis.common.StatsAggregate;


/**
 * Base class all actors must implement.
 *
 */
public abstract class TrellisActor
{
	private String actorName;
	
	public TrellisActor( String actorName ) {
		this.actorName = actorName;
	}
	
	public abstract ByteBuffer execute(Object params, Map<PersistentKey, Object> data, StatsAggregate stats);

	public String getActorName()
	{
		return actorName;
	}
}
