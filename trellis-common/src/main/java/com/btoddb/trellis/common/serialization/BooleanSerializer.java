
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;

public class BooleanSerializer implements TrellisSerializer<Boolean>
{

	public static final int SERIALIZED_SIZE = 1;

	@Override
	public Boolean deserialize(ByteBuffer bb)
	{
		return 1 == bb.get();
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Boolean obj)
	{
		return bb.put((byte)(obj ? 1 : 0));
	}

	@Override
	public String getTypeId()
	{
		return "BOO";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Boolean.class;
	}

	@Override
	public int calculateSerializedSize(Boolean obj)
	{
		return SERIALIZED_SIZE;
	}

}
