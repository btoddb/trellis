
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;

public class FloatSerializer implements TrellisSerializer<Float>
{

	public static final int SERIALIZED_SIZE;
	static
	{
		ByteBuffer bb = ByteBuffer.allocate(10);
		bb.putFloat(1f);
		SERIALIZED_SIZE = bb.position();
	}

	@Override
	public Float deserialize(ByteBuffer bb)
	{
		return bb.getFloat();
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Float obj)
	{
		return bb.putFloat(obj);
	}

	@Override
	public String getTypeId()
	{
		return "FLT";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Float.class;
	}

	@Override
	public int calculateSerializedSize(Float obj)
	{
		return SERIALIZED_SIZE;
	}

}
