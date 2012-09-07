
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