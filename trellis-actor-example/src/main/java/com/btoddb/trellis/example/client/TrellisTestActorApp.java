
package com.btoddb.trellis.example.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.client.TrellisSessionMgr;
import com.btoddb.trellis.client.NettyClientHandler;
import com.btoddb.trellis.client.NettyConnectionManager;
import com.btoddb.trellis.client.NoOpQueue;
import com.btoddb.trellis.client.NodeDiscoveryRemoteService;
import com.btoddb.trellis.client.RoundRobinSelectionStrategy;
import com.btoddb.trellis.client.TrellisClient;
import com.btoddb.trellis.client.TrellisCluster;
import com.btoddb.trellis.client.TrellisObjectDecoder;
import com.btoddb.trellis.client.TrellisObjectEncoder;
import com.btoddb.trellis.common.JmxStatsHelper;
import com.btoddb.trellis.common.StopWatchInNanos;
import com.btoddb.trellis.common.TrellisRemoteException;
import com.btoddb.trellis.common.serialization.TrellisSerializerServiceImpl;
import com.btoddb.trellis.example.actors.DataPoint;
import com.btoddb.trellis.example.actors.MedianPersistenceProvider;
import com.btoddb.trellis.example.actors.MedianTrellisActor;

import me.prettyprint.cassandra.serializers.IntegerSerializer;
import me.prettyprint.cassandra.service.BatchSizeHint;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

public class TrellisTestActorApp
{
	private static final Logger logger = LoggerFactory.getLogger(TrellisTestActorApp.class);

	private static final String DEFAULT_HOST = "phsxcasess009";
	private static final int CASS_PORT = 9160;
	private static final int TRELLIS_PORT = 2582;

	private static final int KEY_START = 1;
	private static final int KEY_MAX = 2520;
	private static final int NUM_COLUMNS = 70000;

	private Cluster cluster;
	private Keyspace keyspace;
	private TrellisCluster trellisCluster;
	private TrellisClient trellisClient;

	private long runTimeInSeconds;
	private int numKeys;
	private int numCols;

	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException
	{
		if ( 3 != args.length ) {
			System.out.println();
			System.out.println( "usage: <test-duration-secs> <num-keys-per-call> <num-columns-per-call>");
			System.out.println();
			return;
		}
		TrellisTestActorApp app = new TrellisTestActorApp();
		app.setRunTimeInSeconds(Long.parseLong(args[0]));
		app.setNumKeys(Integer.parseInt(args[1]));
		app.setNumCols(Integer.parseInt(args[2]));		

		app.initHector();
		app.createKeyspace();
		app.initTrellis();
//		app.generatePrices();
		app.calcMedian();
	}

	private void initHector()
	{
		CassandraHostConfigurator conf = new CassandraHostConfigurator();
		conf.setHosts(DEFAULT_HOST);
		conf.setPort(CASS_PORT);
		conf.setAutoDiscoverHosts(true);
		cluster = HFactory.createCluster(MedianPersistenceProvider.CLUSTER_NAME, conf, null);
		keyspace = HFactory.createKeyspace(MedianPersistenceProvider.KEYSPACE_NAME, cluster);
	}

	private void createKeyspace()
	{
		// try
		// {
		// cluster.dropKeyspace(MedianPersistenceProvider.KEYSPACE_NAME);
		// }
		// catch (HInvalidRequestException e)
		// {
		// logger.info(e.getMessage());
		// }

		try
		{
			ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(
					MedianPersistenceProvider.KEYSPACE_NAME, MedianPersistenceProvider.CF_MEDIAN,
					ComparatorType.INTEGERTYPE);
			KeyspaceDefinition ksDef = HFactory.createKeyspaceDefinition(MedianPersistenceProvider.KEYSPACE_NAME,
					"org.apache.cassandra.locator.SimpleStrategy", 1, Collections.singletonList(cfDef));
			cluster.addKeyspace(ksDef);
		}
		catch (HInvalidRequestException e)
		{
			logger.info(e.getMessage());
		}

	}

