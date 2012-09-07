
package com.btoddb.trellis.client;

import com.btoddb.trellis.common.TransportSession;


public interface ConnectionMgr
{

	TransportSession getConnection(String hostName, int port);

	void shutdown();

}