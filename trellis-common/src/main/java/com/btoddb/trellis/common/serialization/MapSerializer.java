
/*
 * Copyright 2013 B. Todd Burruss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.common.TrellisException;

@SuppressWarnings("rawtypes")
public class MapSerializer implements TrellisSerializer<Map<?, ?>>
{
	private static final Logger logger = LoggerFactory.getLogger(MapSerializer.class);

	private final TrellisSerializer<Integer> integerSerializer;

	private TrellisSerializerService serSrvc;

	public MapSerializer(TrellisSerializerService serSrvc)
	{
		this.serSrvc = serSrvc;
		integerSerializer = serSrvc.findSerializerDefinitionByType(Integer.class).getSerializer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map deserialize(ByteBuffer bb)
	{
		int size = integerSerializer.deserialize(bb);
		logger.debug("deserializing {} map entries", size);
		Map map = new HashMap();
		for (int i = 0; i < size; i++)
		{
			logger.debug("deserializing entry {}", (i + 1));
			try
			{
				Object key = serSrvc.deserialize(bb);
				Object val = serSrvc.deserialize(bb);
				map.put(key, val);
			}
			catch (Exception e)
			{
				throw new TrellisException("exception while deserializing map entry, " + (i + 1)
						+ ", of size " + size + " : " + bb, e);
			}
		}
		logger.debug("finished deserializing {} entries", size);
		return map;
	}

	@Override
	public ByteBuffer serialize(ByteBuffer bb, Map obj)
	{
		logger.debug("serializing {} map entries", obj.size());

		integerSerializer.serialize(bb, obj.size());
		int count = 0;
		for (Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet())
		{
			logger.debug("serializing entry {}", ++count);
			serSrvc.serialize(bb, entry.getKey());
			serSrvc.serialize(bb, entry.getValue());
		}

		logger.debug("finished serializing {} entries", count);
		return bb;
	}

	@Override
	public String getTypeId()
	{
		return "MAP";
	}

	@Override
	public Class getType()
	{
		return Map.class;
	}

	@Override
	public int calculateSerializedSize(Map obj)
	{
		int size = integerSerializer.calculateSerializedSize(obj.size());
		for (Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet())
		{
			size += serSrvc.calculateSerializedObjectWireSize(entry.getKey());
			size += serSrvc.calculateSerializedObjectWireSize(entry.getValue());
		}
		return size;
	}

}
