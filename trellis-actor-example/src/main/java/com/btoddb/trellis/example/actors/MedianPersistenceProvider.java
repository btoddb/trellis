
package com.btoddb.trellis.example.actors;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;

import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.Row;
import org.apache.cassandra.db.SliceByNamesReadCommand;
import org.apache.cassandra.db.SliceFromReadCommand;
import org.apache.cassandra.service.StorageProxy;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btoddb.trellis.actor.KeyspaceColFamKey;
import com.btoddb.trellis.actor.TrellisPersistenceProvider;
import com.btoddb.trellis.common.PersistentKey;
import com.btoddb.trellis.common.TrellisException;
import com.btoddb.trellis.common.serialization.TrellisSerializerService;

public class MedianPersistenceProvider implements TrellisPersistenceProvider
{
	public static final String CLUSTER_NAME = "BToddB-Test";
	public static final String KEYSPACE_NAME = "TrellisTest";
	public static final String CF_MEDIAN = "Median";

	private static Logger logger = LoggerFactory.getLogger(MedianPersistenceProvider.class);
	private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
	private static final KeyspaceColFamKey KS_COL_FAM_KEY = new KeyspaceColFamKey(KEYSPACE_NAME, CF_MEDIAN);

	public MedianPersistenceProvider()
	{}

	@Override
	public void init()
	{}

	@Override
	public void shutdown()
	{}

	@Override
	public void save(PersistentKey key, Object value)
	{
		throw new NotImplementedException();
	}

	@Override
	public Map<PersistentKey, Object> load(List<PersistentKey> keyList)
	{
		if (null == keyList || keyList.isEmpty())
		{
			return null;
		}

		List<ReadCommand> cmdList = createReadCommandList(keyList);

		List<Row> rowList = null;
		try
		{
			rowList = StorageProxy.read(cmdList, ConsistencyLevel.ONE);
			assert 1 == rowList.size() || rowList.isEmpty();
		}
		catch (Throwable e)
		{
			logger.error("exception while retrieving hotel prices via StorageProxy", e);
			throw new TrellisException("exception while reading via StorageProxy", e);
		}

		if (null == rowList)
		{
			return null;
		}

		Map<PersistentKey, Object> retMap = new LinkedHashMap<PersistentKey, Object>();
		int keyIndex = 0;
		for (Row row : rowList)
		{
			if (null == row.cf)
			{
				Integer key = row.key.key.remaining() > 0 ? row.key.key.duplicate().getInt() : null;
				logger.warn("for key, " + key + ", returned nothing");
				return null;
			}
			DataPoint[] list = new DataPoint[row.cf.getColumnCount()];
			int index = 0;
			for (IColumn col : row.cf)
			{
				DataPoint dp = new DataPoint(col.name().duplicate().getInt(), col.value().duplicate().getInt());
				list[index++] = dp;
			}

			retMap.put(keyList.get(keyIndex), list);
			keyIndex++;
		}

		return retMap;
	}

	@Override
	public Map<PersistentKey, Object> load(PersistentKey[] keyArr)
	{
		return load(Arrays.asList(keyArr));
	}

	private List<ReadCommand> createReadCommandList(List<PersistentKey> keyList)
	{
		ColumnParent colParent = new ColumnParent(KS_COL_FAM_KEY.getColFamName());

		List<ReadCommand> cmdList = new ArrayList<ReadCommand>(keyList.size());
		for (PersistentKey key : keyList)
		{
			ReadCommand cmd;
			if (null == key.getColumnList() || key.getColumnList().isEmpty())
			{
				cmd = new SliceFromReadCommand(KS_COL_FAM_KEY.getKeyspaceName(), key.getRowKey(), colParent,
						EMPTY_BYTE_BUFFER, EMPTY_BYTE_BUFFER, false, 10000);
			}
			else
			{
				cmd = new SliceByNamesReadCommand(KS_COL_FAM_KEY.getKeyspaceName(), key.getRowKey(), colParent,
						key.getColumnList());
			}
			cmdList.add(cmd);
		}
		return cmdList;
	}

	DataPoint[] copyArrayAndInsertSorted(DataPoint[] oldRow, ArrayList<DataPoint> hpdList)
	{
		if (hpdList.isEmpty())
		{
			return oldRow;
		}

		DataPoint[] newRow = new DataPoint[oldRow.length + hpdList.size()];
		int newIndex = 0;
		int nextHpdToCopy = 0;
		for (int oldIndex = 0; oldIndex < oldRow.length; oldIndex++)
		{
			for (int i = nextHpdToCopy; i < hpdList.size(); i++)
			{
				if (hpdList.get(i).getColName() <= oldRow[oldIndex].getColName())
				{
					newRow[newIndex++] = hpdList.get(i);
					nextHpdToCopy = i + 1;
				}
				else
				{
					break;
				}
			}
			newRow[newIndex++] = oldRow[oldIndex];
		}

		for (int i = nextHpdToCopy; i < hpdList.size(); i++)
		{
			newRow[newIndex++] = hpdList.get(i);
		}

		return newRow;
	}

	public void setmBeanServer(MBeanServer mBeanServer)
	{
		// this.mBeanServer = mBeanServer;
	}

	public void setSerSrvc(TrellisSerializerService serSrvc)
	{}

	@Override
	public KeyspaceColFamKey getKeyspaceColFamKey()
	{
		return KS_COL_FAM_KEY;
	}
}
