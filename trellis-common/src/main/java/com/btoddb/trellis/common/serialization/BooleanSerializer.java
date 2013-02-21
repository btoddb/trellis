
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

public class BooleanSerializer implements TrellisSerializer<Boolean>
{

	public static final int SERIALIZED_SIZE = 1;

	@Override
	public Boolean deserialize(ByteBuffer bb)
	{
		return 1 == bb.get();
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Boolean obj)
	{
		return bb.put((byte)(obj ? 1 : 0));
	}

	@Override
	public String getTypeId()
	{
		return "BOO";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Boolean.class;
	}

	@Override
	public int calculateSerializedSize(Boolean obj)
	{
		return SERIALIZED_SIZE;
	}

}
