package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;

public class ByteBufferSerializer implements TrellisSerializer<ByteBuffer>
{

	@Override
	public ByteBuffer deserialize(ByteBuffer bb)
	{
		int size = bb.getInt();
		ByteBuffer tmp = bb.slice();
		tmp.limit(size);
		ByteBuffer obj = ByteBuffer.allocate(size);
		obj.put(tmp);
		obj.flip();
		bb.position(bb.arrayOffset() + bb.position() + size);
		return obj;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, ByteBuffer obj)
	{
		ByteBuffer dup;
		if ( null != obj ) {
			dup = obj.duplicate();
		}
		else {
			dup = ByteBuffer.allocate(0);
		}

		
		bb.putInt(dup.remaining());
		bb.put(dup);
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "BB";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return ByteBuffer.class;
	}

	@Override
	public int calculateSerializedSize(ByteBuffer obj)
	{
		if ( null == obj ) {
			return IntegerSerializer.SERIALIZED_SIZE;
		}
		
		return IntegerSerializer.SERIALIZED_SIZE + obj.remaining();
	}

}
