
package com.btoddb.trellis.classloader;

import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.TrellisException;

@Service("actorClassLoader")
public class ActorClassLoaderImpl extends ClassLoader
{
//	private static final Logger logger = LoggerFactory.getLogger(ActorClassLoaderImpl.class);


	@Override
	public Class<?> loadClass(String name)
	{
		try
		{
			return super.loadClass(name);
		}
		catch (ClassNotFoundException e)
		{
			throw new TrellisException("exception while loading new class in grid", e);
		}
	}
}
