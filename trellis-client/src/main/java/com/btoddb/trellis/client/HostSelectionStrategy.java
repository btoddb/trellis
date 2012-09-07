
package com.btoddb.trellis.client;

import java.util.List;


public interface HostSelectionStrategy
{

	String selectNext();

	void setHostList(List<String> hostList);
	

}