	private void initTrellis()
	{
		NodeDiscoveryRemoteService discoveryService = new NodeDiscoveryRemoteService();

		TrellisSessionMgr sessMgr = new TrellisSessionMgr();

		NettyClientHandler nettyClientHandler = new NettyClientHandler();
		nettyClientHandler.setFinalizeQueue(new NoOpQueue());
		nettyClientHandler.setGridSessionMgr(sessMgr);

		TrellisSerializerServiceImpl serSrvc = new TrellisSerializerServiceImpl();
		serSrvc.setServerMode(false);
		serSrvc.init();

		TrellisObjectEncoder trellisObjEnc = new TrellisObjectEncoder();
		trellisObjEnc.setSerSrvc(serSrvc);
		TrellisObjectDecoder trellisObjDec = new TrellisObjectDecoder();
		trellisObjDec.setSerSrvc(serSrvc);

		NettyConnectionManager connMgr = new NettyConnectionManager();
		connMgr.setClientHandler(nettyClientHandler);
		connMgr.setObjEncoder(trellisObjEnc);
		connMgr.setObjDecoder(trellisObjDec);
		connMgr.init();

		trellisCluster = new TrellisCluster();
		trellisCluster.setHostsStr(DEFAULT_HOST);
		trellisCluster.setPort(TRELLIS_PORT);
		trellisCluster.setHostSelectionStrategy(new RoundRobinSelectionStrategy());
		trellisCluster.setNodeDiscoverySrvc(discoveryService);
		trellisCluster.setGridSessMgr(sessMgr);
		trellisCluster.setConnMgr(connMgr);

		trellisClient = new TrellisClient();
		trellisClient.setTrellisCluster(trellisCluster);

		// do this last so as not to start the discover service before client is ready - bad, should be refactored
		discoveryService.setGridClient(trellisClient);
		discoveryService.setSerSrvc(serSrvc);
		trellisCluster.init();
	}

	private void generatePrices()
	{
		BatchSizeHint bsHint = new BatchSizeHint(1, NUM_COLUMNS);
		for (int key = KEY_START; key <= KEY_MAX; key++)
		{
			System.out.print("writing key = " + key + " ... ");
			Mutator<Integer> m = HFactory.createMutator(keyspace, IntegerSerializer.get(), bsHint);
			for (int colName = 1; colName <= NUM_COLUMNS; colName++)
			{
				m.addInsertion(key, MedianPersistenceProvider.CF_MEDIAN, HFactory.createColumn(colName, colName));
			}
			m.execute();
			System.out.println("complete");
		}
	}

	private void calcMedian()
	{
		System.out.println( "test duration              = " + runTimeInSeconds);
		System.out.println( "number of keys per call    = " + numKeys);
		System.out.println( "number of columns per call = " + numCols);
		System.out.println();
		
		boolean instrumentCall = true;
		Random rand = new Random(System.currentTimeMillis());
		
		JmxStatsHelper jmx = new JmxStatsHelper(runTimeInSeconds*2);

		try
		{
			StopWatchInNanos testDuration = new StopWatchInNanos().start();
			while (testDuration.getDuratinInSeconds() < runTimeInSeconds)
			{
				//
				// generate keys
				//
				
				Set<Integer> keySet = new HashSet<Integer>();
				while (keySet.size() < numKeys)
				{
					keySet.add(rand.nextInt(KEY_MAX) + 1);
				}

				List<ByteBuffer> keyList = new ArrayList<ByteBuffer>(keySet.size());
				for (Integer key : keySet)
				{
					keyList.add((ByteBuffer) ByteBuffer.allocate(4).putInt(key).rewind());
				}

				//
				// generate columns
				//
				
				Set<Integer> colIds = new HashSet<Integer>();
				while (colIds.size() < numCols)
				{
					colIds.add(rand.nextInt(numCols) + 1);
				}
				
				StopWatchInNanos callDuration = new StopWatchInNanos().start();
				Map<ByteBuffer, Object> respMap = trellisClient.sendRequestToGrid(MedianTrellisActor.ACTOR_NAME,
						MedianPersistenceProvider.KEYSPACE_NAME, keyList, colIds, instrumentCall);
				callDuration.stop();
				jmx.addRollingSample("call-duration", 1, callDuration.getDuratinInMicros());
				System.out.println( "(" + (runTimeInSeconds - testDuration.getDuratinInSeconds()) + " secs) duration = " + callDuration.getDuratinInMicros() + " micros" );
				for (Entry<ByteBuffer, Object> entry : respMap.entrySet())
				{
					if (!(entry.getValue() instanceof TrellisRemoteException))
					{
						ByteBuffer bb = (ByteBuffer) entry.getValue();
						DataPoint dp = new DataPoint(bb.getInt(), bb.getInt());
//						System.out.println("(" + (runTimeInSeconds - testDuration.getDuratinInSeconds()) + " secs) dp = " + dp);
					}
					else
					{
						logger.error("remote exception thrown by trellis server : " + entry.getValue());
					}
				}
			}
		}
		finally
		{
			trellisCluster.shutdown();
		}

		System.out.println();
		System.out.println( "average call duration = " + jmx.getRollingStat("call-duration").getAverageSample());
	}

	public void setRunTimeInSeconds(long runTimeInSeconds)
	{
		this.runTimeInSeconds = runTimeInSeconds;
	}

	public int getNumKeys()
	{
		return numKeys;
	}

	public void setNumKeys(int numKeys)
	{
		this.numKeys = numKeys;
	}

	public int getNumCols()
	{
		return numCols;
	}

	public void setNumCols(int numCols)
	{
		this.numCols = numCols;
	}

	public long getRunTimeInSeconds()
	{
		return runTimeInSeconds;
	}
}
