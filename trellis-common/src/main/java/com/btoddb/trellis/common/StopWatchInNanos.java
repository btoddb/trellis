
package com.btoddb.trellis.common;

import java.util.concurrent.TimeUnit;

public class StopWatchInNanos
{
	private long start;
	private Long stop;

	public StopWatchInNanos clear() {
		start = 0;
		stop = null;
		return this;
	}
	
	public StopWatchInNanos start()
	{
		clear();
		start = System.nanoTime();
		return this;
	}

	public StopWatchInNanos stop()
	{
		stop = System.nanoTime();
		return this;
	}

	public long getDurationInNanos()
	{
		return null != stop ? stop - start : System.nanoTime() - start;
	}

	public long getDuratinInMicros()
	{
		return TimeUnit.NANOSECONDS.toMicros(getDurationInNanos());
	}

	public long getDuratinInMillis()
	{
		return TimeUnit.NANOSECONDS.toMillis(getDurationInNanos());
	}

	public long getStartTimeInNanos()
	{
		return start;
	}

	public long getStopTimeInNanos()
	{
		return stop;
	}

	public long getDuratinInSeconds()
	{
		return TimeUnit.NANOSECONDS.toSeconds(getDurationInNanos());
	}
}
