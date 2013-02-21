
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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

public class TrellisStatsJmxBean implements DynamicMBean
{
	private static final long DEFAULT_WINDOW_SIZE_IN_MILLIS = 60000;
	
	private static final String AVG_TYPE = "average";
	private static final Object PER_SECOND_TYPE = "perSecond";
	private static final String MIN_TYPE = "min";
	private static final String MAX_TYPE = "max";	
	
	private final JmxStatsHelper stats;
	
	public TrellisStatsJmxBean() {
		stats = new JmxStatsHelper(DEFAULT_WINDOW_SIZE_IN_MILLIS);
	}

	/**
	 * Add the attribute to the bean, but with empty value.
	 * 
	 * @param string
	 */
	public void addRollingSampleName(String attribBaseName)
	{
		stats.addRollingSample(attribBaseName, 0, 0);
	}

	/**
	 * Add sample in microseconds
	 * 
	 * @param attribBaseName
	 * @param val
	 * @return
	 */
	public TrellisStatsJmxBean addRollingSample(String attribBaseName, long valInMicros)
	{
		stats.addRollingSample(attribBaseName, 1, valInMicros);
		return this;
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException,
			ReflectionException
	{
		Object retObj = getNormalAttribute(attribute);
		if (null == retObj)
		{
			retObj = getRollingAttribute(attribute);
		}

		if (null == retObj)
		{
				throw new AttributeNotFoundException("Attribute," + attribute
						+ ", not found on this MBean, "
						+ TrellisStatsJmxBean.class.getCanonicalName());
		}
		return retObj;
	}

	private Object getNormalAttribute(String name)
	{
		return stats.getCounterValue(name);
	}

	private Object getRollingAttribute(String attribute) throws AttributeNotFoundException,
			MBeanException, ReflectionException
	{
		AttribDescriptor attribDesc = new AttribDescriptor(attribute);
		Stat val = stats.getRollingStat(attribDesc.getBaseName());
		
		
		if (AVG_TYPE.equals(attribDesc.getStatType()))
		{
			return val.getAverageSample();
		}
		else if (PER_SECOND_TYPE.equals(attribDesc.getStatType()))
		{
			return val.getSamplesPerSecond();
		}
		else if ( MIN_TYPE.equals(attribDesc.getStatType())) {
			return val.getMinimumSample();
		}
		else if ( MAX_TYPE.equals(attribDesc.getStatType())) {
			return val.getMaximumSample();
		}
		else
		{
			throw new AttributeNotFoundException("Attribute," + attribute
					+ ", not found on this MBean, "
					+ TrellisStatsJmxBean.class.getCanonicalName());
		}
	}

//	private Object stringifyFloat(Object val)
//	{
//		if ( val instanceof Float || val instanceof Double ) {
//			return String.format( "%.3f", val);
//		}
//		else {
//			return val;
//		}
//	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException
	{
		
	}

	@Override
	public AttributeList getAttributes(String[] attributes)
	{
		AttributeList al = new AttributeList(attributes.length);
		for (String attribName : attributes)
		{
			try
			{
				al.add(new Attribute(attribName, getAttribute(attribName)));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		return al;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes)
	{
		// TODO bburruss Auto-generated method stub
		return null;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException
	{
		// TODO bburruss Auto-generated method stub
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo()
	{
		MBeanAttributeInfo[] ai = new MBeanAttributeInfo[stats.getCounterStatNames().size()+(3*stats.getRollingStatNames().size())];
		int index = 0;
		for ( String name : stats.getCounterStatNames()) {
			ai[index++] = new MBeanAttributeInfo(name, String.class.getName(),
					name, true, false, false);
		}

		for (String baseName : stats.getRollingStatNames())
		{
			ai[index++] = new MBeanAttributeInfo(baseName + "-" + AVG_TYPE, String.class.getName(),
					"Average of " + baseName, true, false, false);
			ai[index++] = new MBeanAttributeInfo(baseName + "-" + MAX_TYPE , String.class.getName(),
					"Maximum of " + baseName, true, false, false);
			ai[index++] = new MBeanAttributeInfo(baseName + "-" + MIN_TYPE, String.class.getName(),
					"Maximum of " + baseName, true, false, false);
		}

		return new MBeanInfo(TrellisStatsJmxBean.class.getCanonicalName(),
				"Message Statistics", ai, null, null, null);
	}

	public class AttribDescriptor
	{
		private String baseName;
		private String statType;

		public AttribDescriptor(String attribName)
		{
			int dashPos = attribName.lastIndexOf('-');
			if (-1 == dashPos)
			{
				throw new RuntimeException("attribName, " + attribName
						+ ", is malformed.  Must be of the form <attribute-name>-<attribute-type>");
			}

			baseName = attribName.substring(0, dashPos);
			statType = attribName.substring(dashPos + 1);
		}

		public AttribDescriptor(String baseName, String statType)
		{
			this.baseName = baseName;
			this.statType = statType;
		}

		public String getBaseName()
		{
			return baseName;
		}

		public String getStatType()
		{
			return statType;
		}

	}
}
