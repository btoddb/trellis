package com.btoddb.trellis.common;

import java.net.SocketAddress;



public interface TrellisReqResp
{

	/**
	 * Don't serialize the {@link TrellisSession} object during wire communication.
	 * 
	 * @return
	 */
	TrellisSession getGridSession();
	
	long getSessionId();

	long getDeserializeDuration();
	void setDeserializeDuration( long durationInMicros );

	long getArrivalTime();
	void setArrivalTime(long timestampInMs);

	void setRemoteAddress(SocketAddress remoteAddress);
}
