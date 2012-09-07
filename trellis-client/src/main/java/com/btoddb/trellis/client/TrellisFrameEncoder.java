
package com.btoddb.trellis.client;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

public class TrellisFrameEncoder extends LengthFieldPrepender implements ChannelHandler
{

	public TrellisFrameEncoder()
	{
		// means: length=4 bytes
		super(4);
	}

}
