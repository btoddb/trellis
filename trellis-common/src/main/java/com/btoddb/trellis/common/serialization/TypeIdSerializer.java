
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

import com.btoddb.trellis.common.TrellisException;

public class TypeIdSerializer implements TrellisSerializer<String>
{
	@Override
	public String deserialize(ByteBuffer bb)
	{
		byte len = bb.get();
		byte[] arr = new byte[len];
		bb.get(arr, 0, len);
		String tmp = new String(arr);
		return tmp;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, String obj)
	{
		if (255 < obj.length())
		{
			throw new TrellisException("Object type ID cannot be longer than 255 chars");
		}

		bb.put((byte) obj.length());
		bb.put(obj.getBytes());
		return bb;
	}

	@Override
	public String getTypeId()
	{
		throw new TrellisException("method is unsupported");
		// return "ID";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		throw new UnsupportedOperationException("getType is not supported on this class ("
				+ this.getClass().getName() + ")");
	}

	@Override
	public int calculateSerializedSize(String obj)
	{
		return 1 + obj.length();
	}

}
