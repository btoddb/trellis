
package com.btoddb.trellis.common.serialization;

import java.util.ArrayList;
import java.util.List;

public class GenericListSerializer extends ListSerializerBaseImpl<List<?>>
{

	public GenericListSerializer(TrellisSerializerService serSrvc)
	{
		super(serSrvc);
	}

	@Override
	public String getTypeId()
	{
		return "GL";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return List.class;
	}

	@Override
	protected ArrayList<?> instantiateListObject(int size)
	{
		return new ArrayList<Object>(size);
	}
}
