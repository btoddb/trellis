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

package com.btoddb.trellis.common;

import java.util.List;

public class GetNodesResult {
	private List<String> liveHosts;
	private List<String> unreachableHosts;

	public GetNodesResult() {}

	public GetNodesResult(List<String> liveHosts, List<String> unreacableHosts)
	{
		this.liveHosts = liveHosts;
		this.unreachableHosts = unreacableHosts;
	}
	
	public List<String> getUnreachableHosts()
	{
		return unreachableHosts;
	}

	public List<String> getLiveHosts()
	{
		return liveHosts;
	}

	public void setLiveHosts(List<String> liveHosts)
	{
		this.liveHosts = liveHosts;
	}

	public void setUnreachableHosts(List<String> unreachableHosts)
	{
		this.unreachableHosts = unreachableHosts;
	}

}
