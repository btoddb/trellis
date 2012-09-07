
package com.btoddb.trellis.client;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import com.btoddb.trellis.common.StopWatchInNanos;
import com.btoddb.trellis.common.TrellisSession;
import com.btoddb.trellis.common.TransportSession;

public class NettyTransportSession implements TransportSession, ChannelFutureListener
{
	private Channel channel;
	private TrellisSession gridSession;
	private StopWatchInNanos sw = new StopWatchInNanos();

	private DurationCallback writeDurationCallback;

	public NettyTransportSession(Channel channel, DurationCallback writeDurationCallback)
	{
		this.channel = channel;
		this.writeDurationCallback = writeDurationCallback;
	}

	@Override
	public void write(Object obj)
	{
		sw.start();
		ChannelFuture f = channel.write(obj);
		f.addListener(this);
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception
	{
		sw.stop();

		if (null != gridSession)
		{
			gridSession.setRequestSentTimeInNanos(sw.getStopTimeInNanos());
		}

		if (null != writeDurationCallback)
		{
			writeDurationCallback.totalDuration(this, sw.getDurationInNanos());
		}
	}

	@Override
	public long getWriteDurationInMicros()
	{
		return sw.getDuratinInMicros();
	}

	@Override
	public TrellisSession getGridSession()
	{
		return gridSession;
	}

	@Override
	public void setGridSession(TrellisSession gridSession)
	{
		this.gridSession = gridSession;
	}

	@Override
	public String getHostName()
	{
		return channel.getRemoteAddress().toString();
	}

}
