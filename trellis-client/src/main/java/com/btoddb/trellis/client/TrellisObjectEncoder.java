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
