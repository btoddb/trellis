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

public class ByteBufferSerializer implements TrellisSerializer<ByteBuffer>
{

	@Override
	public ByteBuffer deserialize(ByteBuffer bb)
	{
		int size = bb.getInt();
		ByteBuffer tmp = bb.slice();
		tmp.limit(size);
		ByteBuffer obj = ByteBuffer.allocate(size);
		obj.put(tmp);
		obj.flip();
		bb.position(bb.arrayOffset() + bb.position() + size);
		return obj;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, ByteBuffer obj)
	{
		ByteBuffer dup;
		if ( null != obj ) {
			dup = obj.duplicate();
		}
		else {
			dup = ByteBuffer.allocate(0);
		}

		
		bb.putInt(dup.remaining());
		bb.put(dup);
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "BB";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return ByteBuffer.class;
	}

	@Override
	public int calculateSerializedSize(ByteBuffer obj)
	{
		if ( null == obj ) {
			return IntegerSerializer.SERIALIZED_SIZE;
		}
		
		return IntegerSerializer.SERIALIZED_SIZE + obj.remaining();
	}

}
