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
