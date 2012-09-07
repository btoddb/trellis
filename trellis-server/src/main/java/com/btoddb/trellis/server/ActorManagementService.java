
package com.btoddb.trellis.server;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.actor.TrellisActor;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.server.srvcloaders.ActorServiceLoader;

@Service("actorMgr")
public class ActorManagementService
{
	// private static final Logger logger = LoggerFactory.getLogger(ActorManagementService.class);

	@Autowired
	private ActorServiceLoader actorServiceLoader;
	@Resource(name = "getNodesActor")
	private TrellisActor getNodesActor;

	private Map<String, ActorDescriptor> internalActorMap = new HashMap<String, ActorDescriptor>();

	public ActorManagementService()
	{}

	@PostConstruct
	public void init()
	{
		internalActorMap.put(getNodesActor.getActorName(), new ActorDescriptor(getNodesActor.getActorName(),
				getNodesActor, null));
	}

	public ActorDescriptor getActorDescriptorByName(String actorName)
	{
		ActorDescriptor ad;

		// check for internal actors first
		ad = internalActorMap.get(actorName);
		if (null != ad)
		{
			return ad;
		}
		ad = actorServiceLoader.getActorDescriptorInstance(actorName);
		if (null == ad)
		{
			throw new TrellisException("cannot locate actor, " + actorName
					+ ".  You must provide a ServiceLoader implementation.  See javadoc for ActorServiceLoader");
		}

		return ad;
	}

}
