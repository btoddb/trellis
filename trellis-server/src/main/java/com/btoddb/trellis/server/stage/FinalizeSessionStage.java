
package com.btoddb.trellis.server.stage;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.btoddb.trellis.common.TrellisResponse;
import com.btoddb.trellis.common.TrellisSession;

@ManagedResource(objectName = "trellis-server:type=stages,name=session-finalizer", description = "Finalize grid sessions")
public class FinalizeSessionStage extends StageController
{
	private static final Logger logger = LoggerFactory.getLogger(FinalizeSessionStage.class);
	private BlockingQueue<TrellisSession> outQueue;

	@Override
	protected void process(TrellisSession gridSession)
	{
		TrellisSession parentSess = gridSession.getParent();
		if (null == parentSess)
		{
			outQueue.offer(gridSession);
		}
		else
		{
			if (gridSession.isRemoteCall())
			{
				parentSess.addRemoteDurationSample(gridSession.getResponseReceivedTimeInNanos()
						- gridSession.getRequestQueuedTimeInNanos());
			}
			else
			{
				parentSess.addLocalDurationSample(gridSession.getResponseReceivedTimeInNanos()
						- gridSession.getRequestQueuedTimeInNanos());
			}

			// when do this move, all processing of session should be complete
			parentSess.moveSessionToProcessed(gridSession);
			// if (parentSess.isFinishedMovingSessions())
			// {
			// synchronized (parentSess)
			// {
			// if (!parentSess.isFinishedProcessing())
			// {
			// createSessionGroupResponse(parentSess);
			// outQueue.add(parentSess);
			// parentSess.setFinishedProcessing(true);
			// }
			// }
			// }

			if (parentSess.isFinishedMovingSessions() && !parentSess.isFinishedProcessing()
					&& parentSess.lockForFinalizing())
			{
//				if (!parentSess.isFinishedProcessing())
//				{
					createSessionGroupResponse(parentSess);
					outQueue.add(parentSess);
					parentSess.setFinishedProcessing(true);
//				}
			}

		}
	}

	private TrellisResponse createSessionGroupResponse(TrellisSession parentSession)
	{
		long clientSessionId = parentSession.getClientSessionId();

		logger.debug("finalizing client, " + parentSession.getRequest().getRemoteAddress()
				+ ",  session = {}", clientSessionId);

		Map<ByteBuffer, Object> retMap = new HashMap<ByteBuffer, Object>();
		Map<Long, TrellisSession> processedMap = parentSession.getProcessedMap();
		for (TrellisSession gridSession : processedMap.values())
		{
			TrellisResponse resp = gridSession.getResponse();
			for (Entry<ByteBuffer, Object> entry : ((Map<ByteBuffer, Object>) resp.getData())
					.entrySet())
			{
				retMap.put(entry.getKey(), entry.getValue());
			}
		}

		TrellisResponse gridResponse = new TrellisResponse(clientSessionId, retMap);
		parentSession.setResponse(gridResponse);
		return gridResponse;
	}

	public void setOutQueue(BlockingQueue<TrellisSession> outQueue)
	{
		this.outQueue = outQueue;
	}

}
