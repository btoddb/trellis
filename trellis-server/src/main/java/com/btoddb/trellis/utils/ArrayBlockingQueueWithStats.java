
package com.btoddb.trellis.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.btoddb.trellis.common.JmxStatsHelper;
import com.btoddb.trellis.common.StopWatchInNanos;

public class ArrayBlockingQueueWithStats<E> implements BlockingQueue<E>
{
	private static final String STAT_QUEUE_SIZE = "queue-size";
	private static final String STAT_WAIT_TIME = "queue-wait";

	private ArrayBlockingQueue<ElementEnvelope> delegateQueue;
	
	private AtomicLong pushCount = new AtomicLong();
	private AtomicLong popCount = new AtomicLong();
	private String queueName;
	
	private JmxStatsHelper stats = new JmxStatsHelper(60000);

	public ArrayBlockingQueueWithStats(String queueName, int queueSize, boolean fairness)
	{
		this.queueName = queueName;
		
		delegateQueue = new ArrayBlockingQueue<ElementEnvelope>(queueSize, fairness);
	}

	public double getAvgWaitTime() {
		return stats.getRollingStat(STAT_WAIT_TIME).getAverageSample();
	}

	public double getMinWaitTime() {
		return stats.getRollingStat(STAT_WAIT_TIME).getMinimumSample();
	}

	public double getMaxWaitTime() {
		return stats.getRollingStat(STAT_WAIT_TIME).getMaximumSample();
	}

	public double getAvgQueueSize() {
		return stats.getRollingStat(STAT_QUEUE_SIZE).getAverageSample();
	}

	public double getMaxQueueSize() {
		return stats.getRollingStat(STAT_QUEUE_SIZE).getMaximumSample();
	}
	
	@Override
	public boolean add(E e)
	{
		ElementEnvelope ee = new ElementEnvelope(e);
		ee.sw.start();
		boolean success = delegateQueue.add(ee);
		if (success)
		{
			incrementPushStats();
		}
		return success;
	}

	public boolean offer(E e)
	{
		ElementEnvelope ee = new ElementEnvelope(e);
		ee.sw.start();
		boolean success = delegateQueue.offer(ee);
		if (success)
		{
			incrementPushStats();
		}
		return success;
	}

	@Override
	public int size()
	{
		return (int)(pushCount.get()-popCount.get());
	}

	public E take() throws InterruptedException
	{
		ElementEnvelope ee = delegateQueue.take();
		if (null != ee)
		{
			incrementPopStats();
		}
		ee.sw.stop();
		stats.addRollingSample(STAT_WAIT_TIME, 1, ee.sw.getDuratinInMicros());
		return ee.element;
	}

	private void incrementPopStats()
	{
		popCount.incrementAndGet();
		stats.addRollingSample(STAT_QUEUE_SIZE, 1, size());
	}

	private void incrementPushStats()
	{
		pushCount.incrementAndGet();
		stats.addRollingSample(STAT_QUEUE_SIZE, 1, size());
	}

	// ---- UNSUPPORTED METHODS ----

	@Override
	public E element()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E peek()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E poll()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection<? super E> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int drainTo(Collection<? super E> arg0, int arg1)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean offer(E arg0, long arg1, TimeUnit arg2) throws InterruptedException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E poll(long arg0, TimeUnit arg1) throws InterruptedException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void put(E arg0) throws InterruptedException
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public int remainingCapacity()
	{
		return delegateQueue.remainingCapacity();
	}

	@Override
	public boolean remove(Object arg0)
	{
		throw new UnsupportedOperationException();
	}

	// ------------

	class ElementEnvelope
	{
		E element;
		StopWatchInNanos sw = new StopWatchInNanos();

		public ElementEnvelope(E e)
		{
			element = e;
		}
	}

	public String getQueueName()
	{
		return queueName;
	}
}
