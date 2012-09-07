
package com.btoddb.trellis.classloader;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.TrellisException;

@Service("classInstantiationService")
public class ClassInstantiationService
{
	private static final Logger logger = LoggerFactory.getLogger(ClassInstantiationService.class);

//	@Autowired
//	private TrellisSerializerService serSrvc;
//	@Autowired
//	private DependencyInjectionService depInjSrvc;

//	public Object instantiateGridSerializerClass(Class<?> typeClass, String initMethodName)
//	{
//		Object obj;
//
//		// try with both objects
//		obj = instantiateObject(typeClass, new Object[] { TrellisSerializerService.class, serSrvc });
//		if (null == obj)
//		{
//			// try the default constructor as last resort
//			obj = instantiateObject(typeClass);
//			if (null == obj)
//			{
//				throw new TrellisException("class type, " + typeClass.getCanonicalName()
//						+ ", must have default constructor.  Cannot instantiate and register");
//			}
//		}
//
//		initNewObj(obj, initMethodName);
//		return obj;
//	}

//	private Object initNewObj(Object obj, String initMethName)
//	{
//		depInjSrvc.injectDependencies(obj);
//
//		if (null != initMethName)
//		{
//			callInitMethod(obj, initMethName);
//		}
//		return obj;
//	}

//	public Object instantiateAndInjectObject(Class<?> clazz, String initMethod, Object... paramArr)
//	{
//		Object obj = instantiateObject(clazz, paramArr);
//		return initNewObj(obj, initMethod);
//	}

	public Object instantiateObject(Class<?> clazz, Object... paramArr)
	{
		Class<?>[] paramTypeArr = null;
		Object[] paramObjArr = null;

		if (null != paramArr)
		{
			if (0 != paramArr.length % 2)
			{
				throw new TrellisException(
						"parameter array to instantiateObject must consist of type/Object pairs, starting with index 0");
			}

			paramTypeArr = new Class<?>[paramArr.length / 2];
			paramObjArr = new Object[paramArr.length / 2];

			for (int i = 0; i < paramArr.length / 2; i++)
			{
				paramTypeArr[i] = (Class<?>) paramArr[i * 2];
				paramObjArr[i] = paramArr[i * 2 + 1];
			}
		}

		try
		{
			Constructor<?> constr = clazz.getConstructor(paramTypeArr);
			return constr.newInstance(paramObjArr);
		}
		catch (NoSuchMethodException e)
		{
			return null;
		}
		catch (Exception e)
		{
			logger.warn("exception while finding an appropriate constructor for serializer, "
					+ clazz.getCanonicalName() + " : " + e.getMessage());
			return null;
		}
	}

//	private void callInitMethod(Object obj, String initMethodName)
//	{
//		try
//		{
//			Method meth = obj.getClass().getMethod(initMethodName, (Class<?>[]) null);
//			meth.invoke(obj, (Object[]) null);
//		}
//		catch (Exception e)
//		{
//			throw new TrellisException("exception while calling init method", e);
//		}
//
//	}

}
