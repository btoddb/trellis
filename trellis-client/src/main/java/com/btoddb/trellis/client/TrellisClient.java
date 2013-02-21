
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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.InstrumentationAggregator;
import com.btoddb.trellis.common.StopWatchInNanos;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.TrellisRequest;
import com.btoddb.trellis.common.TrellisSession;

@Service("gridClient")
public class TrellisClient
{
	// private static final Logger logger =
	// LoggerFactory.getLogger(GridClient.class);
	private static final long RESPONSE_MAX_WAIT = 10000;

	private long responseMaxWait = RESPONSE_MAX_WAIT;

	@Autowired
	private TrellisCluster trellisCluster;

	public TrellisClient()
	{}

	/**
	 * Send request to each host in parallel and wait for responses, or until timeout exceeded.
	 * 
	 * @param actorName
	 * @param hostToKeysMap
	 * @param data
	 * @param instrument
	 * @return
	 */
	public Map<ByteBuffer, Object> sendRequestToGrid(String actorName, String keyspaceName, List<ByteBuffer> keyList,
			Object data, boolean instrument)
	{
		// clock to time duration to send/receive all requests
		StopWatchInNanos sw = new StopWatchInNanos().start();

		// this is an asynchronous call
		TrellisSession gridSession = sendRequestToGridAsync(actorName, keyspaceName, keyList, data, instrument);

		Map<ByteBuffer, Object> resp = waitForResponse(gridSession);
		InstrumentationAggregator.addSampleInMicros("client-total-duration", sw.getDuratinInMicros());
		return resp;
	}

	public TrellisSession sendRequestToGridAsync(String actorName, String keyspaceName, List<ByteBuffer> keyList,
			Object data, boolean instrument)
	{
		TrellisRequest req = new TrellisRequest(-1, actorName, data, instrument);
		req.setKeyspaceName(keyspaceName);
		req.setKeyList(keyList);
		return trellisCluster.sendRequestToGridAsync(req);
	}

	private Map<ByteBuffer, Object> processCompletedSession(TrellisSession gridSession)
	{
		if (gridSession.isResponseReady())
		{
			InstrumentationAggregator.addSampleInMicros("client-round-trip-duration",
					(System.nanoTime() - gridSession.getRequestSentTimeInNanos()) / 1000);
			InstrumentationAggregator.addSampleInMicros("server-resp-ser", gridSession.getResponse()
					.getSerializationDuration());
			InstrumentationAggregator.saveResults(gridSession.getInstrumentation().getMap());
			return gridSession.getResponse().getData();
		}
		else
		{
			throw new TrellisException("timeout of " + responseMaxWait
					+ "ms was exceeded while waiting for response from "
					+ gridSession.getTransportSession().getHostName());
		}
	}

	private Map<ByteBuffer, Object> waitForResponse(TrellisSession gridSession)
	{
		return waitForResponse(gridSession, System.currentTimeMillis());
	}

	private Map<ByteBuffer, Object> waitForResponse(TrellisSession gridSession, long startTime)
	{
		trellisCluster.waitForResponse(gridSession, startTime, responseMaxWait);
		return processCompletedSession(gridSession);
	}

	public void setTrellisCluster(TrellisCluster trellisCluster)
	{
		this.trellisCluster = trellisCluster;
	}
}
