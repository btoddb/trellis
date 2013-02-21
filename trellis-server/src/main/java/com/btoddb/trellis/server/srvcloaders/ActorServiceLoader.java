
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

package com.btoddb.trellis.server.srvcloaders;

import java.lang.reflect.Method;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.actor.KeyspaceColFamKey;
import com.btoddb.trellis.actor.TrellisActor;
import com.btoddb.trellis.actor.TrellisActorLoader;
import com.btoddb.trellis.actor.TrellisDataTranslator;
import com.btoddb.trellis.actor.TrellisPersistenceProvider;
import com.btoddb.trellis.classloader.ClassInstantiationService;
import com.btoddb.trellis.common.DependencyInjectionService;
import com.btoddb.trellis.server.ActorDescriptor;

/**
 * 
 * Loads actors on-demand via the ServiceLoader facility. As an actor "Service Provider" you should follow instructions
 * here, {@link http ://download.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html}. The service type is,
 * {@link TrellisActorLoader}.
 * 
 */
@Service("actorServiceLoader")
public class ActorServiceLoader
{
	private static Logger logger = LoggerFactory.getLogger(ActorServiceLoader.class);

	private static final ServiceLoader<TrellisActorLoader> serviceLoader = ServiceLoader.load(TrellisActorLoader.class);

	private final Object actorMonitor = new Object();
	private final ConcurrentMap<String, ActorDescriptor> actorMap = new ConcurrentHashMap<String, ActorDescriptor>();

	private final Object dataTransMonitor = new Object();
	private final ConcurrentMap<KeyspaceColFamKey, TrellisDataTranslator> colFamToDataTransMap = new ConcurrentHashMap<KeyspaceColFamKey, TrellisDataTranslator>();

	private final Object providerMonitor = new Object();
	private final ConcurrentMap<Class<? extends TrellisPersistenceProvider>, TrellisPersistenceProvider> classToProviderMap = new ConcurrentHashMap<Class<? extends TrellisPersistenceProvider>, TrellisPersistenceProvider>();

	@Autowired
	private ClassInstantiationService classInstSrvc;
	@Autowired
	private DependencyInjectionService depInjSrvc;
//	@Autowired
//	private TrellisSerializerService serSrvc;


	public ActorDescriptor getActorDescriptorInstance(String actorName)
	{
		ActorDescriptor ad = actorMap.get(actorName);
		if (null != ad)
		{
			return ad;
		}

		// this double-check-lock is thread-safe because ConcurrentMap
		// guarantees that 'gets' always see the latest 'updates'
		synchronized (actorMonitor)
		{
			ad = actorMap.get(actorName);
			if (null != ad)
			{
				return ad;
			}

			for (TrellisActorLoader loader : serviceLoader)
			{
				if (loader.getActorName().equals(actorName))
				{
					ad = new ActorDescriptor(actorName, instantiateGridActor(loader), getProviderByClass(loader.getPersistenceProvider()));
					actorMap.put(actorName, ad);
					return ad;
				}
			}
		}

		return ad;
	}

	private TrellisActor instantiateGridActor(TrellisActorLoader loader)
	{
		TrellisActor actor = (TrellisActor) classInstSrvc.instantiateObject(loader.getActorClass(), (Object[]) null);
		depInjSrvc.injectDependencies(actor);

		logger.info("instantiated actor : " + actor.getClass().getName());

		return actor;
	}

	private void instantiateSingletons(Class<?>[] classes)
	{
		if (null != classes && 0 < classes.length)
		{
			for (Class<?> clazz : classes)
			{
				Object obj = classInstSrvc.instantiateObject(clazz, (Object[]) null);
				depInjSrvc.injectDependencies(obj);
			}
		}
	}

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	private void instantiateSerializers(Map<Class<?>, Class<? extends TrellisSerializer<?>>> serMap)
//	{
//		if (null != serMap && !serMap.isEmpty())
//		{
//			for (Entry<Class<?>, Class<? extends TrellisSerializer<?>>> entry : serMap.entrySet())
//			{
//				TrellisSerializer<?> obj = (TrellisSerializer<?>) classInstSrvc.instantiateObject(
//						entry.getValue(), (Object[]) null);
//				depInjSrvc.injectDependencies(obj);
//				callInitMethod(obj);
//				serSrvc.registerSerializerButDontPersist(
//						new SerializedTypeDef(entry.getKey(), obj), true);
//				logger.info("registered serializer : " + obj.getClass().getName());
//			}
//		}
//	}

