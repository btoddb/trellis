
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("rawtypes")
public abstract class ListSerializerBaseImpl<T extends List> implements TrellisSerializer<T>
{
	private TrellisSerializerService serSrvc;
	private final TrellisSerializer<Integer> integerSerializer;

	public ListSerializerBaseImpl(TrellisSerializerService serSrvc)
	{
		this.serSrvc = serSrvc;
		integerSerializer = serSrvc.findSerializerDefinitionByType(Integer.class).getSerializer();
	}

	protected abstract T instantiateListObject(int size);

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(ByteBuffer bb)
	{
		int size = integerSerializer.deserialize(bb);
		T list = instantiateListObject(size);
		for (int i = 0; i < size; i++)
		{
			list.add(serSrvc.deserialize(bb));
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ByteBuffer serialize(ByteBuffer bb, T obj)
	{
		if (null != obj)
		{

			integerSerializer.serialize(bb, obj.size());
			for (Object elem : obj)
			{
				serSrvc.serialize(bb, elem);
			}
		}
		else
		{
			serialize(bb, (T) Collections.emptyList());
		}

		return bb;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int calculateSerializedSize(T obj)
	{
		if (null != obj)
		{
			int size = integerSerializer.calculateSerializedSize(obj.size());
			for (Object elem : obj)
			{
				size += serSrvc.calculateSerializedObjectWireSize(elem);
			}
			return size;
		}
		else
		{
			return calculateSerializedSize((T) Collections.emptyList());
		}
	}

}