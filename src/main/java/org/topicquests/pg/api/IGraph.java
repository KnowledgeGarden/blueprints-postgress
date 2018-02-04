/**
 * 
 */
package org.topicquests.pg.api;

import org.topicquests.support.api.IResult;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author jackpark
 *
 */
public interface IGraph extends Graph {
	public static final String
		INDEX_TABLE		= "vindex",
		EDGE_KEY		= "edge";

	IResult putVertex(Vertex v);
	
	/**
	 * Update the graph when <code>v</code> has been externally modified.
	 * @param v
	 * @return
	 */
	IResult updateVertex(Vertex v);
}
