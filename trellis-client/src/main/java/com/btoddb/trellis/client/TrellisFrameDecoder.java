package com.btoddb.trellis.client;

import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

public class TrellisFrameDecoder extends LengthFieldBasedFrameDecoder
{
	private static final int MAX_FRAME_LENGTH = 10*1024*1024; // 10m frame size

	public TrellisFrameDecoder() {
		// means: offset=0, length=4, adjustment=0, strip=4
		super(MAX_FRAME_LENGTH, 0, 4, 0, 4);
	}
}
