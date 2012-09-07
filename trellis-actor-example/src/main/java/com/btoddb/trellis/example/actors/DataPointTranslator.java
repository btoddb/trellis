
package com.btoddb.trellis.example.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.apache.cassandra.db.IColumn;

import com.btoddb.trellis.actor.TrellisDataTranslator;

public class DataPointTranslator implements TrellisDataTranslator
{
	// private static Logger logger = LoggerFactory.getLogger(PriceQueryTrellisDataTranslator.class);

	private static final Comparator<? super DataPoint> dpComparator = new DataPointComparator();

	public DataPointTranslator()
	{}

	@Override
	public void init()
	{}

	@Override
	public void shutdown()
	{}

	@Override
	public Object translateRow(byte[] key, Collection<IColumn> colList, Object data)
	{
		if (null == colList || colList.isEmpty())
		{
			return null;
		}

		DataPoint[] row = (DataPoint[]) data;

		// if no row pass in, means we are not updating existing data. set row
		// to empty array to signal 'add' all of them as new
		if (null == row)
		{
			row = new DataPoint[0];
		}

		ArrayList<DataPoint> newDpList = new ArrayList<DataPoint>(colList.size());

		for (IColumn col : colList)
		{
			int colName = col.name().duplicate().getInt();

			DataPoint dp = new DataPoint(col.name().duplicate().getInt(), col.value().duplicate().getInt());

			int index = findByColumnName(row, colName);
			if (-1 < index)
			{
				row[index] = dp;
			}
			else
			{
				newDpList.add(dp);
			}
		}

		row = copyArrayAndInsertSorted(row, newDpList);

		return row;
	}

	private DataPoint[] copyArrayAndInsertSorted(DataPoint[] oldRow, ArrayList<DataPoint> dpList)
	{
		if (dpList.isEmpty())
		{
			return oldRow;
		}

		DataPoint[] newRow = new DataPoint[oldRow.length + dpList.size()];
		int newIndex = 0;
		int nextDpToCopy = 0;
		for (int oldIndex = 0; oldIndex < oldRow.length; oldIndex++)
		{
			for (int i = nextDpToCopy; i < dpList.size(); i++)
			{
				if (dpList.get(i).getColName() <= oldRow[oldIndex].getColName())
				{
					newRow[newIndex++] = dpList.get(i);
					nextDpToCopy = i + 1;
				}
				else
				{
					break;
				}
			}
			newRow[newIndex++] = oldRow[oldIndex];
		}

		for (int i = nextDpToCopy; i < dpList.size(); i++)
		{
			newRow[newIndex++] = dpList.get(i);
		}

		return newRow;
	}

	private int findByColumnName(DataPoint[] row, int hid)
	{
		int index = Arrays.binarySearch(row, new DataPoint(hid, 0), dpComparator);
		return index;
	}

}
