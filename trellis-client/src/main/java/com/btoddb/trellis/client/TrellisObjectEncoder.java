package com.btoddb.trellis.client;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.btoddb.trellis.common.serialization.TrellisSerializerServiceImpl;

public class TrellisObjectEncoder extends OneToOneEncoder implements ChannelHandler
{
	private TrellisSerializerServiceImpl serSrvc;

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
			throws Exception
	{
		return ChannelBuffers.wrappedBuffer(serSrvc.serialize(msg));
	}

	public void setSerSrvc(TrellisSerializerServiceImpl serSrvc)
	{
		this.serSrvc = serSrvc;
	}

}
