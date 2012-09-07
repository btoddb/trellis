
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
