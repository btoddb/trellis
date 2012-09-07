package com.btoddb.trellis.common;



public interface TransportSession
{

	void write( Object obj );
	
	long getWriteDurationInMicros();

	void setGridSession(TrellisSession gridSession);
	
	TrellisSession getGridSession();
	
	String getHostName();
	
}
