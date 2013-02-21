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
