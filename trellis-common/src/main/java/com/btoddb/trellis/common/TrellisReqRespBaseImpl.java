
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