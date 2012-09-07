package com.btoddb.trellis.common.serialization;

import java.nio.ByteBuffer;


/**
 * Defines interface for custom serializers.  If you have a custom type, you must create and
 * register a custom serializer so the serialization system can handle the new type.
 *
 * @param <T> The custom type
 */
public interface TrellisSerializer<T>
{
	T deserialize( ByteBuffer bb );
	
	ByteBuffer serialize( ByteBuffer bb, T obj );

	String getTypeId();
	
	@SuppressWarnings("rawtypes")
	Class getType();
	
	int calculateSerializedSize( T obj );

}
