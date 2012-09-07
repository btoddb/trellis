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
