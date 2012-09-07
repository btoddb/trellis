
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
