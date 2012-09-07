
package com.btoddb.trellis.common;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


public class InstrumentationAggregator
{
	private static final ThreadLocal<Map<String, Stat>> stats = new ThreadLocal<Map<String, Stat>>()
		{
			@Override
			protected Map<String, Stat> initialValue()
			{
				return new ConcurrentHashMap<String, Stat>();
			}
		};

	public static void addSampleInMicros(String key, int value)
	{
		Stat s = stats.get().get(key);
		if (null == s)
		{
			s = new Stat(key, 0);
			stats.get().put(key, s);
		}
		s.addSample(1, value);
	}

	public static void addSampleInMicros(String key, long value)
	{
		addSampleInMicros(key, (int) value);
	}

	public static void saveResults(Map<String, List<Integer>> instruments)
	{
		if (null == instruments || instruments.isEmpty())
		{
			return;
		}

		for (Entry<String, List<Integer>> entry : instruments.entrySet())
		{
			String key = entry.getKey();
			List<Integer> valList = entry.getValue();

			for (Integer sample : valList)
			{
				if (null == key || null == sample)
				{
					System.out.println("key = " + key + " : " + valList.toString());
				}
				addSampleInMicros(key, sample);
			}
		}
	}

	public static Map<String, Stat> getStats()
	{
		return stats.get();
	}

	public static void clear()
	{
		stats.get().clear();
	}

}
