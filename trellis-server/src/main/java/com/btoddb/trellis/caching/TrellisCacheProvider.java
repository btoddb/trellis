
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

package com.btoddb.trellis.caching;

/**
 * 
 * {@link #get(byte[])} and {@link #put(byte[], Object)} must be thread-safe and
 * guarantee that <code>get</code> always returns the latest <code>put</code.
 * 
 */
public interface TrellisCacheProvider
{

	void start();

	Object get(String keyspaceName, String columnFamilyName, byte[] key);
	Object get(CacheKey key);

	void put(String keyspaceName, String columnFamilyName, byte[] key, Object value);

	void put(CacheKey key, Object value);

	TrellisCacheLock lock(String keyspaceName, String columnFamilyName, byte[] key, boolean block, long lockTimeout);
	TrellisCacheLock lock(CacheKey key, boolean block, long lockTimeout);

	void unlock(TrellisCacheLock lock);

	void shutdown();

}