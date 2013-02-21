
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
