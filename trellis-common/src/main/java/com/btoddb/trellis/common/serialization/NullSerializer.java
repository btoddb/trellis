package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;

public class NullSerializer implements TrellisSerializer<Object>
{

	@Override
	public Object deserialize(ByteBuffer bb)
	{
		return null;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Object obj)
	{
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "NUL";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return null;
	}

	@Override
	public int calculateSerializedSize(Object obj)
	{
		return 0;
	}

}
