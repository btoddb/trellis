
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

package com.btoddb.trellis.common;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class Instrumentation
{
	public static final String REQ_DESER = "req-deser";
	public static final String OPERATION_ONLY = "grid-op";
	public static final String TOTAL_TIME = "grid-total-time";
	public static final String SERIALIZATION = "grid-ser";
	public static final String REPLY = "grid-reply";

	private Map<String, List<Integer>> map = new ConcurrentHashMap<String, List<Integer>>();

	public Instrumentation(Map<String, List<Integer>> map)
	{
		this.map.putAll(map);
	}

	public Instrumentation()
	{}

	public Instrumentation addDuration(String id, int duration)
	{
		if (null == id)
		{
			throw new TrellisException("instrumented attribute cannot be NULL!");
		}

		List<Integer> list = map.get(id);
		if (null == list)
		{
			list = Collections.synchronizedList(new LinkedList<Integer>());
			map.put(id, list);
		}
		list.add(duration);

		return this;
	}

	public Instrumentation addDuration(String id, long duration)
	{
		return addDuration(id, (int) duration);
	}

	public Map<String, List<Integer>> getMap()
	{
		return map;
	}
}
