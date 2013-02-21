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

package com.btoddb.trellis.caching;

import java.util.Arrays;


public class CacheKey
{
	private final String keyspaceName;
	private final String columnFamilyName;
	private final byte[] arr;

	public CacheKey(String keyspaceName, String columnFamilyName, byte[] arr)
	{
		this.keyspaceName = keyspaceName;
		this.columnFamilyName = columnFamilyName;
		this.arr = arr;
	}

	public byte[] getArr()
	{
		return arr;
	}

	public String getKeyspaceName()
	{
		return keyspaceName;
	}

	public String getColumnFamilyName()
	{
		return columnFamilyName;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arr);
		result = prime * result + ((columnFamilyName == null) ? 0 : columnFamilyName.hashCode());
		result = prime * result + ((keyspaceName == null) ? 0 : keyspaceName.hashCode());
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
		CacheKey other = (CacheKey) obj;
		if (!Arrays.equals(arr, other.arr))
			return false;
		if (columnFamilyName == null)
		{
			if (other.columnFamilyName != null)
				return false;
		}
		else if (!columnFamilyName.equals(other.columnFamilyName))
			return false;
		if (keyspaceName == null)
		{
			if (other.keyspaceName != null)
				return false;
		}
		else if (!keyspaceName.equals(other.keyspaceName))
			return false;
		return true;
	}

}
