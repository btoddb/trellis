
package com.btoddb.trellis.actor;

import java.util.Collection;

import org.apache.cassandra.db.IColumn;

/**
 * Called by Trellis when a Cassandra row must be translated to a Java Object for caching. Typically called when a
 * mutation notification is raised by Cassandra to Trellis.
 * 
 */
public interface TrellisDataTranslator
{

	void init();

	Object translateRow(byte[] key, Collection<IColumn> colList, Object data);

	void shutdown();

}
