
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

package com.btoddb.trellis.example.actors;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.actor.TrellisActor;
import com.btoddb.trellis.common.PersistentKey;
import com.btoddb.trellis.common.StatsAggregate;
import com.btoddb.trellis.common.TrellisException;

/**
 * Calculates the median price for a date over a collection of hotel IDs.
 * 
 * @author <a href="mailto:bburruss@expedia.com">bburruss</a>
 * 
 */
public class MedianTrellisActor extends TrellisActor
{
	private static final Logger logger = LoggerFactory.getLogger(MedianTrellisActor.class);

	public static final String ACTOR_NAME = "median-query";
	
	private DataPointComparator dataPointComparator;

	public MedianTrellisActor()
	{
		super(ACTOR_NAME);
	}

	@Override
	public ByteBuffer execute(Object params, Map<PersistentKey, Object> data, StatsAggregate stats)
	{
		if (null == data)
		{
			throw new TrellisException(getClass().getSimpleName()
					+ " requires exactly 1 data object - you gave a null object");
		}

		if (1 != data.size())
		{
			throw new TrellisException(getClass().getSimpleName()
					+ " can only work on one key/value pair at a time - do not send multiple pairs");
		}

		@SuppressWarnings("unchecked")
		Set<Integer> colIds = (Set<Integer>)params;
		
		Entry<PersistentKey, Object> entry = data.entrySet().iterator().next();
		
		DataPoint[] dpList = (DataPoint[]) entry.getValue();
		
		try
		{
			List<DataPoint> medianList = new ArrayList<DataPoint>( colIds.size());

			for (DataPoint dp : dpList)
			{
				if (colIds.contains(dp.getColName()))
				{
					medianList.add(dp);
				}
			}

			Collections.sort(medianList, dataPointComparator);

			// If n is odd then the median is x[(n-1)/2].
			// If n is even than the median is ( x[n/2] + x[(n/2)-1] ) / 2.
			int medianIndex = (medianList.size() - 1) / 2;
			DataPoint theMedian = medianList.get(medianIndex);

			ByteBuffer retBB = ByteBuffer.allocate(4+4);
			retBB.putInt(theMedian.getColName()).putInt(theMedian.getColValue()).rewind();
			return retBB;
		}
		catch (Throwable e)
		{
			logger.error("exception while calculating median price", e);
			return null;
		}
	}

	public void setDataPointComparator(DataPointComparator dataPointComparator)
	{
		this.dataPointComparator = dataPointComparator;
	}

}
