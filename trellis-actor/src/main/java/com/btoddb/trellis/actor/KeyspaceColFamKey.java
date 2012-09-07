
package com.btoddb.trellis.actor;

/**
 * Simple pairing of keyspace and column family for mapping to {@link TrellisDataTranslator}s and ease passing around
 * and returning the pairing.
 * 
 */
public class KeyspaceColFamKey
{
	private final String keyspaceName;
	private final String colFamName;

	public KeyspaceColFamKey(String keyspaceName, String colFamName)
	{
		this.keyspaceName = keyspaceName;
		this.colFamName = colFamName;
	}

	public String getKeyspaceName()
	{
		return keyspaceName;
	}

	public String getColFamName()
	{
		return colFamName;
	}

	@Override
	public String toString()
	{
		return "KsPlusColFamKey [keyspaceName=" + keyspaceName + ", colFamName=" + colFamName + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colFamName == null) ? 0 : colFamName.hashCode());
		result = prime * result + ((keyspaceName == null) ? 0 : keyspaceName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyspaceColFamKey other = (KeyspaceColFamKey) obj;
		if (colFamName == null)
		{
			if (other.colFamName != null)
				return false;
		}
		else if (!colFamName.equals(other.colFamName))
			return false;
		if (keyspaceName == null)
		{
			if (other.keyspaceName != null)
				return false;
		}
		else if (!keyspaceName.equals(other.keyspaceName))
			return false;
		return true;
	}

}
