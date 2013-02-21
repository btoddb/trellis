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

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BigIntegerSerializer implements TrellisSerializer<BigInteger>
{
	private static TrellisSerializer<ByteBuffer> bbSerializer;

	public BigIntegerSerializer(TrellisSerializerService serSrvc) {
		bbSerializer = serSrvc.findSerializerDefinitionByType(ByteBuffer.class).getSerializer();
	}
	
	@Override
	public BigInteger deserialize(ByteBuffer bb)
	{
		ByteBuffer obj = bbSerializer.deserialize(bb);
		return new BigInteger(obj.array());
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, BigInteger obj)
	{
		byte[] arr = obj.toByteArray();
		bbSerializer.serialize(bb, ByteBuffer.wrap(arr));
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "BI";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return BigInteger.class;
	}

	@Override
	public int calculateSerializedSize(BigInteger obj)
	{
		return bbSerializer.calculateSerializedSize(ByteBuffer.wrap(obj.toByteArray()));
	}

}
