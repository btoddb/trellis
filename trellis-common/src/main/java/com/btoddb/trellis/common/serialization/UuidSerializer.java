
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
import java.util.UUID;

public class UuidSerializer implements TrellisSerializer<UUID>
{
	private final TrellisSerializer<Long> longSerializer;

	public UuidSerializer(TrellisSerializerService serSrvc)
	{
		this.longSerializer = serSrvc.findSerializerDefinitionByType(Long.class).getSerializer();
	}

	@Override
	public UUID deserialize(ByteBuffer bb)
	{
		return new UUID(longSerializer.deserialize(bb), longSerializer.deserialize(bb));
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, UUID obj)
	{
		longSerializer.serialize(bb, obj.getMostSignificantBits());
		longSerializer.serialize(bb, obj.getLeastSignificantBits());
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "UUID";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return UUID.class;
	}

	@Override
	public int calculateSerializedSize(UUID obj)
	{
		return longSerializer.calculateSerializedSize(obj.getMostSignificantBits())
				+ longSerializer.calculateSerializedSize(obj.getLeastSignificantBits());
	}
}
