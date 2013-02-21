
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

package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SetSerializer implements TrellisSerializer<Set<?>>
{
	private TrellisSerializerService serSrvc;

	public SetSerializer(TrellisSerializerService serSrvc)
	{
		this.serSrvc = serSrvc;
	}

	@Override
	public Set<?> deserialize(ByteBuffer bb)
	{
		int size = bb.getInt();
		Set<Object> coll = new LinkedHashSet<Object>();
		for (int i = 0; i < size; i++)
		{
			coll.add(serSrvc.deserialize(bb));
		}
		return coll;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Set<?> obj)
	{
		if (null != obj)
		{
			bb.putInt(obj.size());
			for (Object elem : obj)
			{
				serSrvc.serialize(bb, elem);
			}
		}
		else
		{
			serialize(bb, Collections.emptySet());
		}
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "SET";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Set.class;
	}

	@Override
	public int calculateSerializedSize(Set<?> obj)
	{
		int size = IntegerSerializer.SERIALIZED_SIZE;
		for (Object elem : obj)
		{
			size += serSrvc.calculateSerializedObjectWireSize(elem);
		}
		return size;
	}
}
