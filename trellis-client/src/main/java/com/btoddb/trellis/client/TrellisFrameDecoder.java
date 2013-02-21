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

package com.btoddb.trellis.client;

import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

public class TrellisFrameDecoder extends LengthFieldBasedFrameDecoder
{
	private static final int MAX_FRAME_LENGTH = 10*1024*1024; // 10m frame size

	public TrellisFrameDecoder() {
		// means: offset=0, length=4, adjustment=0, strip=4
		super(MAX_FRAME_LENGTH, 0, 4, 0, 4);
	}
}
