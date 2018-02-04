/**
 * 
 */
package org.topicquests.pg.api;

import org.topicquests.pg.blueprints.EdgeProvider;
import org.topicquests.pg.blueprints.TinkerGraph;
import org.topicquests.pg.blueprints.VertexProvider;
import org.topicquests.support.api.IResult;
import org.topicquests.pg.api.IEdge;
import org.topicquests.pg.api.IVertex;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author jackpark
 *
 */
public interface IPostgresBlueprintsModel {

	void init(TinkerGraph g, VertexProvider vp, EdgeProvider ep);
	
	IDataProvider getDatabase();
	
	/**
	 * <p>Return a new vertex<p>
	 * <p>There are usecases for <code>isNew</code><br/>
	 * <li><code>true</code> for when an {@link IVertex} is just going to
	 * be constructed and used as is</li>
	 * <li><code>false</code> for when the database is constructing an {@link IVertex}
	 *  during a query</li>
	 * <li><code>false</code> for which a user needs an {@link IVertex} which will
	 *  have additional key-value pairs (internal data) added before persisting</li></p>
	 * <p>When an existing vertex has user manipulations on internal data (changes to
	 *  key-value pairs), the user must make appropriate calls to the model to reflect
	 *  those changes in the database. That is, the present codebase does not perform
	 *  live updates to any key-value pairs other than adding edges.</p>
	 * @param id
	 * @param label
	 * @param type can be <code>null</code>
	 * @param isNew store when <code>true</code>
	 * @return
	 */
	Vertex newVertex(String id, String label, String type, boolean isNew);
	
	/**
	 * For construction in database
	 * @param id
	 * @param outVertex can be <code>null</code>
	 * @param inVertex can be <code>null</code>
	 * @param label
	 * @return
	 */
	Edge newEdgeShell(final String id, final Vertex outVertex, final Vertex inVertex, final String label);
		
	/**
	 * Connect to vertex objects
	 * @param edgeId
	 * @param from
	 * @param to
	 * @param edgeLabel
	 * @return
	 */
	Edge connectVertices(String edgeId, Vertex from, Vertex to, String edgeLabel);
	
	IResult getVertex(String id);
		
	IResult putVertex(IVertex vertex);
	
	IResult insertVertexProperty(String vId, String key, String value);
	IResult updateSingleVertexProperty(String vId, String key, String newValue);
	IResult removeVertexProperty(String vId, String key, String value);
	IResult deleteVertexProperty(String vId, String key);
	IResult updateVertexLabel(String vId, String newLabel);
	IResult updateVertexEditTimestamp(String vId, long newTimestamp);
	IResult hydrateVertex(IVertex vertex);

	IResult putEdge(IEdge edge);
	IResult insertEdgeProperty(String eId, String key, String value);
	IResult updateSingleEdgeProperty(String eId, String key, String newValue);
	IResult removeEdgeProperty(String eId, String key, String value);
	IResult deleteEdgeProperty(String eId, String key);
	IResult updateEdgeLabel(String eId, String newLabel);
	IResult updateEdgeEditTimestamp(String vId, long newTimestamp);
	IResult getEdge(String vId);
	IResult hydrateEdge(IVertex edge);
	
	IResult vertexTableSize();
	
	IResult edgeTableSize();
	
	/**
	 * Very Dangerous
	 * @return
	 */
	IResult clearDatabase();
	
	/**
	 * Must be called to clear open {@link java.sql.Connection} objects
	 */
	void shutDown();
}
