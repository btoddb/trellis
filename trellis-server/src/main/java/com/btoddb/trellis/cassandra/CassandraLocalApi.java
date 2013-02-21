
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

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.StopWatchInNanos;
import com.btoddb.trellis.common.TrellisException;

/**
 * Abstraction above the Cassandra API to create a buffer around the Cassandra
 * technology in case of change.
 * 
 */
@Service("cassandraApi")
public class CassandraLocalApi implements KeyLocatorService, NodeManagement
{
	private static final Logger logger = LoggerFactory.getLogger(CassandraLocalApi.class);

	/**
	 * @see com.btoddb.trellis.cassandra.KeyLocatorService#getNodesForKey(java.lang.String,
	 *      java.nio.ByteBuffer)
	 */
	@Override
	public List<InetAddress> getNodesForKey(String keyspaceName, ByteBuffer key)
	{
		List<InetAddress> hostList = StorageService.instance.getNaturalEndpoints(keyspaceName, key);
		return hostList;
	}

	/**
	 * @see com.btoddb.trellis.cassandra.NodeManagement#isNodeReachable(java.net.InetAddress)
	 */
	@Override
	public boolean isNodeReachable(InetAddress host)
	{
		List<String> unreachableNodes = StorageService.instance.getUnreachableNodes();
		if (null == unreachableNodes || unreachableNodes.isEmpty())
		{
			return true;
		}
		else
		{
			return unreachableNodes.contains(host.getHostAddress());
		}
	}

	@Override
	public boolean isNodeLocal(InetAddress host)
	{
		InetAddress addr = FBUtilities.getLocalAddress();
		return addr.equals(host);
	}

	@Override
	public Map<String, List<ByteBuffer>> generateHostToKeysMap(String keyspaceName,
			List<ByteBuffer> keyList)
	{
		StopWatchInNanos stat = new StopWatchInNanos().start();
		Map<String, List<ByteBuffer>> hostToKeysMap = new HashMap<String, List<ByteBuffer>>();
		for (ByteBuffer key : keyList)
		{
			List<InetAddress> hostList = getNodesForKey(keyspaceName, key);

			InetAddress selectedHost = null;
			for (InetAddress host : hostList)
			{
				// ask if node is reachable
				if (isNodeReachable(host))
				{
					selectedHost = host;
					// if node is local, we prefer it, so stop looking
					if (isNodeLocal(host))
					{
						break;
					}
				}
			}

			if (null != selectedHost)
			{
				List<ByteBuffer> perHostKeyList = hostToKeysMap.get(selectedHost.getHostAddress());
				if (null == perHostKeyList)
				{
					perHostKeyList = new LinkedList<ByteBuffer>();
					hostToKeysMap.put(selectedHost.getHostAddress(), perHostKeyList);
				}
				perHostKeyList.add(key);
			}
			else
			{
				throw new TrellisException("no host available to service key, " + key);
			}
		}

		logger.debug("duration to determine hosts (from cached range map) = "
				+ stat.getDuratinInMillis() + "ms");
		return hostToKeysMap;
	}

	@Override
	public List<String> getLiveNodes()
	{
		return StorageService.instance.getLiveNodes();
	}

	@Override
	public List<String> getUnreachableNodes()
	{
		return StorageService.instance.getUnreachableNodes();
	}

}
