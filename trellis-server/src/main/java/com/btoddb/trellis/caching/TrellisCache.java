
package com.btoddb.trellis.caching;

import java.net.URL;

import javax.management.MBeanServer;

import com.btoddb.trellis.common.TrellisException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.management.ManagementService;

public class TrellisCache implements TrellisCacheProvider
{
//	private static Logger logger = LoggerFactory.getLogger(TrellisCache.class);

	private static final String TRELLIS_CACHE = "trellis";

	// private static final ByteBuffer EMPTY_BYTE_BUFFER =
	// ByteBuffer.allocate(0);

	private CacheManager cacheMgr;
	private Cache ehc;

	private String confFile;
	private MBeanServer mBeanServer;

	public TrellisCache()
	{}

	/**
	 * @see com.btoddb.trellis.caching.TrellisCacheProvider#start()
	 */
	@Override
	public void start()
	{
		if (null == confFile)
		{
			confFile = findConfInClassPath();
		}
		
		if ( null == confFile) {
			throw new TrellisException( "ehcache.xml not specified and cannot find it in same package of this class, nor root of classpath");
		}
		cacheMgr = new CacheManager(confFile);
		cacheMgr.setName("Trellis-EhcacheManager");

		if (null != mBeanServer)
		{
			ManagementService.registerMBeans(cacheMgr, mBeanServer, true, true, true, true);
		}

		ehc = cacheMgr.getCache(TRELLIS_CACHE);
	}

	private String findConfInClassPath()
	{
		// InputStream is = getClass().getResourceAsStream("ehcache.xml");
		// if ( null == is ) {
		// return null;
		// }
		//
		URL url;
		url = getClass().getResource("ehcache.xml");
		if (null == url)
		{
			url = getClass().getResource("/ehcache.xml");
		}
		return null != url ? url.getFile() : null;
	}

	/**
	 * @see com.btoddb.trellis.caching.TrellisCacheProvider#get(byte[])
	 */
	@Override
	public Object get(String keyspaceName, String columnFamilyName, byte[] key)
	{
		return get(new CacheKey(keyspaceName, columnFamilyName, key));
	}
	
	@Override
	public Object get(CacheKey key)
	{
		Element e = ehc.get(key);
		if (null != e)
		{
			return e.getObjectValue();
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see com.btoddb.trellis.caching.TrellisCacheProvider#put(byte[],
	 *      java.lang.Object)
	 */
	@Override
	public void put(String keyspaceName, String columnFamilyName, byte[] key, Object value)
	{
		put(new CacheKey(keyspaceName, columnFamilyName, key), value);
	}

	/**
	 * @see com.btoddb.trellis.caching.TrellisCacheProvider#put(com.btoddb.trellis.caching.CacheKey,
	 *      java.lang.Object, boolean)
	 */
	@Override
	public void put(CacheKey key, Object value)
	{
		ehc.put(new Element(key, value));
	}

	/**
	 * @see com.btoddb.trellis.caching.TrellisCacheProvider#lock(byte[])
	 */
	@Override
	public TrellisCacheLock lock(String keyspaceName, String columnFamilyName, byte[] key, boolean block, long lockTimeout)
	{
		return lock( new CacheKey(keyspaceName, columnFamilyName, key), block, lockTimeout);
	}
	@Override
	public TrellisCacheLock lock(CacheKey key, boolean block, long lockTimeout)
	{
		// TODO:BTB - obviously not doing anything!!!
		return new TrellisCacheLock(key);
	}
	
	/**
	 * @see com.btoddb.trellis.caching.TrellisCacheProvider#unlock(com.btoddb.trellis.caching.TrellisCacheLock)
	 */
	@Override
	public void unlock(TrellisCacheLock lock)
	{

	}

	/**
	 * @see com.btoddb.trellis.caching.TrellisCacheProvider#shutdown()
	 */
	@Override
	public void shutdown()
	{
		if (null != cacheMgr)
		{
			cacheMgr.shutdown();
			cacheMgr = null;
		}
	}

	public void setmBeanServer(MBeanServer mBeanServer)
	{
		this.mBeanServer = mBeanServer;
	}

	public void setConfFile(String confFile)
	{
		this.confFile = confFile;
	}
}
