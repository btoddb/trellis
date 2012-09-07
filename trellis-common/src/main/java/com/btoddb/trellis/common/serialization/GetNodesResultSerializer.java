
package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.List;

import com.btoddb.trellis.common.GetNodesResult;

public class GetNodesResultSerializer implements TrellisSerializer<GetNodesResult>
{
	@SuppressWarnings("rawtypes")
	private TrellisSerializer<List> listSerializer;

	public GetNodesResultSerializer(TrellisSerializerService serSrvc)
	{
		listSerializer = serSrvc.findSerializerDefinitionByType(List.class).getSerializer();
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, GetNodesResult obj)
	{
		listSerializer.serialize(bb, obj.getLiveHosts());
		listSerializer.serialize(bb, obj.getUnreachableHosts());
		return bb;
	}

	@SuppressWarnings("unchecked")
	@Override
	public GetNodesResult deserialize(ByteBuffer bb)
	{
		GetNodesResult obj = new GetNodesResult();
		obj.setLiveHosts((List<String>) listSerializer.deserialize(bb));
		obj.setUnreachableHosts((List<String>) listSerializer.deserialize(bb));
		return obj;
	}

	@Override
	public String getTypeId()
	{
		return "GRTHM";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return GetNodesResult.class;
	}

	@Override
	public int calculateSerializedSize(GetNodesResult obj)
	{
		return listSerializer.calculateSerializedSize(obj.getLiveHosts())
				+ listSerializer.calculateSerializedSize(obj.getUnreachableHosts());
	}
}
