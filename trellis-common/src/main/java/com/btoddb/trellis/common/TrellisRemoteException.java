
package com.btoddb.trellis.common;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TrellisRemoteException
{
	private String className;
	private String msg;
	private String stackTrace;

	public TrellisRemoteException()
	{}

	public TrellisRemoteException(Throwable e)
	{
		this.className = e.getClass().getName();
		this.msg = e.getMessage();
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
	}

	public String getClassName()
	{
		return className;
	}

	public void setClassName(String className)
	{
		this.className = className;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public String getStackTrace()
	{
		return stackTrace;
	}

	public void setStackTrace(String stackTrace)
	{
		this.stackTrace = stackTrace;
	}

	@Override
	public String toString()
	{
		return "GridRemoteException [className=" + className + ", msg=" + msg + "]\n" + stackTrace;
	}

}
