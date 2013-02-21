
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

import java.util.LinkedList;

public class LinkedListSerializer extends ListSerializerBaseImpl<LinkedList<?>>
{

	public LinkedListSerializer(TrellisSerializerService serSrvc)
	{
		super(serSrvc);
	}

	@Override
	public String getTypeId()
	{
		return "LL";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		return LinkedList.class;
	}

	@Override
	protected LinkedList<?> instantiateListObject(int size)
	{
		return new LinkedList<Object>();
	}
}
