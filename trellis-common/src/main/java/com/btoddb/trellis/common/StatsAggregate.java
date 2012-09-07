
package com.btoddb.trellis.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class StatsAggregate
{
	private Map<String, Stat> statsMap = new ConcurrentHashMap<String, Stat>();
	private Object statAddMonitor = new Object();

	public void addStat(Stat stat)
	{
		synchronized (statAddMonitor)
		{
			Stat oldStat = statsMap.get(stat.getName());
			if (null == oldStat)
			{
				statsMap.put(stat.getName(), stat);
			}
			else {
				oldStat.addSample(stat);
			}
		}
	}

	public Map<String, Stat> getStatsMap()
	{
		return statsMap;
	}
}
