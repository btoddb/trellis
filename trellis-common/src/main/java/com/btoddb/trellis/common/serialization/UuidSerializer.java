
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidSerializer implements TrellisSerializer<UUID>
{
	private final TrellisSerializer<Long> longSerializer;

	public UuidSerializer(TrellisSerializerService serSrvc)
	{
		this.longSerializer = serSrvc.findSerializerDefinitionByType(Long.class).getSerializer();
	}

	@Override
	public UUID deserialize(ByteBuffer bb)
	{
		return new UUID(longSerializer.deserialize(bb), longSerializer.deserialize(bb));
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, UUID obj)
	{
		longSerializer.serialize(bb, obj.getMostSignificantBits());
		longSerializer.serialize(bb, obj.getLeastSignificantBits());
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "UUID";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return UUID.class;
	}

	@Override
	public int calculateSerializedSize(UUID obj)
	{
		return longSerializer.calculateSerializedSize(obj.getMostSignificantBits())
				+ longSerializer.calculateSerializedSize(obj.getLeastSignificantBits());
	}
}
