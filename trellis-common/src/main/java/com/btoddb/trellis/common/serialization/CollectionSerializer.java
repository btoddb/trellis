
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
public class CollectionSerializer implements TrellisSerializer<Collection>
{
	private TrellisSerializerService serSrvc;
	private final TrellisSerializer<Integer> integerSerializer;

	public CollectionSerializer(TrellisSerializerService serSrvc)
	{
		this.serSrvc = serSrvc;
		integerSerializer = serSrvc.findSerializerDefinitionByType(Integer.class).getSerializer();
	}

	@Override
	public Collection deserialize(ByteBuffer bb)
	{
		int size = integerSerializer.deserialize(bb);
		Set<Object> coll = new HashSet<Object>();
		for (int i = 0; i < size; i++)
		{
			coll.add(serSrvc.deserialize(bb));
		}
		return coll;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Collection obj)
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
			serialize(bb, Collections.emptySet());
		}
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "COL";
	}

	@Override
	public Class getType()
	{
		return Collection.class;
	}

	@Override
	public int calculateSerializedSize(Collection obj)
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
			return calculateSerializedSize(Collections.emptySet());
		}
	}
}
