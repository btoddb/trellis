package com.btoddb.trellis.example.actors;

import java.util.Comparator;

public class DataPointComparator implements Comparator<DataPoint>
{
	@Override
	public int compare(DataPoint o1, DataPoint o2)
	{
		float diff = o1.getColName() - o2.getColName();
		return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
	}

}
