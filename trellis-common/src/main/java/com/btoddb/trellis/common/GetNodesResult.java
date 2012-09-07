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
