
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

import java.util.Queue;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.common.StopWatchInNanos;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.TrellisResponse;
import com.btoddb.trellis.common.TrellisSession;

public class NettyClientHandler extends SimpleChannelHandler
{
	private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

	private Queue<TrellisSession> finalizeQueue;
	private TrellisSessionMgr gridSessionMgr;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	{
		StopWatchInNanos sw = new StopWatchInNanos().start();

		TrellisResponse resp = (TrellisResponse) e.getMessage();
		logger.debug("response received on client : client session ID = {}", resp.getSessionId());
		TrellisSession gridSession = gridSessionMgr.getAndRemove(resp.getSessionId());
		if (null == gridSession)
		{
			throw new TrellisException("This response's session ID, " + resp.getSessionId()
					+ ", does not match to an active session");
		}
		else
		{
			logger.debug("client removed session ID = {}", resp.getSessionId());
		}

		gridSession.setResponseReceivedTimeInNanos(sw.getDurationInNanos());
		gridSession.addInstrumentation(resp.getInstrumentation());
		gridSession.setResponse(resp);

		// if this request part of a session group, then post to finalizer queue
		if (gridSession.isPartOfSessionGroup())
		{
			finalizeQueue.add(gridSession);
		}

		synchronized (gridSession.getParent())
		{
			gridSession.getParent().notifyAll();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
	{
		logger.error("exception while processing request/response", e.getCause());
	}

	public void setFinalizeQueue(Queue<TrellisSession> finalizeQueue)
	{
		this.finalizeQueue = finalizeQueue;
	}

	public void setGridSessionMgr(TrellisSessionMgr gridSessionMgr)
	{
		this.gridSessionMgr = gridSessionMgr;
	}

}
