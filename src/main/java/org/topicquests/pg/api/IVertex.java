/**
 * 
 */
package org.topicquests.pg.api;
import java.util.*;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public interface IVertex extends Vertex {
	public static final String
		ID_KEY		= "id",
		LABEL		= "label",
		TYPE		= "type",
		OUT_EDGE	= "outEID",
		IN_EDGE		= "inEID";


	//void startTransaction();
	//void endTransaction();
	
	void setId(String id);

	/**
	 * 
	 * @param e
	 * @param isHydrating <code>true</code> when database constructing from query
	 */
	void addInEdge(final Edge e, boolean isHydrating);
	
	/**
	 * 
	 * @param e
	 * @param isHydrating <code>true</code> when database constructing from query
	 */
	void addOutEdge(final Edge e, boolean isHydrating);
	
	void addInEdgeId(String id);
	void addOutEdgeId(String id);
	
	void setLabel(String label);
	String getLabel();
	
	void setType(String type);
	String getType();
	
	boolean hasInEdges();
	boolean hasOutEdges();
	
	Set<Edge> getOutEdgesByLabel(String label);
	Set<Edge> getInEdgesByLabel(String label);
	
	List<String> listAllOutEdges();
	
	List<String> listAllInEdges();
	
	void setInEdges(List<String> edges);
	
	void setOutEdges(List<String> edges);
	
//	Map<String, Set<Edge>> outEdges();
//	Map<String, Set<Edge>> inEdges();
}
