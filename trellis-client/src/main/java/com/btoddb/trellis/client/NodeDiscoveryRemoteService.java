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

package com.btoddb.trellis.client;

import java.nio.ByteBuffer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.common.GetNodesResult;
import com.btoddb.trellis.common.serialization.TrellisSerializerServiceImpl;


public class NodeDiscoveryRemoteService implements NodeDiscoveryService
{
	private static final Logger logger = LoggerFactory.getLogger(NodeDiscoveryRemoteService.class);
	
	private TrellisClient gridClient;
	private TrellisSerializerServiceImpl serSrvc;

	/** 
	 * @see com.btoddb.trellis.client.NodeDiscoveryService#execute()
	 */
	@Override
	public GetNodesResult execute() {
		Map<ByteBuffer, Object> resMap = gridClient.sendRequestToGrid("get-nodes", null, null, null, true);
		if ( null == resMap || resMap.isEmpty()) {
			logger.error( "Empty set of nodes returned from cluster - NO NODES!!");
			return null;
		}
		
		return (GetNodesResult) serSrvc.deserialize((ByteBuffer)resMap.values().iterator().next());
	}

	public void setGridClient(TrellisClient gridClient)
	{
		this.gridClient = gridClient;
	}

	public void setSerSrvc(TrellisSerializerServiceImpl serSrvc)
	{
		this.serSrvc = serSrvc;
	}

}
