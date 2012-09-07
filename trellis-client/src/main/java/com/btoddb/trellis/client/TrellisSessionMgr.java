package com.btoddb.trellis.client;

import java.util.HashMap;
import java.util.Map;

import com.btoddb.trellis.common.TrellisSession;


public class TrellisSessionMgr
{
	private Map<Long, TrellisSession> idToSessMap = new HashMap<Long, TrellisSession>(100);

	public void save(TrellisSession gridSession) {
		if ( null == gridSession) {
			return;
		}
		idToSessMap.put(gridSession.getSessionId(), gridSession);
	}
	
	public TrellisSession get(Long sessionId)
	{
		return idToSessMap.get(sessionId);
	}

	public TrellisSession getAndRemove(Long sessionId)
	{
		return idToSessMap.remove(sessionId);
	}

	public void shutdown()
	{
		idToSessMap.clear();
	}

}
