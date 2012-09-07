
package com.btoddb.trellis.server;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.client.TrellisCluster;
import com.btoddb.trellis.server.netty.NettyServer;
import com.btoddb.trellis.server.stage.DispatchRequestStage;
import com.btoddb.trellis.server.stage.FinalizeSessionStage;
import com.btoddb.trellis.server.stage.ReceiveRequestStage;
import com.btoddb.trellis.server.stage.SendResponseStage;

/**
 * Start grid server and listen for grid requests, delegating to @link
 * {@link GridQueryHandler}. The grid server runs inside the Cassandra JVM to
 * gain performance using direct access to cassandra data.
 * 
 * @author bburruss
 * 
 */
@Service("gridServer")
public class TrellisServer
{
	private static Logger logger = LoggerFactory.getLogger(TrellisServer.class);

	public static final int MINA_MAX_BUFFER_SIZE = 10 * 1024 * 1024;
	public static final int MINA_MIN_BUFFER_SIZE = 4096;
	
	@Value("$gridProps{port}")
	private int port;

	@Autowired
	private TrellisCluster trellisCluster;
	@Autowired
	private NettyServer nettyServer;

	//
	// queue processors
	//

	@Autowired
	private ReceiveRequestStage receiveRequest;
	@Autowired
	private DispatchRequestStage actorDispatcher;
	@Autowired
	private SendResponseStage responseSender;
	@Autowired
	private FinalizeSessionStage sessionFinalizer;

	/**
	 * Entry into object
	 */
	public void startGrid()
	{
		responseSender.start();
		sessionFinalizer.start();
		actorDispatcher.start();
		receiveRequest.start();

		nettyServer.start(port);
		
		// now that server is init'ed, can init grid cluster object
		trellisCluster.init();
		
		logger.info("Trellis is up and waiting for requests on port " + port);
	}

	@PreDestroy
	public void shutdownGrid()
	{
		receiveRequest.stopProcessing();
		actorDispatcher.stopProcessing();
		sessionFinalizer.stopProcessing();
		responseSender.stopProcessing();
		
		nettyServer.shutdown();
		
		trellisCluster.shutdown();
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public void setTrellisCluster(TrellisCluster gridCluster)
	{
		this.trellisCluster = gridCluster;
	}

}
