
package com.btoddb.trellis.server.stage;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.btoddb.trellis.common.Instrumentation;
import com.btoddb.trellis.common.Stat;
import com.btoddb.trellis.common.StatsAggregate;
import com.btoddb.trellis.common.TrellisRequest;
import com.btoddb.trellis.common.TrellisResponse;
import com.btoddb.trellis.common.TrellisSession;

@ManagedResource(objectName = "trellis-server:type=stages,name=send-response", description = "Send response to client")
public class SendResponseStage extends StageController
{
	private static final Logger logger = LoggerFactory.getLogger(SendResponseStage.class);

	@Override
	protected void process(TrellisSession gridSession)
	{
		logger.debug("sending response from server : session ID = {}", gridSession.getResponse()
				.getSessionId());

		TrellisResponse resp = gridSession.getResponse();
		TrellisRequest req = gridSession.getRequest();
		if (req.isInstrument())
		{
			Instrumentation instr = new Instrumentation();
			instr.addDuration("server-" + Instrumentation.REQ_DESER, req.getDeserializeDuration());

			instr.addDuration("server-remote-call-avg", gridSession.getRemoteDurationAvgInMicros());
			instr.addDuration("server-remote-call-max", gridSession.getRemoteDurationMaxInMicros());
			instr.addDuration("server-remote-call-min", gridSession.getRemoteDurationMinInMicros());

			StatsAggregate stats = gridSession.getStats();
			for (Entry<String, Stat> entry : stats.getStatsMap().entrySet())
			{
				Stat st = entry.getValue();
				instr.addDuration("server-" + entry.getKey(), st.getAverageSample());
			}
			instr.addDuration("server-total-time", (System.nanoTime() - gridSession.getRequest()
					.getArrivalTime()) / 1000);
			resp.setInstrumentation(instr);
		}

		// for stats
		resp.setGridSession(gridSession);

		gridSession.setResponseQueuedTimeInNanos(System.nanoTime());
		gridSession.getTransportSession().write(resp);
	}

}
