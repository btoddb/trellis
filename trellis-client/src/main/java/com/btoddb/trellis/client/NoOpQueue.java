package com.btoddb.trellis.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.btoddb.trellis.common.TrellisSession;

/**
 * This queue does nothing.  It is intended for use by {@link TrellisCluster} when used in client-mode.
 *
 */
public class NoOpQueue implements BlockingQueue<TrellisSession>
{

	@Override
	public TrellisSession element()
	{
		return null;
	}

	@Override
	public TrellisSession peek()
	{
		return null;
	}

	@Override
	public TrellisSession poll()
	{
		return null;
	}

	@Override
	public TrellisSession remove()
	{
		return null;
	}

	@Override
	public boolean addAll(Collection<? extends TrellisSession> arg0)
	{
		return false;
	}

	@Override
	public void clear()
	{
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		return false;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public Iterator<TrellisSession> iterator()
	{
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		return false;
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public Object[] toArray()
	{
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0)
	{
		return null;
	}

	@Override
	public boolean add(TrellisSession arg0)
	{
		return false;
	}

	@Override
	public boolean contains(Object arg0)
	{
		return false;
	}

	@Override
	public int drainTo(Collection<? super TrellisSession> arg0)
	{
		return 0;
	}

	@Override
	public int drainTo(Collection<? super TrellisSession> arg0, int arg1)
	{
		return 0;
	}

	@Override
	public boolean offer(TrellisSession arg0)
	{
		return false;
	}

	@Override
	public boolean offer(TrellisSession arg0, long arg1, TimeUnit arg2) throws InterruptedException
	{
		return false;
	}

	@Override
	public TrellisSession poll(long arg0, TimeUnit arg1) throws InterruptedException
	{
		return null;
	}

	@Override
	public void put(TrellisSession arg0) throws InterruptedException
	{		
	}

	@Override
	public int remainingCapacity()
	{
		return 0;
	}

	@Override
	public boolean remove(Object arg0)
	{
		return false;
	}

	@Override
	public TrellisSession take() throws InterruptedException
	{
		return null;
	}

}
