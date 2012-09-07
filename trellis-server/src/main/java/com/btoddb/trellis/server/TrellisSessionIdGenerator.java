package com.btoddb.trellis.server;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service("gridSessionIdGenerator")
public class TrellisSessionIdGenerator
{

	private final AtomicLong gridSessIdGen = new AtomicLong();

	public long getNextId() {
		return gridSessIdGen.incrementAndGet();
	}
}
