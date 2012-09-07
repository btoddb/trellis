
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.Map;

import com.btoddb.trellis.common.TrellisResponse;
import com.btoddb.trellis.common.Instrumentation;

public class TrellisResponseSerializer implements TrellisSerializer<TrellisResponse>
{
	private TrellisSerializer<ByteBuffer> bbSerializer;
	private TrellisSerializer<Integer> intSerializer;
	private TrellisSerializer<Long> longSerializer;
	private TrellisSerializer<Boolean> booleanSerializer;
	@SuppressWarnings("rawtypes")
	private TrellisSerializer<Map> mapSerializer;

	public TrellisResponseSerializer(TrellisSerializerService serSrvc)
	{
		intSerializer = serSrvc.findSerializerDefinitionByType(Integer.class).getSerializer();
		bbSerializer = serSrvc.findSerializerDefinitionByType(ByteBuffer.class).getSerializer();
		longSerializer = serSrvc.findSerializerDefinitionByType(Long.class).getSerializer();
		booleanSerializer = serSrvc.findSerializerDefinitionByType(Boolean.class).getSerializer();
		mapSerializer = serSrvc.findSerializerDefinitionByType(Map.class).getSerializer();
	}

	@Override
	public String getTypeId()
	{
		return "GRESP";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return TrellisResponseSerializer.class;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, TrellisResponse obj)
	{
		// serializing this integer reserves space during Encoder process for
		// timing this serialization operation
		intSerializer.serialize(bb, 0);

		bbSerializer.serialize(bb, obj.getKey());
		longSerializer.serialize(bb, obj.getSessionId());
		booleanSerializer.serialize(bb, obj.isInstrumented());
		mapSerializer.serialize(bb, obj.getData());

		if (obj.isInstrumented())
		{
			mapSerializer.serialize(bb, obj.getInstrumentation().getMap());
		}

		return bb;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TrellisResponse deserialize(ByteBuffer bb)
	{
		TrellisResponse obj = new TrellisResponse();
		obj.setSerializationDuration(intSerializer.deserialize(bb));
		obj.setKey(bbSerializer.deserialize(bb));
		obj.setSessionId(longSerializer.deserialize(bb));
		boolean instrumented = booleanSerializer.deserialize(bb);
		obj.setData(mapSerializer.deserialize(bb));
		if (instrumented)
		{
			obj.setInstrumentation(new Instrumentation(mapSerializer.deserialize(bb)));
		}
		return obj;
	}

	@Override
	public int calculateSerializedSize(TrellisResponse obj)
	{
		return intSerializer.calculateSerializedSize(obj.getSerializationDuration())
				+ bbSerializer.calculateSerializedSize(obj.getKey())
				+ longSerializer.calculateSerializedSize(obj.getSessionId())
				+ booleanSerializer.calculateSerializedSize(obj.isInstrumented())
				+ mapSerializer.calculateSerializedSize(obj.getData())
				+ (obj.isInstrumented() ? mapSerializer.calculateSerializedSize(obj
						.getInstrumentation().getMap()) : 0);
	}

}
