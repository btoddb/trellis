
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

import com.btoddb.trellis.common.TrellisRemoteException;

public class TrellisRemoteExceptionSerializer implements TrellisSerializer<TrellisRemoteException>
{
	private TrellisSerializer<String> stringSerializer;

	public TrellisRemoteExceptionSerializer(TrellisSerializerService serSrvc)
	{
		this.stringSerializer = serSrvc.findSerializerDefinitionByType(String.class)
				.getSerializer();
	}

	@Override
	public TrellisRemoteException deserialize(ByteBuffer bb)
	{
		TrellisRemoteException obj = new TrellisRemoteException();
		obj.setClassName(stringSerializer.deserialize(bb));
		obj.setMsg(stringSerializer.deserialize(bb));
		obj.setStackTrace(stringSerializer.deserialize(bb));
		return obj;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, TrellisRemoteException obj)
	{
		stringSerializer.serialize(bb, obj.getClassName());
		stringSerializer.serialize(bb, obj.getMsg());
		stringSerializer.serialize(bb, obj.getStackTrace());
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "EX";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return TrellisRemoteException.class;
	}

	@Override
	public int calculateSerializedSize(TrellisRemoteException obj)
	{
		return stringSerializer.calculateSerializedSize(obj.getClassName())
				+ stringSerializer.calculateSerializedSize(obj.getMsg())
				+ stringSerializer.calculateSerializedSize(obj.getStackTrace());
	}

}
