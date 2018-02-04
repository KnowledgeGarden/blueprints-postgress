/**
 * 
 */
package org.topicquests.pg;

import java.util.*;
import org.topicquests.pg.api.IDataProvider;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.blueprints.EdgeProvider;
import org.topicquests.pg.blueprints.TinkerGraph;
import org.topicquests.pg.blueprints.VertexProvider;
import org.topicquests.pg.persist.PostgresDatabase;
import org.topicquests.support.RootEnvironment;

/**
 * @author jackpark
 *
 */
public class PostgresBlueprintsEnvironment extends RootEnvironment {
	private List<TinkerGraph> graphs;
	
	/**
	 * 
	 */
	public PostgresBlueprintsEnvironment() {
		super("postgress-props.xml", "logger.properties");
		graphs = new ArrayList<TinkerGraph>();
	}
	
	
	/**
	 * <p>Return a {#link TinkerGraph} for a graph named by <code>graphName</code></p>
	 * <p>NOTE: <code>graphName</code> is the name of a database which must have
	 * been created by commandline or other means before use.</p>
	 * <p>NOTE: the database must be created with ENCODING UTF-8 and OWNER is the
	 * <em>userName</em> found in /config/postgress-props.xml</p>
	 * @param graphName
	 * @return
	 */
	public TinkerGraph getGraph(String graphName) {
		TinkerGraph result = new TinkerGraph(this, graphName);
		return result;
	}
	
	public void shutDown() {
		Iterator<TinkerGraph> itr = graphs.iterator();
		while (itr.hasNext())
			itr.next().shutdown();
	}
}
