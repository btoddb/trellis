
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

package com.btoddb.trellis.common;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;



public class TrellisResponse extends TrellisReqRespBaseImpl
{
	private int serializationDuration;
	private ByteBuffer key;
	private Map<ByteBuffer, Object> data;
	private Instrumentation instrumentation;

	// don't serialize these
	private SocketAddress remoteAddress;

	public TrellisResponse()
	{}

	public TrellisResponse(ByteBuffer key, long sessionId, Object data)
	{
		super(sessionId);
		this.key = key;
		this.data = new HashMap<ByteBuffer, Object>();
		this.data.put(this.key, data);
	}

	public TrellisResponse(long sessionId, Map<ByteBuffer, Object> data)
	{
		super(sessionId);
		this.data = data;
	}

	public Map<ByteBuffer, Object> getData()
	{
		return data;
	}

	public Instrumentation getInstrumentation()
	{
		return instrumentation;
	}

	public void setInstrumentation(Instrumentation instrumentation)
	{
		this.instrumentation = instrumentation;
	}

	public Boolean isInstrumented()
	{
		return null != this.instrumentation;
	}

	public void setData(Map<ByteBuffer, Object> data)
	{
		this.data = data;
	}

	public ByteBuffer getKey()
	{
		return key;
	}

	public void setKey(ByteBuffer key)
	{
		this.key = key;
	}

	public SocketAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress)
	{
		this.remoteAddress = remoteAddress;
	}

	public int getSerializationDuration()
	{
		return serializationDuration;
	}

	public void setSerializationDuration(int serializationDuration)
	{
		this.serializationDuration = serializationDuration;
	}
}
