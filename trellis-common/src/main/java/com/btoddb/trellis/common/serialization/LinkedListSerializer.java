
package com.btoddb.trellis.common.serialization;

import java.util.LinkedList;

public class LinkedListSerializer extends ListSerializerBaseImpl<LinkedList<?>>
{

	public LinkedListSerializer(TrellisSerializerService serSrvc)
	{
		super(serSrvc);
	}

	@Override
	public String getTypeId()
	{
		return "LL";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return LinkedList.class;
	}

	@Override
	protected LinkedList<?> instantiateListObject(int size)
	{
		return new LinkedList<Object>();
	}
}
