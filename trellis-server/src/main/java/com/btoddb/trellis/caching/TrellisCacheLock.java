package com.btoddb.trellis.caching;


public class TrellisCacheLock
{

	private final CacheKey key;
	private final long lockTime;
	
	public TrellisCacheLock(CacheKey key) {
		this.key = key;
		this.lockTime = System.currentTimeMillis();
	}

	public CacheKey getKey()
	{
		return key;
	}

	public long getLockTime()
	{
		return lockTime;
	}
}
