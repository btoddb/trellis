package com.btoddb.trellis.common.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.btoddb.trellis.common.TrellisException;

public class JavaSerializer implements TrellisSerializer<Serializable>
{

	public JavaSerializer() {
	}
	
	@Override
	public Serializable deserialize(final ByteBuffer bb)
	{
		try
		{
			ObjectInputStream ois;
			ois = new ObjectInputStream(new InputStream()
				{
					@Override
					public int read() throws IOException
					{
						return bb.get();
					}
				});
			Object obj = ois.readObject();
			return (Serializable) obj;
		}
		catch (IOException e)
		{
			throw new TrellisException("exception while deserializing Java Serializable object", e);
		}
		catch (ClassNotFoundException e)
		{
			throw new TrellisException("exception while deserializing Java Serializable object", e);
		}
	}

	@Override
	public ByteBuffer serialize(final ByteBuffer bb, Serializable obj)
	{
		OutputStream os = new OutputStream()
			{
				@Override
				public void write(int oneByte) throws IOException
				{
					bb.put((byte) oneByte);
				}
			};
		
		serializeToOutputStream(obj, os);
		return bb;
	}
	
	private void serializeToOutputStream(Serializable obj, OutputStream os) {
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(obj);
			oos.close();
		}
		catch (IOException e)
		{
			throw new TrellisException("exception while serializing Java Serializable object", e);
		}
	}

	@Override
	public String getTypeId()
	{
		return "JSER";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class getType()
	{
		// means to ignore when looking up serializers by class
		return null;
	}

	@Override
	public int calculateSerializedSize(Serializable obj)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializeToOutputStream(obj, baos);
		return baos.size();
	}

}
