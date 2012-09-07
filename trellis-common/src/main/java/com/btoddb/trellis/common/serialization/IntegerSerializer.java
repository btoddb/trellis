
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;

public class IntegerSerializer implements TrellisSerializer<Integer>
{

	public static final int SERIALIZED_SIZE;
	static
	{
		ByteBuffer bb = ByteBuffer.allocate(10);
		bb.putInt(1);
		SERIALIZED_SIZE = bb.position();
	}

	@Override
	public Integer deserialize(ByteBuffer bb)
	{
		return bb.getInt();
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Integer obj)
	{
		return bb.putInt(obj);
	}

	@Override
	public String getTypeId()
	{
		return "INT";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Integer.class;
	}

	@Override
	public int calculateSerializedSize(Integer obj)
	{
		return SERIALIZED_SIZE;
	}

}
