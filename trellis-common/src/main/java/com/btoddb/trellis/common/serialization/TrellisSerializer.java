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


/**
 * Defines interface for custom serializers.  If you have a custom type, you must create and
 * register a custom serializer so the serialization system can handle the new type.
 *
 * @param <T> The custom type
 */
public interface TrellisSerializer<T>
{
	T deserialize( ByteBuffer bb );
	
	ByteBuffer serialize( ByteBuffer bb, T obj );

	String getTypeId();
	
	@SuppressWarnings("rawtypes")
	Class getType();
	
	int calculateSerializedSize( T obj );

}
