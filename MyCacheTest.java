
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

package com.btoddb.trellis.cassandra;

import static org.junit.Assert.assertEquals;

import org.apache.cassandra.cache.ICache;
import org.apache.cassandra.cache.LinkedHashMapCache;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MyCacheTest
{
	private static ICache<Integer, String> cache;

	@Test
	public void testGetPutRemovePlentyCapacity()
	{
		int maxVal = 100;
		int capacity = maxVal;

		cache.setCapacity(capacity);

		assertEquals(capacity, cache.capacity());
		assertEquals(0, cache.size());

		// put some data in there
		for (int i = 0; i < maxVal; i++)
		{
			cache.put(i, String.valueOf(i));
		}

		assertEquals(capacity, cache.capacity());
		assertEquals(maxVal, cache.size());

		// get the data and remove it
		for (int i = 0; i < maxVal; i++)
		{
			assertEquals(String.valueOf(i), cache.get(i));
			cache.remove(i);
		}

		assertEquals(0, cache.size());
		assertEquals(capacity, cache.capacity());
	}

	@Test
	public void testGetPutRemoveExceedCapacity()
	{
		int maxVal = 100;
		int capacity = maxVal / 2;

		cache.setCapacity(capacity);

		assertEquals(capacity, cache.capacity());
		assertEquals(0, cache.size());

		// put some data in there
		for (int i = 0; i < maxVal; i++)
		{
			cache.put(i, String.valueOf(i));
		}

		assertEquals(capacity, cache.capacity());
		assertEquals(capacity, cache.size());

		for (Integer key : cache.keySet())
		{
			System.out.println("key = " + key);
		}

		// get the data and remove it - with LRU not really sure which ones
		// remain so just check size, but this is FIFO and it still crap
		for (int i = 0; i < maxVal; i++)
		{
			String value = cache.get(i);
			if (null != value)
			{
				cache.remove(i);
			}

			// // this works because capcity is exactly half of maxVal
			// String value = cache.get(i);
			// if (i < capacity)
			// {
			// assertNull("entry, " + i +
			// ", should have been removed because capacity exceeded",
			// value);
			// }
			// else
			// {
			// assertEquals(String.valueOf(i), value);
			// cache.remove(i);
			// }
		}

		assertEquals(0, cache.size());
		assertEquals(capacity, cache.capacity());
	}

	@Test
	public void testCapacityAndReplacingEntries()
	{
		int maxVal = 100;
		int capacity = maxVal / 2;

		cache.setCapacity(capacity);

		// put some data in there
		for (int i = 0; i < maxVal; i++)
		{
			cache.put(i, String.valueOf(i));
		}

		assertEquals(capacity, cache.capacity());
		assertEquals(capacity, cache.size());

		capacity = maxVal * 2;
		cache.setCapacity(capacity);
		assertEquals(capacity, cache.capacity());

		// put some more data in there
		for (int i = 0; i < maxVal; i++)
		{
			cache.put(i, String.valueOf(i));
		}

		// get the data and remove it
		for (int i = 0; i < maxVal; i++)
		{
			String value = cache.get(i);

			assertEquals(String.valueOf(i), value);
			cache.remove(i);
		}

		assertEquals(capacity, cache.capacity());
		assertEquals(0, cache.size());
	}

	@Test
	public void testRemoveEntriesNotPresent()
	{
		int maxVal = 100;
		int capacity = 200;

		cache.setCapacity(capacity);

		// put some data in there
		for (int i = 0; i < maxVal; i++)
		{
			cache.put(i, String.valueOf(i));
		}

		assertEquals(capacity, cache.capacity());
		assertEquals(maxVal, cache.size());

		// put some data in there
		for (int i = 0; i < maxVal; i++)
		{
			cache.put(i, String.valueOf(i));
		}

		cache.remove(maxVal + 1);
		cache.remove(maxVal + 2);
		cache.remove(maxVal + 3);

		// get the data and remove it
		for (int i = 0; i < maxVal / 2; i++)
		{
			String value = cache.get(i);

			assertEquals(String.valueOf(i), value);
			cache.remove(i);
		}

		assertEquals(capacity, cache.capacity());
		assertEquals(maxVal / 2, cache.size());
	}

	// ---------------

	@BeforeClass
	public static void createCache()
	{
		cache = LinkedHashMapCache.createInstance("test", "cf-test", 100);
	}

	@Before
	public void clearCache()
	{
		cache.clear();
	}

//	@AfterClass
//	public static void shutdownCache()
//	{
//		ConcurrentSkipListCache.shutdown();
//	}
}