	public void init()
	{
		for (TrellisActorLoader loader : serviceLoader)
		{
//			instantiateSerializers(loader.getSerializerClasses());
			instantiateSingletons(loader.getSingletonDependencies());
		}
	}

	public TrellisDataTranslator getCassDataTransInstance(String keyspaceName, String colFamName)
	{
		KeyspaceColFamKey targetKsColKey = new KeyspaceColFamKey(keyspaceName, colFamName);
		TrellisDataTranslator dataTrans = colFamToDataTransMap.get(targetKsColKey);
		if (null != dataTrans)
		{
			return dataTrans;
		}

		// this double-check-lock is thread-safe because ConcurrentMap
		// guarantees that 'gets' always see the latest 'updates'
		synchronized (dataTransMonitor)
		{

			dataTrans = colFamToDataTransMap.get(targetKsColKey);
			if (null != dataTrans)
			{
				return dataTrans;
			}

			for (TrellisActorLoader loader : serviceLoader)
			{
				KeyspaceColFamKey[] loaderKsColKeys = loader.getColumnFamilyNames();
				if (null != loaderKsColKeys)
				{
					for (KeyspaceColFamKey ksColKey : loaderKsColKeys)
					{
						if (ksColKey.equals(targetKsColKey))
						{
							dataTrans = instantiateDataTranslator(loader);
							break;
						}
					}

					if (null != dataTrans)
					{
						// inject dependencies since instantiated with default
						// constructor

						depInjSrvc.injectDependencies(dataTrans);

						colFamToDataTransMap.put(targetKsColKey, dataTrans);
						return dataTrans;
					}
				}
			}
		}

		return dataTrans;
	}

	public TrellisPersistenceProvider getProviderByClass(Class<? extends TrellisPersistenceProvider> providerClass)
	{
		TrellisPersistenceProvider provider = classToProviderMap.get(providerClass);
		if (null != provider)
		{
			return provider;
		}

		// this double-check-lock is thread-safe because ConcurrentMap
		// guarantees that 'gets' always see the latest 'updates'
		synchronized (providerMonitor)
		{
			provider = classToProviderMap.get(providerClass);
			if (null != provider)
			{
				return provider;
			}

			for (TrellisActorLoader loader : serviceLoader)
			{
				Class<? extends TrellisPersistenceProvider> clazz = loader.getPersistenceProvider();
				if (null != clazz)
				{
					if (clazz.equals(providerClass))
					{
						provider = instantiateProvider(loader);
						depInjSrvc.injectDependencies(provider);

						classToProviderMap.put(providerClass, provider);
						return provider;
					}
				}
			}
		}

		return provider;
	}

	private TrellisDataTranslator instantiateDataTranslator(TrellisActorLoader loader)
	{
		TrellisDataTranslator dataTrans = (TrellisDataTranslator) classInstSrvc.instantiateObject(
				loader.getDataTranslator(), (Object[]) null);
		depInjSrvc.injectDependencies(dataTrans);
		callInitMethod(dataTrans);

		logger.info("instantiated cassandra data translator : " + dataTrans.getClass().getName());

		return dataTrans;
	}

	private TrellisPersistenceProvider instantiateProvider(TrellisActorLoader loader)
	{
		TrellisPersistenceProvider provider = (TrellisPersistenceProvider) classInstSrvc.instantiateObject(
				loader.getPersistenceProvider(), (Object[]) null);
		depInjSrvc.injectDependencies(provider);
		callInitMethod(provider);

		logger.info("instantiated cassandra persistence provider : " + provider.getClass().getName());

		return provider;
	}

	private void callInitMethod(Object obj)
	{
		try
		{
			Method meth = obj.getClass().getMethod("init", new Class[] {});
			meth.invoke(obj, new Object[] {});
		}
		catch (Exception e)
		{
			// ignore - means no 'init' method
		}
	}
}
