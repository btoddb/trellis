
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

package com.btoddb.trellis.server.stage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import com.btoddb.trellis.common.JmxStatsHelper;
import com.btoddb.trellis.common.StopWatchInNanos;
import com.btoddb.trellis.common.TrellisSession;
import com.btoddb.trellis.common.TrellisStatsJmxBean;
import com.btoddb.trellis.utils.ArrayBlockingQueueWithStats;

public abstract class StageController
{
	private static final Logger logger = LoggerFactory.getLogger(StageController.class);

	private static final String STAT_DURATION = "duration";

	private final Object threadAdjustMonitorObj = new Object();

	private ArrayBlockingQueueWithStats<TrellisSession> inQueue;
	private TrellisStatsJmxBean msgStatsMBean;
	private String stageName;

	private int numThreads;
	private int threadIdCount;
	private Set<StageProcessor> threadSet = new HashSet<StageProcessor>();
	private boolean started = false;
	
	private JmxStatsHelper processingTimeStat = new JmxStatsHelper(60000);


	public StageController()
	{}

	/**
	 * Stage must implement this method to do the 'read work'
	 * 
	 * @param data
	 */
	protected abstract void process(TrellisSession gridSession);

	public void start()
	{
		adjustThreadCount();
		started = true;
	}

	private void adjustThreadCount()
	{
		if (numThreads < threadSet.size())
		{
			Iterator<StageProcessor> iter = threadSet.iterator();
			while (threadSet.size() > numThreads)
			{
				StageProcessor sp = iter.next();
				iter.remove();
				sp.setStopProcessing(true);
			}
		}
		else if (numThreads > threadSet.size())
		{
			while (threadSet.size() < numThreads)
			{
				StageProcessor sp = new StageProcessor();
				Thread t = new Thread(sp);
				sp.setMyThread(t);
				t.setName(String.format("%s-%02d", stageName, threadIdCount++));
				t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
				{
					@Override
					public void uncaughtException(Thread t, Throwable e)
					{
						logger.error("uncaught exception on thread, " + t.getName(), e);
					}
				});
				t.start();
				threadSet.add(sp);
			}
		}
	}

	public void stopProcessing()
	{
		if (!threadSet.isEmpty())
		{
			for (StageProcessor sp : threadSet)
			{
				sp.setStopProcessing(true);
				sp.getMyThread().interrupt();
			}
		}
	}

//	private void handleException(GridSession gridSession, Throwable e, boolean sendResponse)
//	{
//		GridResponse res = new GridResponse(null, gridSession.getRequest().getSessionId(),
//				new GridRemoteException(e));
//		gridSession.setResponse(res);
//		responseQueue.offer(gridSession);
//	}

	public void setInQueue(ArrayBlockingQueueWithStats<TrellisSession> inQueue)
	{
		this.inQueue = inQueue;
	}

	public void setStageName(String stageName)
	{
		this.stageName = stageName;
	}

	@ManagedAttribute()
	public void setNumThreads(int coreStageThreads)
	{
		this.numThreads = coreStageThreads;
		if (numThreads != threadSet.size() && started)
		{
			synchronized (threadAdjustMonitorObj)
			{
				if (numThreads != threadSet.size())
				{
					adjustThreadCount();
				}
			}
		}
	}

	@ManagedAttribute
	public int getNumThreads()
	{
		return numThreads;
	}

	@ManagedAttribute
	public String getStageName()
	{
		return stageName;
	}
	
	@ManagedAttribute
	public double getProcessingTime_Avg() {
		return processingTimeStat.getRollingStat(STAT_DURATION).getAverageSample();
	}

	@ManagedAttribute
	public double getProcessingTime_Max() {
		return processingTimeStat.getRollingStat(STAT_DURATION).getMaximumSample();
	}

	@ManagedAttribute
	public double getProcessingTime_Min() {
		return processingTimeStat.getRollingStat(STAT_DURATION).getMinimumSample();
	}
	
	@ManagedAttribute
	public double getWaitTime_Avg() {
		return inQueue.getAvgWaitTime();
	}
	
	@ManagedAttribute
	public double getWaitTime_Min() {
		return inQueue.getMinWaitTime();
	}
	
	@ManagedAttribute
	public double getWaitTime_Max() {
		return inQueue.getMaxWaitTime();
	}
	
	@ManagedAttribute
	public double getQueueSize_Avg() {
		return inQueue.getAvgQueueSize();
	}
	
	@ManagedAttribute
	public double getQueueSize_Max() {
		return inQueue.getMaxQueueSize();
	}
	
	// --------------

	public TrellisStatsJmxBean getMsgStatsMBean()
	{
		return msgStatsMBean;
	}

	public void setMsgStatsMBean(TrellisStatsJmxBean msgStatsMBean)
	{
		this.msgStatsMBean = msgStatsMBean;
	}

	class StageProcessor implements Runnable
	{
		private volatile boolean stopProcessing = false;
		private Thread myThread;

		@Override
		public void run()
		{
			StopWatchInNanos sw = new StopWatchInNanos();
			while (!stopProcessing)
			{
				try
				{
					TrellisSession data = inQueue.take();
					sw.start();
					process(data);
					sw.stop();
					processingTimeStat.addRollingSample(STAT_DURATION, 1, sw.getDuratinInMicros());
				}
				catch (InterruptedException e)
				{
					Thread.interrupted();
				}
				catch (Throwable e)
				{
					logger.error("exception while processing during stage, "
							+ this.getClass().getName(), e);
				}
			}
		}

		public Thread getMyThread()
		{
			return myThread;
		}

		public void setMyThread(Thread myThread)
		{
			this.myThread = myThread;
		}

		public boolean isStopProcessing()
		{
			return stopProcessing;
		}

		public void setStopProcessing(boolean stopProcessing)
		{
			this.stopProcessing = stopProcessing;
			myThread.interrupt();
		}
	}
}
