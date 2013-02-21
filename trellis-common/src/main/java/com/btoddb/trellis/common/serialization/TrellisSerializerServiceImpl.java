
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

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.DependencyInjectionService;
import com.btoddb.trellis.common.GetNodesResult;
import com.btoddb.trellis.common.TrellisRequest;
import com.btoddb.trellis.common.TrellisResponse;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.TrellisRemoteException;

@Service("serSrvc")
public class TrellisSerializerServiceImpl implements TrellisSerializerService
{
	private static final Logger logger = LoggerFactory.getLogger(TrellisSerializerServiceImpl.class);

	public static final TypeIdSerializer typeIdSerializer = new TypeIdSerializer();

	@Autowired(required = false)
	private DependencyInjectionService depInjSrvc;
	@Autowired(required = false)
	private SerializerLoaderService serLoaderSrvc;

	@Value("$gridProps{server-mode}")
	private boolean serverMode = false;

	private Map<String, SerializedTypeDef<?>> idToDef = new HashMap<String, SerializedTypeDef<?>>();
	private Map<String, SerializedTypeDef<?>> classToDef = new LinkedHashMap<String, SerializedTypeDef<?>>();

	private SerializedTypeDef<Serializable> javaSerTypeDef = new SerializedTypeDef<Serializable>(
			null, new JavaSerializer());

	@PostConstruct
	public void init()
	{
		registerDefaultSerializers();

		if (!serverMode)
		{
			logger.info("grid serializer service init'ed in client-only mode");
			return;
		}

		depInjSrvc.registerSettableFieldType(this);
	}

