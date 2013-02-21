
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
