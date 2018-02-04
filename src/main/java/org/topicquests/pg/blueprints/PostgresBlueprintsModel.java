/**
 * 
 */
package org.topicquests.pg.blueprints;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IDataProvider;
import org.topicquests.pg.api.IEdge;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.api.IBlueprintsSchema;
import org.topicquests.pg.api.IVertex;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class PostgresBlueprintsModel implements IPostgresBlueprintsModel {
		
	private PostgresBlueprintsEnvironment environment;
	private IDataProvider database;
	private VertexProvider vertices;
	private EdgeProvider edges;
	private TinkerGraph graph;
	
	/**
	 * @param env
	 * @param db
	 */
	public PostgresBlueprintsModel(PostgresBlueprintsEnvironment env, IDataProvider db) {
		environment = env;
		database = db;
	}

	public void init(TinkerGraph g, VertexProvider vp, EdgeProvider ep) {
		graph = g;
		edges = ep;
		vertices = vp;		
	}
	
	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#newVertex(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Vertex newVertex(String id, String label, String type, boolean isNew) {
		TinkerVertex result = new TinkerVertex(id, environment, graph, vertices, edges );

		((IVertex)result).setLabel(label);
		if (type != null && !type.equals(""));
			((IVertex)result).setType(type);
			if (isNew)
				this.putVertex(result);
		return result;
	}


	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#connectVertices(java.lang.String, com.tinkerpop.blueprints.Vertex, com.tinkerpop.blueprints.Vertex, java.lang.String)
	 */
	@Override
	public Edge connectVertices(String edgeId, Vertex from, Vertex to, String edgeLabel) {
		environment.logDebug("PostgresBlueprintsModel.connectVertices "+edgeId);
		return (IEdge)graph.addEdge(edgeId, to, from, edgeLabel);
	}

	@Override
	public Edge newEdgeShell(final String id, final Vertex outVertex, final Vertex inVertex, final String label) {
		IEdge result = new TinkerEdge(id, outVertex, inVertex, label,
				environment, graph, vertices, edges);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#getVertex(java.lang.String)
	 */
	@Override
	public IResult getVertex(String id) {
		IResult r = database.getVertex(id);
		System.out.println("GETVERTEX "+id+" "+r.getErrorString()+" | "+r.getResultObject());
		return r; 
		
	}

	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#getEdge(java.lang.String)
	 */
	@Override
	public IResult getEdge(String id) {
		IResult r = database.getEdge(id);
		return r;
	}

	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#putVertex(java.lang.String, net.minidev.json.JSONObject)
	 */
	@Override
	public IResult putVertex(IVertex vertex) {
		return database.putVertex(vertex);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#putEdge(java.lang.String, net.minidev.json.JSONObject)
	 */
	@Override
	public IResult putEdge(IEdge edge) {
		return database.putEdge(edge);
	}


	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#vertexTableSize()
	 */
	@Override
	public IResult vertexTableSize() {
		return database.tableSize(IBlueprintsSchema.VERTEX_TABLE);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IPostgresBlueprintsModel#edgeTableSize()
	 */
	@Override
	public IResult edgeTableSize() {
		return database.tableSize(IBlueprintsSchema.EDGE_TABLE);
	}

	@Override
	public void shutDown() {
		this.database.shutDown();
	}
	

	@Override
	public IResult clearDatabase() {
		return database.clearDatabase();
	}

	@Override
	public IResult insertVertexProperty(String vId, String key, String value) {
		return database.insertVertexProperty(vId, key, value);
	}

	@Override
	public IResult updateSingleVertexProperty(String vId, String key, String newValue) {
		return database.updateSingleVertexProperty(vId, key, newValue);
	}

	@Override
	public IResult removeVertexProperty(String vId, String key, String value) {
		return database.removeVertexProperty(vId, key, value);
	}

	@Override
	public IResult deleteVertexProperty(String vId, String key) {
		return database.deleteVertexProperty(vId, key);
	}

	@Override
	public IResult updateVertexLabel(String vId, String newLabel) {
		return database.updateVertexLabel(vId, newLabel);
	}

	@Override
	public IResult updateVertexEditTimestamp(String vId, long newTimestamp) {
		return database.updateVertexEditTimestamp(vId, newTimestamp);
	}

	@Override
	public IResult hydrateVertex(IVertex vertex) {
		return database.hydrateVertex(vertex);
	}

	@Override
	public IResult insertEdgeProperty(String eId, String key, String value) {
		return database.insertEdgeProperty(eId, key, value);
	}

	@Override
	public IResult updateSingleEdgeProperty(String eId, String key, String newValue) {
		return database.updateSingleEdgeProperty(eId, key, newValue);
	}

	@Override
	public IResult removeEdgeProperty(String eId, String key, String value) {
		return database.removeEdgeProperty(eId, key, value);
	}

	@Override
	public IResult deleteEdgeProperty(String eId, String key) {
		return database.deleteEdgeProperty(eId, key);
	}

	@Override
	public IResult updateEdgeLabel(String eId, String newLabel) {
		return database.updateEdgeLabel(eId, newLabel);
	}

	@Override
	public IResult updateEdgeEditTimestamp(String vId, long newTimestamp) {
		return database.updateEdgeEditTimestamp(vId, newTimestamp);
	}

	@Override
	public IResult hydrateEdge(IVertex edge) {
		return database.hydrateEdge(edge);
	}

	@Override
	public IDataProvider getDatabase() {
		return database;
	}


}
