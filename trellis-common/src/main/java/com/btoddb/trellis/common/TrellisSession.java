
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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrellisSession
{

	private static Logger logger = LoggerFactory.getLogger(TrellisSession.class);

	private Long sessionId;

	private StatsAggregate stats = new StatsAggregate();
	private Instrumentation instrumentation = new Instrumentation();
	private TrellisRequest request;
	private TrellisResponse response;
	private TransportSession transportSession;
	// private Object finalizerLockMonitor = new Object();

	private long creationTime;
	private long deserDurationInMicros;

	private long requestSentTimeInNanos;
	private long requestQueuedTimeInNanos;
	private long responseQueuedTimeInNanos;
	private long responseReceivedTimeInNanos;
	private boolean remoteCall;

	// session parent/group stuff
	private TrellisSession parent;
	private Map<Long, TrellisSession> unprocessedMap = new ConcurrentHashMap<Long, TrellisSession>();
	private Map<Long, TrellisSession> processedMap = new ConcurrentHashMap<Long, TrellisSession>();
	private AtomicInteger numExpectedChildren = new AtomicInteger();
	private boolean finishedProcessing = false;
	private Stat remoteDurationStat = new Stat("remote", 0);
	private Stat localDurationStat = new Stat("local", 0);

	private AtomicBoolean lockedForFinalizing = new AtomicBoolean(false);

	public TrellisSession(TransportSession transportSession)
	{
		this.creationTime = System.currentTimeMillis();
		this.transportSession = transportSession;
	}

	public TrellisSession(TransportSession transportSession, long sessionId)
	{
		this(transportSession);
		this.sessionId = sessionId;
	}

	public TrellisRequest getRequest()
	{
		return request;
	}

	public void setRequest(TrellisRequest request)
	{
		this.request = request;
	}

	public TransportSession getTransportSession()
	{
		return transportSession;
	}

	public TrellisResponse getResponse()
	{
		return response;
	}

	public void setResponse(TrellisResponse response)
	{
		this.response = response;
	}

	public boolean isResponseReady()
	{
		return null != response;
	}

	public long getCreationTime()
	{
		return creationTime;
	}

	public void setCreationTime(long creationTime)
	{
		this.creationTime = creationTime;
	}

	public long getDeserDurationInMicros()
	{
		return deserDurationInMicros;
	}

	public void setDeserDurationInMicros(long deserializationDurationInMicros)
	{
		this.deserDurationInMicros = deserializationDurationInMicros;
	}

	public StatsAggregate getStats()
	{
		return stats;
	}

	public Long getSessionId()
	{
		return sessionId;
	}

	public void addSample(String key, long sample)
	{
		this.instrumentation.addDuration(key, sample);
	}

	public void addInstrumentation(Instrumentation instrumentation)
	{
		for (Entry<String, List<Integer>> entry : instrumentation.getMap().entrySet())
		{
			for (Integer sample : entry.getValue())
			{
				addSample(entry.getKey(), sample);
			}
		}
	}

	public Instrumentation getInstrumentation()
	{
		return instrumentation;
	}

	public long getResponseQueuedTimeInNanos()
	{
		return responseQueuedTimeInNanos;
	}

	public void setResponseQueuedTimeInNanos(long responseQueuedTimeInNanos)
	{
		this.responseQueuedTimeInNanos = responseQueuedTimeInNanos;
	}

	public long getRequestQueuedTimeInNanos()
	{
		return requestQueuedTimeInNanos;
	}

	public void setRequestQueuedTimeInNanos(long requestQueuedTimeInNanos)
	{
		this.requestQueuedTimeInNanos = requestQueuedTimeInNanos;
	}

	public long getResponseReceivedTimeInNanos()
	{
		return responseReceivedTimeInNanos;
	}

	public void setResponseReceivedTimeInNanos(long responseReceivedTimeInNanos)
	{
		this.responseReceivedTimeInNanos = responseReceivedTimeInNanos;
	}

	public void setRemoteCall(boolean remoteCall)
	{
		this.remoteCall = remoteCall;
	}

	public boolean isRemoteCall()
	{
		return remoteCall;
	}

	public TrellisSession getParent()
	{
		return parent;
	}

	public void setParent(TrellisSession sessParent)
	{
		this.parent = sessParent;
	}

	public void addSessionChild(TrellisSession gridSession)
	{
		gridSession.setParent(this);
		unprocessedMap.put(gridSession.getSessionId(), gridSession);

	}

	public void setNumExpectedChildren(int numOfExpectedChildren)
	{
		this.numExpectedChildren.set(numOfExpectedChildren);
	}

	public boolean isFinishedMovingSessions()
	{
		return 0 == numExpectedChildren.get() || processedMap.size() == numExpectedChildren.get();
	}

	public Map<Long, TrellisSession> getProcessedMap()
	{
		return processedMap;
	}

	public void adjustNumberOfRequests(int numberOfRequests)
	{
		this.numExpectedChildren.addAndGet(numberOfRequests);
	}

	public void moveSessionToProcessed(TrellisSession gridSession)
	{
		TrellisSession sess = unprocessedMap.get(gridSession.getSessionId());
		if (null == sess)
		{
			throw new TrellisException("trying to move grid session with ID, " + gridSession.getSessionId()
					+ ", from unprocessed to processed, but doesn't exist in unprocessed");
		}

		// make sure to put before remove so processing will happen properly
		processedMap.put(sess.getSessionId(), sess);
		unprocessedMap.remove(gridSession.getSessionId());
		logger.debug("moved session ID, " + gridSession.getSessionId());
	}

	public void addRemoteDurationSample(long durationInNanos)
	{
		remoteDurationStat.addSample(1, durationInNanos);
	}

	public void addLocalDurationSample(long durationInNanos)
	{
		localDurationStat.addSample(1, durationInNanos);
	}

	public boolean isFinishedProcessing()
	{
		return finishedProcessing;
	}

	public void setFinishedProcessing(boolean finishedProcessing)
	{
		this.finishedProcessing = finishedProcessing;
	}

	public long getClientSessionId()
	{
		return getRequest().getSessionId();
	}

	public long getLocalDurationAvg()
	{
		return localDurationStat.getAverageSample() / 1000;
	}

	public long getLocalDurationMax()
	{
		return localDurationStat.getAverageSample() / 1000;
	}

	public long getLocalDurationMin()
	{
		return localDurationStat.getMinimumSample() / 1000;
	}

	public long getRemoteDurationAvgInMicros()
	{
		return remoteDurationStat.getAverageSample() / 1000;
	}

	public long getRemoteDurationMaxInMicros()
	{
		return remoteDurationStat.getMaximumSample() / 1000;
	}

	public long getRemoteDurationMinInMicros()
	{
		return remoteDurationStat.getMinimumSample() / 1000;
	}

	public boolean isPartOfSessionGroup()
	{
		return null != parent && 0 < parent.numExpectedChildren.get();
	}

	public void setRequestSentTimeInNanos(long endWriteTime)
	{
		this.requestSentTimeInNanos = endWriteTime;
	}

	public long getRequestSentTimeInNanos()
	{
		return requestSentTimeInNanos;
	}

	/**
	 * 
	 * @return true if the 'finalize' locked is granted to this thread. the lock will forever be locked and cannot be
	 *         unlocked. false if lock is not granted
	 */
	public boolean lockForFinalizing()
	{
		return lockedForFinalizing.compareAndSet(false, true);
	}

}
