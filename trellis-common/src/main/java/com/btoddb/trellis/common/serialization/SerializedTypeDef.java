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

public class SerializedTypeDef<T>
{
	private String id;
	private Class<?> type;
	private TrellisSerializer<T> serializer;
	
	public SerializedTypeDef(String id, Class<?> type, TrellisSerializer<T> serializer)
	{
		this.id = id;
		this.type = type;
		this.serializer = serializer;
	}

	public SerializedTypeDef(Class<?> type, TrellisSerializer<T> serializer)
	{
		this(serializer.getTypeId(), type, serializer);
	}

	public String getId()
	{
		return id;
	}

	public Class<?> getType()
	{
		return type;
	}

	public TrellisSerializer<T> getSerializer()
	{
		return serializer;
	}

	@Override
	public String toString()
	{
		return "SerializedTypeDef [id=" + id + ", type=" + type + ", serializer=" + serializer.getClass().getSimpleName()
				+ "]";
	}
}
