/**
 * 
 */
package org.topicquests.pg.api;

//import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author jackpark
 *
 */
public interface IEdge extends Edge {
	public static final String
		LABEL		= "label",
		OUT_VERTEX	= "outID",
		IN_VERTEX	= "inID";
	
//	void startTransaction();
//	void endTransaction();
	void setId(String id);

	void setOutVertex(Vertex v);
	void setInVertex(Vertex v);
	
	Vertex getOutVertex();
	Vertex getInVertex();
	
	/**
	 * Allow to update a Vertex
	 * @param v
	 */
	void updateInVertex(Vertex v);
	void updateOutVertex(Vertex v);
	
	String getInVertexId();
	String getOutVertexId();
	
	void setInVertexId(String id);
	void setOutVertexId(String id);
}
