
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class SetSerializer implements TrellisSerializer<Set<?>>
{
	private TrellisSerializerService serSrvc;

	public SetSerializer(TrellisSerializerService serSrvc)
	{
		this.serSrvc = serSrvc;
	}

	@Override
	public Set<?> deserialize(ByteBuffer bb)
	{
		int size = bb.getInt();
		Set<Object> coll = new LinkedHashSet<Object>();
		for (int i = 0; i < size; i++)
		{
			coll.add(serSrvc.deserialize(bb));
		}
		return coll;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Set<?> obj)
	{
		if (null != obj)
		{
			bb.putInt(obj.size());
			for (Object elem : obj)
			{
				serSrvc.serialize(bb, elem);
			}
		}
		else
		{
			serialize(bb, Collections.emptySet());
		}
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "SET";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return Set.class;
	}

	@Override
	public int calculateSerializedSize(Set<?> obj)
	{
		int size = IntegerSerializer.SERIALIZED_SIZE;
		for (Object elem : obj)
		{
			size += serSrvc.calculateSerializedObjectWireSize(elem);
		}
		return size;
	}
}
