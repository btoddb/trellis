
package com.btoddb.trellis.common.serialization;

import java.util.ArrayList;

public class ArrayListSerializer extends ListSerializerBaseImpl<ArrayList<?>>
{

	public ArrayListSerializer(TrellisSerializerService serSrvc)
	{
		super(serSrvc);
	}

	@Override
	public String getTypeId()
	{
		return "AL";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return ArrayList.class;
	}

	@Override
	protected ArrayList<?> instantiateListObject(int size)
	{
		return new ArrayList<Object>(size);
	}
}
