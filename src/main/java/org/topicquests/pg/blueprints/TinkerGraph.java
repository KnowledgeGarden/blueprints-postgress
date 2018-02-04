//package com.tinkerpop.blueprints.impls.tg;
package org.topicquests.pg.blueprints;


import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultGraphQuery;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

import net.minidev.json.JSONObject;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IDataProvider;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.api.IVertex;
import org.topicquests.pg.persist.PostgresDatabase;

import java.util.ArrayList;
import java.util.UUID;

/**
 * <p>Was: An in-memory, reference implementation of the property graph interfaces provided by Blueprints.</p>
 * <p>Is: a RethinkDB-backed implementation of the property graph interfaces</p>
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author park -- adapted to new project
 */
public class TinkerGraph implements Graph {
	private PostgresBlueprintsEnvironment environment;
	private IDataProvider database;
	private IPostgresBlueprintsModel model;
	private VertexProvider vertices;
	private EdgeProvider edges;

    private static final Features FEATURES = new Features();
    private static final Features PERSISTENT_FEATURES;

    static {
        FEATURES.supportsDuplicateEdges = true;
        FEATURES.supportsSelfLoops = true;
        FEATURES.supportsSerializableObjectProperty = true;
        FEATURES.supportsBooleanProperty = true;
        FEATURES.supportsDoubleProperty = true;
        FEATURES.supportsFloatProperty = true;
        FEATURES.supportsIntegerProperty = true;
        FEATURES.supportsPrimitiveArrayProperty = true;
        FEATURES.supportsUniformListProperty = true;
        FEATURES.supportsMixedListProperty = true;
        FEATURES.supportsLongProperty = true;
        FEATURES.supportsMapProperty = true;
        FEATURES.supportsStringProperty = true;

        FEATURES.ignoresSuppliedIds = false;
        FEATURES.isPersistent = false;
        FEATURES.isWrapper = false;

        FEATURES.supportsIndices = true;
        FEATURES.supportsKeyIndices = true;
        FEATURES.supportsVertexKeyIndex = true;
        FEATURES.supportsEdgeKeyIndex = true;
        FEATURES.supportsVertexIndex = true;
        FEATURES.supportsEdgeIndex = true;
        FEATURES.supportsTransactions = false;
        FEATURES.supportsVertexIteration = true;
        FEATURES.supportsEdgeIteration = true;
        FEATURES.supportsEdgeRetrieval = true;
        FEATURES.supportsVertexProperties = true;
        FEATURES.supportsEdgeProperties = true;
        FEATURES.supportsThreadedTransactions = false;
        FEATURES.supportsThreadIsolatedTransactions = false;

        PERSISTENT_FEATURES = FEATURES.copyFeatures();
        PERSISTENT_FEATURES.isPersistent = true;
    }

    public TinkerGraph(PostgresBlueprintsEnvironment env, String dbName) {
    	environment = env;
    	//TODO
		String dbUrl = environment.getStringProperty("DatabaseURL");
		String dbPort = environment.getStringProperty("DatabasePort");
		String dbUser = environment.getStringProperty("DbUser");
		String dbPwd = environment.getStringProperty("DbPwd");
    	database = new PostgresDatabase(environment, dbUrl, dbPort,dbName, dbUser, dbPwd);
    	model = new PostgresBlueprintsModel(environment, database);
    	edges = new EdgeProvider(environment, model);
    	vertices = new VertexProvider(environment, model);
    	edges.init(vertices, this);
    	vertices.init(edges, this);
    	model.init(this, vertices, edges);
    	vertices.setModel(model);
    	edges.setModel(model);
    	database.setModel(model);
    }

    public VertexProvider getVertexIndex() {
    	return vertices;
    }
    
    public EdgeProvider getEdgeIndex() {
    	return edges;
    }
    
    public IPostgresBlueprintsModel getModel() {
    	return model;
    }
    
    public Iterable<Vertex> getVertices(final String key, final Object value) {
        return vertices.getVertices(key, value);
    }

    public Iterable<Edge> getEdges(final String key, final Object value) {
        return edges.getEdges(key, value);
    }

    public Vertex addVertex(final Object id) {
        String idString = (String)id;
        boolean exists = vertices.containsKey(idString);
        if (exists)
        	throw ExceptionFactory.vertexWithIdAlreadyExists(idString);
        Vertex vertex = new TinkerVertex(idString, environment, this, vertices, edges);
        this.vertices.put(vertex);
        return vertex;
    }

    public Vertex getVertex(final Object id) {
        if (null == id)
            throw ExceptionFactory.vertexIdCanNotBeNull();

        String idString = id.toString();
        return this.vertices.get(idString);
    }

    public Edge getEdge(final Object id) {
        if (null == id)
            throw ExceptionFactory.edgeIdCanNotBeNull();
        environment.logDebug("TinkerGraph.getEdge "+id);
        String idString = id.toString();
        return this.edges.get(idString);
    }


    public Iterable<Vertex> getVertices() {
        return new ArrayList<Vertex>(this.vertices.values());
    }

    public Iterable<Edge> getEdges() {
        return new ArrayList<Edge>(this.edges.values());
    }

    public void removeVertex(final Vertex vertex) {
        if (!this.vertices.containsKey(vertex.getId().toString()))
            throw ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());

        this.vertices.remove(vertex);
    }

    public Edge addEdge(final Object id, Vertex outVertex, Vertex inVertex, final String label) {
        if (label == null)
            throw ExceptionFactory.edgeLabelCanNotBeNull();
        environment.logDebug("TinkerGraph.addEdge "+id);
        String idString = (String)id;
        if (id == null)
        	idString = UUID.randomUUID().toString();
        boolean exists = edges.containsKey(idString);
        if (exists)
            throw ExceptionFactory.edgeWithIdAlreadyExist(id);
        Edge edge;
        edge = new TinkerEdge(idString, outVertex, inVertex, label, environment,
        		this, vertices, edges);
        environment.logDebug("ADDEDGE-0 "+ ((JSONObject)outVertex).toJSONString());
        environment.logDebug("ADDEDGE-00 "+ ((JSONObject)inVertex).toJSONString());
        environment.logDebug("ADDEDGE-1 "+ ((JSONObject)edge).toJSONString());
        environment.logDebug("ADDEDGE-2 ");
        ((IVertex)outVertex).addInEdge(edge, false); //addOutEdge(edge, false);
        environment.logDebug("ADDEDGE-3 "+((JSONObject)outVertex).toJSONString());
        environment.logDebug("ADDEDGE-4 ");
        ((IVertex)inVertex).addOutEdge(edge, false); //addInEdge(edge, false);
        this.edges.put(edge);
        environment.logDebug("ADDEDGE-5 ");
        return getEdge(idString);
    }

    public void removeEdge(final Edge edge) {
        this.edges.remove(edge);
    }

    public GraphQuery query() {
        return new DefaultGraphQuery(this);
    }


    public String toString() {
       return StringFactory.graphString(this, "vertices:" + this.vertices.size() + " edges:" + this.edges.size());
    }

    public void clear() {
        this.vertices.clear();
        this.edges.clear();
   }

    public void shutdown() {
 /*       if (null != this.directory) {
            try {
                final TinkerStorage tinkerStorage = TinkerStorageFactory.getInstance().getTinkerStorage(this.fileType);
                tinkerStorage.save(this, this.directory);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } */
    }


    public Features getFeatures() {
 //       if (null == directory)
            return FEATURES;
 //       else
//            return PERSISTENT_FEATURES;
    }
    
    public void shutDown() {
    	model.shutDown();
    }

}
