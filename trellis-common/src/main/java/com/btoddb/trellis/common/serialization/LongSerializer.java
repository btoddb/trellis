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

public class LongSerializer implements TrellisSerializer<Long>
{

	public static final int SERIALIZED_SIZE;
	static
	{
		ByteBuffer bb = ByteBuffer.allocate(10);
		bb.putLong(1);
		SERIALIZED_SIZE = bb.position();
	}

	@Override
	public Long deserialize(ByteBuffer bb)
	{
		return bb.getLong();
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Long obj)
	{
		return bb.putLong(obj);
	}

	@Override
	public String getTypeId()
	{
		return "LNG";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Long.class;
	}

	@Override
	public int calculateSerializedSize(Long obj)
	{
		return SERIALIZED_SIZE;
	}

}
