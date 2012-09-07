package com.btoddb.trellis.client;

import com.btoddb.trellis.common.TransportSession;


public interface DurationCallback
{

	void totalDuration(TransportSession transSess, long durationInNanos);

}