	@SuppressWarnings("rawtypes")
	public void registerDefaultSerializers()
	{
		// primitive serializers with no dependencies
		registerSerializerButDontPersist(new SerializedTypeDef<Object>(null, new NullSerializer()),
				true);
		registerSerializerButDontPersist(new SerializedTypeDef<ByteBuffer>(ByteBuffer.class,
				new ByteBufferSerializer()), true);
		registerSerializerButDontPersist(new SerializedTypeDef<Boolean>(Boolean.class,
				new BooleanSerializer()), true);
		registerSerializerButDontPersist(new SerializedTypeDef<Float>(Float.class,
				new FloatSerializer()), true);
		registerSerializerButDontPersist(new SerializedTypeDef<Integer>(Integer.class,
				new IntegerSerializer()), true);
		registerSerializerButDontPersist(new SerializedTypeDef<Long>(Long.class,
				new LongSerializer()), true);
		registerSerializerButDontPersist(new SerializedTypeDef<Date>(Date.class,
				new DateSerializer()), true);

		// more primitives, but depend on the primitives above
		registerSerializerButDontPersist(new SerializedTypeDef<String>(String.class,
				new StringSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<UUID>(UUID.class,
				new UuidSerializer(this)), true);

		// collections depend on a primitive serializer
		registerSerializerButDontPersist(new SerializedTypeDef<ArrayList<?>>(ArrayList.class,
				new ArrayListSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<LinkedList<?>>(LinkedList.class,
				new LinkedListSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<Map<?, ?>>(Map.class,
				new MapSerializer(this)), true);

		// generic serializers for collections, must come after specific
		// collections
		registerSerializerButDontPersist(new SerializedTypeDef<Set<?>>(Set.class,
				new SetSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<List<?>>(Arrays.asList().getClass(),
				new GenericListSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<List<?>>(List.class,
				new GenericListSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<Collection>(Collection.class,
				new CollectionSerializer(this)), true);

		// Java serializable
		registerSerializerButDontPersist(javaSerTypeDef, true);

		// cassandra specific types
		registerSerializerButDontPersist(new SerializedTypeDef<BigInteger>(BigInteger.class,
				new BigIntegerSerializer(this)), true);

		// top level grid objects
		registerSerializerButDontPersist(new SerializedTypeDef<TrellisRequest>(TrellisRequest.class,
				new TrellisRequestSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<TrellisResponse>(TrellisResponse.class,
				new TrellisResponseSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<TrellisRemoteException>(
				TrellisRemoteException.class, new TrellisRemoteExceptionSerializer(this)), true);
		registerSerializerButDontPersist(new SerializedTypeDef<GetNodesResult>(
				GetNodesResult.class, new GetNodesResultSerializer(this)), true);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SerializedTypeDef findSerializerDefinitionByType(Class clazz)
	{
		SerializedTypeDef serTypeDef = null;
		if (null == clazz)
		{
			serTypeDef = (SerializedTypeDef) idToDef.get("NUL");
		}
		else
		{
			serTypeDef = (SerializedTypeDef) classToDef.get(clazz.getName());
			if (null == serTypeDef)
			{
				for (SerializedTypeDef<?> tmp : classToDef.values())
				{
					if (null != tmp.getType() && tmp.getType().isAssignableFrom(clazz))
					{
						serTypeDef = (SerializedTypeDef) tmp;
						break;
					}
				}
			}
		}

		if (null == serTypeDef && Serializable.class.isAssignableFrom(clazz))
		{
			serTypeDef = javaSerTypeDef;
		}

		return serTypeDef;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public SerializedTypeDef findSerializerDefinitionById(String typeId)
	{
		SerializedTypeDef serTypeDef = idToDef.get(typeId);
		if (null != serTypeDef)
		{
			return serTypeDef;
		}

		TrellisSerializer ser = serLoaderSrvc.getSerializerInstance(typeId);
		if (null != ser)
		{
			serTypeDef = new SerializedTypeDef(typeId, ser.getType(), ser);
		}

		if (null == serTypeDef)
		{
			throw new TrellisException("Could not find serializer for type ID, " + typeId
					+ ".  Did you register a serializer for it?");
		}

		return serTypeDef;
	}

	/**
	 * Supports basic types (Integer, Float, etc) without any work on client
	 * part.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ByteBuffer serialize(ByteBuffer bb, Object obj)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("serializing type, "
					+ (null != obj ? obj.getClass().getSimpleName() : null) + " : before = "
					+ bb.toString());
		}

		@SuppressWarnings("rawtypes")
		SerializedTypeDef serTypeDef = (SerializedTypeDef<Object>) findSerializerDefinitionByType(null != obj ? obj
				.getClass() : null);

		if (null == serTypeDef)
		{
			throw new TrellisException("Could not find serializer for class, "
					+ obj.getClass().getName() + ".  Did you register a serializer for it?");
		}

		typeIdSerializer.serialize(bb, serTypeDef.getId());
		serTypeDef.getSerializer().serialize(bb, obj);

		if (logger.isDebugEnabled())
		{
			logger.debug("serializing type, "
					+ (null != obj ? obj.getClass().getSimpleName() : null) + " : after = "
					+ bb.toString());
		}

		return bb;
	}

	/**
	 * @see #serialize(ByteBuffer, Object) *
	 * @see com.btoddb.trellis.TrellisSerializerService.GridSerializerService#serialize(java.lang.Object)
	 */
	@Override
	public ByteBuffer serialize(Object obj)
	{
		ByteBuffer bb = ByteBuffer.allocate(calculateSerializedObjectWireSize(obj));
		serialize(bb, obj);
		bb.flip();
		return bb;
	}

	/**
	 * Deserialize based on the object's type ID in the first part of the
	 * ByteBuffer.
	 * 
	 * @see com.btoddb.trellis.TrellisSerializerService.GridSerializerService#deserialize(java.nio.ByteBuffer)
	 */
	@Override
	public Object deserialize(ByteBuffer bb)
	{
		String typeId = null;
		SerializedTypeDef<?> serTypeDef = null;
		try
		{
			typeId = typeIdSerializer.deserialize(bb);

			if (logger.isDebugEnabled())
			{
				logger.debug("deserializing type, " + typeId + " : before = " + bb.toString());
			}

			serTypeDef = findSerializerDefinitionById(typeId);
			Object obj = serTypeDef.getSerializer().deserialize(bb);

			if (logger.isDebugEnabled())
			{
				logger.debug("deserializing type, " + typeId + " : after = " + bb.toString());
			}

			return obj;
		}
		catch (TrellisException e)
		{
			throw new TrellisException("typeId = " + typeId + " : serTypeDef = "
					+ (null != serTypeDef ? serTypeDef.toString() : null), e);
		}
		catch (Throwable e)
		{
			throw new TrellisException("exception while trying to deserialize buffer into object", e);
		}
	}

	@SuppressWarnings("unchecked")
	public int calculateSerializedObjectWireSize(Object obj)
	{
		SerializedTypeDef<Object> serTypeDef;
		if (null != obj)
		{
			serTypeDef = (SerializedTypeDef<Object>) findSerializerDefinitionByType(obj.getClass());
		}
		else
		{
			serTypeDef = (SerializedTypeDef<Object>) findSerializerDefinitionByType(null);
		}
		TrellisSerializer<Object> ser = serTypeDef.getSerializer();
		int size = typeIdSerializer.calculateSerializedSize(serTypeDef.getId());
		size += ser.calculateSerializedSize(obj);
		return size;
	}

	public void registerSerializerDefinition(SerializedTypeDef<?> serTypeDef,
			boolean serializerSettable)
	{
		registerSerializerButDontPersist(serTypeDef, serializerSettable);
	}

	public void registerSerializerButDontPersist(SerializedTypeDef<?> serTypeDef,
			boolean serializerSettable)
	{
		idToDef.put(serTypeDef.getId(), serTypeDef);
		if (null != serTypeDef.getType())
		{
			classToDef.put(serTypeDef.getType().getName(), serTypeDef);
		}
		if (serverMode && serializerSettable)
		{
			depInjSrvc.registerSettableFieldType(serTypeDef.getSerializer());
		}
	}

	@Override
	public Collection<SerializedTypeDef<?>> GetAllSerializers()
	{
		return classToDef.values();
	}

	public void setServerMode(boolean serverMode)
	{
		this.serverMode = serverMode;
	}

//	public void setClassInstSrvc(ClassInstantiationService classInstSrvc)
//	{
//		this.classInstSrvc = classInstSrvc;
//	}

}
