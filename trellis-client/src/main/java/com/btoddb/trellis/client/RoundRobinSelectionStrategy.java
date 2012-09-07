
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
