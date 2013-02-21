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

package com.btoddb.trellis.server;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.cassandra.NodeManagement;
import com.btoddb.trellis.client.NodeDiscoveryService;
import com.btoddb.trellis.common.GetNodesResult;

public class NodeDiscoveryLocalService implements NodeDiscoveryService
{
	private static final Logger logger = LoggerFactory.getLogger(NodeDiscoveryLocalService.class);
	
	private NodeManagement nodeMgmt;

	/** 
	 * @see com.btoddb.trellis.client.NodeDiscoveryService#execute()
	 */
	@Override
	public GetNodesResult execute() {
		logger.debug( "using nodeMgmt to discover nodes");
		List<String> liveList = nodeMgmt.getLiveNodes();
		List<String> unreachableList = nodeMgmt.getUnreachableNodes();
		
		logger.debug( "discovery results : live = " + liveList.size() + ", unreachable = " + unreachableList.size());
		return new GetNodesResult(liveList, unreachableList);
	}

	public void setNodeMgmt(NodeManagement nodeMgmt)
	{
		this.nodeMgmt = nodeMgmt;
	}

}
