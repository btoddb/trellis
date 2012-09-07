
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
