
package com.btoddb.trellis.cassandra;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface NodeManagement
{

	boolean isNodeReachable(InetAddress host);

	boolean isNodeLocal(InetAddress host);

	Map<String, List<ByteBuffer>> generateHostToKeysMap(String keyspaceName,
			List<ByteBuffer> keyList);

	List<String> getLiveNodes();
	
	List<String> getUnreachableNodes();
}