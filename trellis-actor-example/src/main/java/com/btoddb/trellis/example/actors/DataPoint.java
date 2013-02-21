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

package com.btoddb.trellis.example.actors;

public class DataPoint
{
	private final int colName;
	private final int colValue;
	
	public DataPoint(int colName, int colValue)
	{
		super();
		this.colName = colName;
		this.colValue = colValue;
	}

	public int getColName()
	{
		return colName;
	}

	public int getColValue()
	{
		return colValue;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + colName;
		result = prime * result + colValue;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataPoint other = (DataPoint) obj;
		if (colName != other.colName)
			return false;
		if (colValue != other.colValue)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "DataPoint [colName=" + colName + ", colValue=" + colValue + "]";
	}
	
	
}
