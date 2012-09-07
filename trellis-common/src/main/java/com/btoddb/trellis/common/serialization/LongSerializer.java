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
