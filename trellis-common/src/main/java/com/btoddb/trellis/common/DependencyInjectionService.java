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

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.serialization.TrellisSerializerService;
import com.btoddb.trellis.common.serialization.TrellisSerializerServiceImpl;

@Service("dependencyInjectionService")
public class DependencyInjectionService
{

	private Map<String, Object> settableFieldMap = new HashMap<String, Object>();

	public void registerSettableFieldType(Object fieldValue)
	{
		registerSettableFieldType(fieldValue.getClass(), fieldValue);
	}

	public void registerSettableFieldType(Class<?> clazz, Object fieldValue)
	{
		settableFieldMap.put(clazz.getName(), fieldValue);
	}

	public void injectDependencies(Object obj)
	{
		Class<?> clazz = obj.getClass();

		// introspect object to get its bean properties (fields, setters,
		// getters, etc)
		PropertyDescriptor[] pdArr;
		try
		{
			pdArr = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
		}
		catch (IntrospectionException e)
		{
			throw new TrellisException("exception while introspecting class, "
					+ obj.getClass().getCanonicalName());
		}

		// iterate over properties trying to set them
		for (PropertyDescriptor pd : pdArr)
		{
			Class<?> propClass = pd.getPropertyType();
			Field f = null;
			f = findFieldClimbingHierarchy(clazz, pd.getName());

			// if can't find property in hierarchy, then skip
			if (null == f)
			{
				continue;
			}

			// if type has direct match in our cache, then use it
			Object val = settableFieldMap.get(f.getType().getName());
			if (null == val)
			{
				val = findAcceptableTypeMatch(f.getType());
			}

			if (null != val)
			{
				// try a setter method first
				Method meth = pd.getWriteMethod();
				if (null != meth)
				{
					try
					{
						meth.invoke(obj, val);
					}
					catch (Exception e)
					{
						throw new TrellisException("exception while calling method, " + meth.getName()
								+ ", on class, " + obj.getClass().getCanonicalName());
					}
				}
				else
				{
					f.setAccessible(true);
					try
					{
						f.set(obj, val);
					}
					catch (Exception e)
					{
						throw new TrellisException(
								"Found property of type, "
										+ propClass.getCanonicalName()
										+ ", but class, "
										+ obj.getClass().getCanonicalName()
										+ ", does not have a setter method for it.  Tried changing access to public then setting field directly, but didn't work either",
								e);
					}
				}
			}
		}
		
		registerSettableFieldType(obj);
	}

	private Field findFieldClimbingHierarchy(Class<?> clazz, String name)
	{
		if (null == clazz)
		{
			return null;
		}

		try
		{
			return clazz.getDeclaredField(name);
		}
		catch (NoSuchFieldException e)
		{
			return findFieldClimbingHierarchy(clazz.getSuperclass(), name);
		}
		catch (Exception e)
		{
			throw new TrellisException("exception while retrieving field declaration from class, "
					+ clazz.getCanonicalName(), e);
		}
	}

	private Object findAcceptableTypeMatch(Class<?> type)
	{
		if (TrellisSerializerService.class.isAssignableFrom(type))
		{
			return settableFieldMap.get(TrellisSerializerServiceImpl.class.getName());
		}
		else
		{
			return null;
		}
	}

}
