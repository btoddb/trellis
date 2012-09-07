package com.btoddb.trellis.common.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.btoddb.trellis.common.DependencyInjectionService;


@Service
public class SerializerLoaderService
{
	@SuppressWarnings("rawtypes")
	private static final ServiceLoader<TrellisSerializer> serializerLoader = ServiceLoader.load(TrellisSerializer.class);
	
	@Autowired
	private DependencyInjectionService depInjSrvc;
	
	@SuppressWarnings("rawtypes")
	private final Map<String, TrellisSerializer> serMap = new HashMap<String, TrellisSerializer>(); 

	@SuppressWarnings({ "rawtypes" })
	public TrellisSerializer getSerializerInstance(String serType) {
		TrellisSerializer ser = serMap.get(serType);
		if ( null != ser ) {
			return ser;
		}
		
		// not found so look for it using ServiceLoader
		
		for ( TrellisSerializer tmp : serializerLoader ) {
			if ( tmp.getTypeId().equals(serType)) {
				ser = tmp;
				break;
			}
		}
		
		if ( null == ser ) {
			return ser;
		}
				
		// inject dependencies since instantiated with default constructor

		depInjSrvc.injectDependencies(ser);

		// save injected object for future use
		
		serMap.put(serType, ser);

		return ser;
	}

}
