
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.TrellisStatsJmxBean;
import com.btoddb.trellis.common.TransportSession;

public class NettyConnectionManager implements ConnectionMgr
{
	private ClientBootstrap bootstrap;
	private ChannelFactory factory;
	private final Object connectionMonitor = new Object();
	private final ChannelGroup allChannels = new DefaultChannelGroup("grid-client");
	private NettyClientHandler clientHandler;
	private TrellisObjectEncoder objEncoder;
	private TrellisObjectDecoder objDecoder;
	private TrellisStatsJmxBean jmxBean;

	private Map<String, Channel> connMap = new HashMap<String, Channel>();

	public void init()
	{
		factory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(new ThreadFactory()
					{
						ThreadFactory defThreadFactory = Executors.defaultThreadFactory();

						@Override
						public Thread newThread(Runnable theObj)
						{
							Thread theThread = defThreadFactory.newThread(theObj);
							theThread.setName("Grid-Netty-Client-Boss-" + theThread.getName());
							return theThread;
						}
					}), Executors.newCachedThreadPool(new ThreadFactory()
					{
						ThreadFactory defThreadFactory = Executors.defaultThreadFactory();

						@Override
						public Thread newThread(Runnable theObj)
						{
							Thread theThread = defThreadFactory.newThread(theObj);
							theThread.setName("Grid-Netty-Client-Worker-" + theThread.getName());
							return theThread;
						}
					}));

		bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
			{
				public ChannelPipeline getPipeline()
				{
					return Channels.pipeline(new TrellisFrameEncoder(), new TrellisFrameDecoder(),
							objEncoder, objDecoder, clientHandler);
				}
			});

		// TODO:BTB:may need to set these on "child"
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
	}

	/**
	 * @see com.btoddb.trellis.client.ConnectionMgr#getConnection(java.lang.String,
	 *      int)
	 */
	@Override
	public TransportSession getConnection(String hostName, int port)
	{
		String ip;
		try
		{
			ip = InetAddress.getByName(hostName).getHostAddress();
		}
		catch (UnknownHostException e)
		{
			throw new TrellisException("could not obtain IP address from host name, " + hostName, e);
		}

		Channel channel = null;
		synchronized (connectionMonitor)
		{
			String connStr = ip + ":" + port;
			channel = connMap.get(connStr);
			// TODO:BTB:may should make these an array of channels per host
			if (null == channel || !channel.isConnected())
			{
				ChannelFuture future = bootstrap.connect(new InetSocketAddress(hostName, port));
				future.awaitUninterruptibly();
				if (future.isSuccess())
				{
					channel = future.getChannel();
					allChannels.add(channel);
				}
				else
				{
					throw new TrellisException("could not create connection to " + connStr + " : " + future.getCause().getMessage());
				}

				connMap.put(connStr, channel);
			}
		}

		return new NettyTransportSession(channel, new DurationCallback()
			{
				@Override
				public void totalDuration(TransportSession transSess, long durationInNanos)
				{
					if ( null != jmxBean ) {
						jmxBean.addRollingSample("client-netty-write-duration", durationInNanos/1000);
					}
				}
			});
	}

	/**
	 * @see com.btoddb.trellis.client.ConnectionMgr#shutdown()
	 */
	@Override
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

	public void setClientHandler(NettyClientHandler clientHandler)
	{
		this.clientHandler = clientHandler;
	}
	
	public void setJmxBean(TrellisStatsJmxBean jmxBean)
	{
		this.jmxBean = jmxBean;
	}

}
