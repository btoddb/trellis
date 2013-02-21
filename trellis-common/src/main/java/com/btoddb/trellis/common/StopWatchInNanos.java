
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
