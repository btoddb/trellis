
package com.btoddb.trellis.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Utils
{

	/**
	 * Will use the ByteBuffer's underlying array if it has one, and the
	 * position, length, capacity, and limit are appropriate, otherwise it will
	 * make a copy of the sub-arry being used by the ByteBuffer's current
	 * position and limit.
	 * 
	 * @param bb
	 * @return
	 */
	public static byte[] safeByteBufferArrayAccess(ByteBuffer bb)
	{
		if (bb.hasArray() && 0 == bb.position() && bb.position() == bb.arrayOffset()
				&& bb.capacity() == bb.limit())
		{
			return bb.array();
		}
		else
		{
			return Arrays.copyOfRange(bb.array(), bb.arrayOffset() + bb.position(),
					bb.arrayOffset() + bb.limit());
		}

	}

}
