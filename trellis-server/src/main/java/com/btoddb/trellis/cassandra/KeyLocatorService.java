
package com.btoddb.trellis.cassandra;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;

public interface KeyLocatorService
{

	List<InetAddress> getNodesForKey(String keyspaceName, ByteBuffer key);

}