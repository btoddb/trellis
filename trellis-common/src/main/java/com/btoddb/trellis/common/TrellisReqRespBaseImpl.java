
package com.btoddb.trellis.common;



public abstract class TrellisReqRespBaseImpl implements TrellisReqResp
{
	private long sessionId;
	private long deserializeDuration;
	private long arrivalTime;
	private TrellisSession gridSession;

	public TrellisReqRespBaseImpl(long sessionId)
	{
		this.sessionId = sessionId;
	}

	public TrellisReqRespBaseImpl()
	{}

	public TrellisSession getGridSession()
	{
		return gridSession;
	}

	public void setGridSession(TrellisSession gridSession)
	{
		this.gridSession = gridSession;
	}

	@Override
	public long getSessionId()
	{
		return sessionId;
	}

	@Override
	public void setDeserializeDuration(long durationInMicros)
	{
		this.deserializeDuration = durationInMicros;
	}

	@Override
	public void setArrivalTime(long timestampInMs)
	{
		this.arrivalTime = timestampInMs;
	}

	@Override
	public long getDeserializeDuration()
	{
		return deserializeDuration;
	}

	@Override
	public long getArrivalTime()
	{
		return arrivalTime;
	}

	public void setSessionId(long sessionId)
	{
		this.sessionId = sessionId;
	}

}