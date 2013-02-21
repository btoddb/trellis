
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

public class StringSerializer implements TrellisSerializer<String>
{
	private final TrellisSerializer<Integer> integerSerializer;

	public StringSerializer(TrellisSerializerService serSrvc)
	{
		integerSerializer = serSrvc.findSerializerDefinitionByType(Integer.class).getSerializer();
	}

	@Override
	public String deserialize(ByteBuffer bb)
	{
		int len = integerSerializer.deserialize(bb);
		byte[] arr = new byte[len];
		bb.get(arr, 0, len);
		String tmp = new String(arr);
		return tmp;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, String obj)
	{
		if (null != obj)
		{
			integerSerializer.serialize(bb, obj.getBytes().length);
			bb.put(obj.getBytes());
		}
		else
		{
			// can' serialize a NULL string easily at the moment, so just do
			// empty
			integerSerializer.serialize(bb, 0);
		}
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "STR";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return String.class;
	}

	@Override
	public int calculateSerializedSize(String obj)
	{
		if (null != obj)
		{
			return integerSerializer.calculateSerializedSize(obj.getBytes().length)
					+ obj.getBytes().length;
		}
		else
		{
			return integerSerializer.calculateSerializedSize(0);
		}
	}

}
