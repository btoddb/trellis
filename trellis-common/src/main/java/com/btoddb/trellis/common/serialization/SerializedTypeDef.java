package com.btoddb.trellis.common.serialization;

public class SerializedTypeDef<T>
{
	private String id;
	private Class<?> type;
	private TrellisSerializer<T> serializer;
	
	public SerializedTypeDef(String id, Class<?> type, TrellisSerializer<T> serializer)
	{
		this.id = id;
		this.type = type;
		this.serializer = serializer;
	}

	public SerializedTypeDef(Class<?> type, TrellisSerializer<T> serializer)
	{
		this(serializer.getTypeId(), type, serializer);
	}

	public String getId()
	{
		return id;
	}

	public Class<?> getType()
	{
		return type;
	}

	public TrellisSerializer<T> getSerializer()
	{
		return serializer;
	}

	@Override
	public String toString()
	{
		return "SerializedTypeDef [id=" + id + ", type=" + type + ", serializer=" + serializer.getClass().getSimpleName()
				+ "]";
	}
}
