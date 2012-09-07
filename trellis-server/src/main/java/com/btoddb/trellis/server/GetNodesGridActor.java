
package com.btoddb.trellis.server;

import java.nio.ByteBuffer;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.btoddb.trellis.actor.TrellisActor;
import com.btoddb.trellis.common.GetNodesResult;
import com.btoddb.trellis.common.PersistentKey;
import com.btoddb.trellis.common.StatsAggregate;
import com.btoddb.trellis.common.serialization.TrellisSerializerServiceImpl;

/**
 * Actor to retrieve the Cassandra endpoint map and return to client. (Used for
 * determining which cassandra nodes can locally serve a key's data.
 * 
 */
public class GetNodesGridActor extends TrellisActor
{
	private static final Logger logger = LoggerFactory.getLogger(GetNodesGridActor.class);

	private static final String ACTOR_NAME = "get-nodes";
	
	@Autowired
	private TrellisSerializerServiceImpl serSrvc;
	
	@Resource(type=NodeDiscoveryLocalService.class)
	private NodeDiscoveryLocalService nodeDiscovery;

	public GetNodesGridActor() {
		super(ACTOR_NAME);
	}
	
	@Override
	public ByteBuffer execute(Object params, Map<PersistentKey, Object> data, StatsAggregate stats)
	{
		logger.debug(getActorName() + " : discovering nodes");
		GetNodesResult res = nodeDiscovery.execute();
		return serSrvc.serialize(res);
	}

	public void setNodeDiscovery(NodeDiscoveryLocalService nodeDiscovery)
	{
		this.nodeDiscovery = nodeDiscovery;
	}

	public void setSerSrvc(TrellisSerializerServiceImpl serSrvc)
	{
		this.serSrvc = serSrvc;
	}

}
