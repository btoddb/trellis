
package com.btoddb.trellis.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.btoddb.trellis.common.TrellisReqResp;
import com.btoddb.trellis.common.serialization.TrellisSerializerServiceImpl;

public class TrellisObjectDecoder extends OneToOneDecoder
{
	private TrellisSerializerServiceImpl serSrvc;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg)
			throws Exception
	{
		// this is the first touch where we can set the "arrival time"
		long arriveTime = System.nanoTime();

		TrellisReqResp reqResp = (TrellisReqResp)serSrvc.deserialize(((ChannelBuffer) msg).toByteBuffer());
		reqResp.setArrivalTime(arriveTime);
		return reqResp;
	}

	public void setSerSrvc(TrellisSerializerServiceImpl serSrvc)
	{
		this.serSrvc = serSrvc;
	}
}
