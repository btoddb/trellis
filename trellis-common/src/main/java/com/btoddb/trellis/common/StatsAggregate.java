
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
