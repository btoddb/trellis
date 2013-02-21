
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
