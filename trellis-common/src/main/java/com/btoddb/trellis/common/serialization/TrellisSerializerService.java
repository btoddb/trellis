
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
import java.util.Collection;

public interface TrellisSerializerService
{

	/**
	 * Serialize an object to a ByteBuffer supplied by the serializer service.
	 * 
	 * @param obj
	 * @return
	 */
	ByteBuffer serialize(Object obj);

	/**
	 * Serialize an object to a ByteBuffer supplied by the client.
	 * 
	 * @param bb
	 * @param obj
	 * @return
	 */
	ByteBuffer serialize(ByteBuffer bb, Object obj);

	/**
	 * Deserialize a ByteBuffer into Object form using the ByteBuffer to
	 * determine the object type.
	 * 
	 * @param bb
	 * @return
	 */
	Object deserialize(ByteBuffer bb);

	/**
	 * Count all the bytes as if they were serialized on the wire. This includes
	 * type ID, object sizes, etc.
	 * 
	 * @param obj
	 * @return
	 */
	int calculateSerializedObjectWireSize(Object obj);

	<T> SerializedTypeDef<T> findSerializerDefinitionByType(Class<T> clazz);

	SerializedTypeDef<?> findSerializerDefinitionById(String typeId);

	Collection<SerializedTypeDef<?>> GetAllSerializers();

	void registerSerializerDefinition(SerializedTypeDef<?> serTypeDef, boolean serializerSettable);

	void registerSerializerButDontPersist(SerializedTypeDef<?> serTypeDef, boolean serializerSettable);

}