
package com.btoddb.trellis.common;

@SuppressWarnings("serial")
public class TrellisException extends RuntimeException
{

	public TrellisException(String msg)
	{
		this(msg, null);
	}

	public TrellisException(String msg, Throwable e)
	{
		super(msg, e);
	}
}
