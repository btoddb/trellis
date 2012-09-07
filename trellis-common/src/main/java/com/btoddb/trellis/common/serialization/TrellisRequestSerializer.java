
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.common.TrellisRequest;

public class TrellisRequestSerializer implements TrellisSerializer<TrellisRequest>
{
	private static final Logger logger = LoggerFactory.getLogger(TrellisRequestSerializer.class);

	private final TrellisSerializer<Boolean> booleanSerializer;
	private final TrellisSerializer<Long> longSerializer;
	private final TrellisSerializer<Integer> integerSerializer;
	private TrellisSerializerService serSrvc;
	private TrellisSerializer<String> stringSerializer;

	public TrellisRequestSerializer(TrellisSerializerService serSrvc)
	{
		this.serSrvc = serSrvc;

		booleanSerializer = serSrvc.findSerializerDefinitionByType(Boolean.class).getSerializer();
		stringSerializer = serSrvc.findSerializerDefinitionByType(String.class).getSerializer();
		longSerializer = serSrvc.findSerializerDefinitionByType(Long.class).getSerializer();
		integerSerializer = serSrvc.findSerializerDefinitionByType(Integer.class).getSerializer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public TrellisRequest deserialize(ByteBuffer bb)
	{
		TrellisRequest obj = new TrellisRequest();
		obj.setInstrument(booleanSerializer.deserialize(bb));

		obj.setSessionId(longSerializer.deserialize(bb));
		logger.debug("session ID = " + obj.getSessionId());

		obj.setForwardCount(integerSerializer.deserialize(bb));
		obj.setActorName(stringSerializer.deserialize(bb));
		obj.setKeyList((List<ByteBuffer>) serSrvc.deserialize(bb));
		obj.setKeyspaceName((String) serSrvc.deserialize(bb));
		obj.setData(serSrvc.deserialize(bb));
		return obj;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, TrellisRequest obj)
	{
		booleanSerializer.serialize(bb, obj.isInstrument());

		longSerializer.serialize(bb, obj.getSessionId());
		logger.debug("tracking ID = " + obj.getSessionId());

		integerSerializer.serialize(bb, obj.getForwardCount());
		stringSerializer.serialize(bb, obj.getActorName());
		serSrvc.serialize(bb, obj.getKeyList());
		serSrvc.serialize(bb, obj.getKeyspaceName());
		serSrvc.serialize(bb, obj.getData());

		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "GREQ";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return TrellisRequestSerializer.class;
	}

	@Override
	public int calculateSerializedSize(TrellisRequest obj)
	{
		return booleanSerializer.calculateSerializedSize(obj.isInstrument())
				+ longSerializer.calculateSerializedSize(obj.getSessionId())
				+ integerSerializer.calculateSerializedSize(obj.getForwardCount())
				+ stringSerializer.calculateSerializedSize(obj.getActorName())
				+ serSrvc.calculateSerializedObjectWireSize(obj.getKeyList())
				+ serSrvc.calculateSerializedObjectWireSize(obj.getKeyspaceName())
				+ serSrvc.calculateSerializedObjectWireSize(obj.getData());
	}

}
