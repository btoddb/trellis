
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.Date;

public class DateSerializer implements TrellisSerializer<Date>
{

	@Override
	public Date deserialize(ByteBuffer bb)
	{
		return new Date(bb.getLong());
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Date obj)
	{
		return bb.putLong(obj.getTime());
	}

	@Override
	public String getTypeId()
	{
		return "DT";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Date.class;
	}

	@Override
	public int calculateSerializedSize(Date obj)
	{
		return LongSerializer.SERIALIZED_SIZE;
	}

}
