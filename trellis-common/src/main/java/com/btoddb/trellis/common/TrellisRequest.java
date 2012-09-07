
package com.btoddb.trellis.common;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

public class TrellisRequest extends TrellisReqRespBaseImpl
{
	private boolean instrument;
	private String actorName;
	private String keyspaceName;
	private List<ByteBuffer> keyList;
	private int forwardCount;
	private Object data;
	
	// don't serialize these
	private SocketAddress remoteAddress;

	public TrellisRequest(long sessionId, String actorName, Object data, boolean instrument)
	{
		super(sessionId);
		this.actorName = actorName;
		this.data = data;
		this.instrument = instrument;
	}

	public TrellisRequest()
	{
		super();
	}

	public String getActorName()
	{
		return actorName;
	}

	public Object getData()
	{
		return data;
	}

	public boolean isInstrument()
	{
		return instrument;
	}

	public ByteBuffer getKey()
	{
		if (null != keyList && 1 == keyList.size())
		{
			return keyList.iterator().next();
		}
		else if ( null == keyList ) {
			return null;
		}
		else
		{
			throw new TrellisException("You asked for a single key, but this class, "
					+ getClass().getSimpleName() + ", has " + (null != keyList ? keyList.size() : "null")
					+ " keys - cannot continue");
		}		
	}

	public List<ByteBuffer> getKeyList()
	{
		return keyList;
	}

	public void setKeyList(List<ByteBuffer> keyList)
	{
		this.keyList = keyList;
	}

	public void setInstrument(boolean instrument)
	{
		this.instrument = instrument;
	}

	public void setActorName(String actorName)
	{
		this.actorName = actorName;
	}

	public void setData(Object data)
	{
		this.data = data;
	}

	public String getKeyspaceName()
	{
		return keyspaceName;
	}

	public void setKeyspaceName(String keyspaceName)
	{
		this.keyspaceName = keyspaceName;
	}

	public int getForwardCount()
	{
		return forwardCount;
	}

	public void incrementForwardCount()
	{
		forwardCount++;
	}

	public void setForwardCount(int forwardCount)
	{
		this.forwardCount = forwardCount;
	}

	@Override
	public String toString()
	{
		return "GridRequest [actorName=" + actorName + ", keyspaceName=" + keyspaceName
				+ ", forwardCount=" + forwardCount + "]";
	}

	public SocketAddress getRemoteAddress()
	{
		return remoteAddress;
	}

	public void setRemoteAddress(SocketAddress remoteAddress)
	{
		this.remoteAddress = remoteAddress;
	}
}
