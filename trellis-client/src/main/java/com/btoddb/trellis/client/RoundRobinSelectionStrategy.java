
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

package com.btoddb.trellis.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.TrellisException;


@Service("roundRobinSelectionStrategy")
public class RoundRobinSelectionStrategy implements HostSelectionStrategy
{
	private ArrayList<String> hostList;
	private AtomicInteger nextHostIndex = new AtomicInteger(0);
	private Object accessMonitor = new Object();

	public RoundRobinSelectionStrategy()
	{}

	/**
	 * @see com.btoddb.trellis.client.HostSelectionStrategy#selectNext()
	 */
	@Override
	public String selectNext()
	{
		if ( null == hostList ) {
			throw new TrellisException( "host list has not been initialized, cannot select one");
		}
		
		synchronized (accessMonitor)
		{
			return hostList.get(nextHostIndex.getAndIncrement() % hostList.size());
		}
	}

	@Override
	public void setHostList(List<String> hostList)
	{
		synchronized (accessMonitor)
		{
			this.hostList = new ArrayList<String>(hostList);
		}
	}
}
