
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

import java.nio.ByteBuffer;
import java.util.List;

public class PersistentKey
{

	private final ByteBuffer rowKey;
	private final List<ByteBuffer> columnList;

	public PersistentKey(byte[] rowKey, List<ByteBuffer> columnList)
	{
		this.rowKey = ByteBuffer.wrap(rowKey);
		this.columnList = columnList;
	}

	public ByteBuffer getRowKey()
	{
		return rowKey;
	}

	public List<ByteBuffer> getColumnList()
	{
		return columnList;
	}

	public byte[] getRowKeyAsByteArray()
	{
		// this is guaranteed to be a valid array for returning because we wrap
		// it in constructor
		return rowKey.array();
	}

}
