
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

package com.btoddb.trellis.server.netty;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.client.DurationCallback;
import com.btoddb.trellis.client.NettyTransportSession;
import com.btoddb.trellis.client.TrellisFrameDecoder;
import com.btoddb.trellis.client.TrellisFrameEncoder;
import com.btoddb.trellis.client.TrellisObjectDecoder;
import com.btoddb.trellis.client.TrellisObjectEncoder;
import com.btoddb.trellis.common.TrellisStatsJmxBean;
import com.btoddb.trellis.common.TrellisRequest;
import com.btoddb.trellis.common.TrellisSession;
import com.btoddb.trellis.common.TransportSession;
import com.btoddb.trellis.server.TrellisSessionIdGenerator;

public class NettyServer
{
	private final ChannelGroup allChannels = new DefaultChannelGroup("trellis-server");
	private final DurationCallback writeDurationCallback = new WriteDurationCallback();
	
	private TrellisObjectEncoder objEncoder;
	private TrellisObjectDecoder objDecoder;
	private Queue<TrellisSession> receiveQueue;
	private TrellisSessionIdGenerator sessIdGen;
	private TrellisStatsJmxBean jmxBean;

	private ChannelFactory factory;

	public void start(int port)
	{
		factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(new ThreadFactory()
					{
						ThreadFactory defThreadFactory = Executors.defaultThreadFactory();

						@Override
						public Thread newThread(Runnable theObj)
						{
							Thread theThread = defThreadFactory.newThread(theObj);
							theThread.setName("Trellis-Netty-Server-Boss-" + theThread.getName());
							return theThread;
						}
					}), Executors.newCachedThreadPool(new ThreadFactory()
					{
						ThreadFactory defThreadFactory = Executors.defaultThreadFactory();

						@Override
						public Thread newThread(Runnable theObj)
						{
							Thread theThread = defThreadFactory.newThread(theObj);
							theThread.setName("Trellis-Netty-Server-Worker-" + theThread.getName());
							return theThread;
						}
					}));

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
			{
				public ChannelPipeline getPipeline()
				{
					return Channels.pipeline(new TrellisFrameEncoder(), new TrellisFrameDecoder(),
							objEncoder, objDecoder, new NettyServerHandler());
				}
			});

		bootstrap.setOption("reuseAddress", true);

		// child means apply to the "accepted" channel, not the server socket
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		Channel channel = bootstrap.bind(new InetSocketAddress(port));

		allChannels.add(channel);
	}

	public void shutdown()
	{
		ChannelGroupFuture future = allChannels.close();
		future.awaitUninterruptibly();
		factory.releaseExternalResources();
	}

	public void setObjEncoder(TrellisObjectEncoder objEncoder)
	{
		this.objEncoder = objEncoder;
	}

	public void setObjDecoder(TrellisObjectDecoder objDecoder)
	{
		this.objDecoder = objDecoder;
	}

	public void setReceiveQueue(Queue<TrellisSession> receiveQueue)
	{
		this.receiveQueue = receiveQueue;
	}

	public void setSessIdGen(TrellisSessionIdGenerator sessIdGen)
	{
		this.sessIdGen = sessIdGen;
	}

	public void setJmxBean(TrellisStatsJmxBean jmxBean)
	{
		this.jmxBean = jmxBean;
	}

	// ----------------------------

	class WriteDurationCallback implements DurationCallback
	{
		@Override
		public void totalDuration(TransportSession transSession, long durationInNanos)
		{
			jmxBean.addRollingSample("server-netty-write-latency", durationInNanos / 1000);
		}
	}

	class NettyServerHandler extends SimpleChannelHandler
	{
		private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

		@Override
		public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
		{
			allChannels.add(e.getChannel());
		}

		// @Override
		// public void channelConnected(ChannelHandlerContext ctx,
		// ChannelStateEvent
		// e)
		// {
		// Channel ch = e.getChannel();
		//
		// ChannelFuture f = ch.write(new Date());
		//
		// f.addListener(new ChannelFutureListener()
		// {
		// public void operationComplete(ChannelFuture future)
		// {
		// Channel ch = future.getChannel();
		// ch.close();
		// }
		// });
		// }

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		{
			NettyTransportSession transSess = new NettyTransportSession(e.getChannel(), writeDurationCallback);

			TrellisRequest req = (TrellisRequest) e.getMessage();

			TrellisSession gridSession = new TrellisSession(transSess, sessIdGen.getNextId());
			gridSession.setRequest(req);

			transSess.setGridSession(gridSession);

			receiveQueue.offer(gridSession);

		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		{
			logger.error( "exception performing Netty I/O", e.getCause());
			e.getChannel().close();
		}
	}
}