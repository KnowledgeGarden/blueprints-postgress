//package com.tinkerpop.blueprints.impls.tg;
package org.topicquests.pg.blueprints;
import java.util.*;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.VerticesFromEdgesIterable;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IVertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author park -- adapted to new project
 */
public class TinkerVertex extends TinkerElement implements IVertex, Serializable {
	
    protected Map<String, Set<Edge>> outEdges = new HashMap<String, Set<Edge>>();
    protected Map<String, Set<Edge>> inEdges = new HashMap<String, Set<Edge>>();

    protected TinkerVertex(final String id, final PostgresBlueprintsEnvironment env,
    		final TinkerGraph g, final VertexProvider vp, final EdgeProvider ep) {
        super(id, env, g, vp, ep);
        isTransaction = false;
    }
    
    protected TinkerVertex(final JSONObject jo, final PostgresBlueprintsEnvironment env,
    		final TinkerGraph g, final VertexProvider vp, final EdgeProvider ep) {
    	super(jo, env, g, vp, ep);
    }
    
    public void setId(String id) {
    	super.setId(id);
    }
    
    void addToInEdges(String edgeId, boolean isHydrating) {
    	List<String> edges = this.listAllInEdges();
    	if (edges == null)
    		edges = new ArrayList<String>();
    	if (!edges.contains(edgeId))
    		edges.add(edgeId);
    	put(IVertex.IN_EDGE, edges);
    	//live update database
    	if (!isHydrating)
    		graph.getModel().insertVertexProperty(this.getId(), IVertex.IN_EDGE, edgeId);
    }
    
    void addToOutEdges(String edgeId, boolean isHydrating) {
    	List<String> edges = this.listAllOutEdges();
    	if (edges == null)
    		edges = new ArrayList<String>();
    	if (!edges.contains(edgeId))
    		edges.add(edgeId);
    	put(IVertex.OUT_EDGE, edges);
    	//live update database
    	if (!isHydrating)
    		graph.getModel().insertVertexProperty(this.getId(), IVertex.OUT_EDGE, edgeId);
    }

    /**
     * Returns a JSONArray which has labels as Keys 
     * and JSONArrays as values
     * @return
     */
    public JSONObject outEdges() {
    	return new JSONObject((HashMap)get(IVertex.OUT_EDGE));
    }
    
    public JSONObject inEdges() {
    	return new JSONObject((HashMap)get(IVertex.IN_EDGE));
    }
    
    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        environment.logDebug("TinkerVertex.getEdges "+this.inEdges+" | "+this.outEdges);
    	if (direction.equals(Direction.OUT)) {
            return this.getOutEdges(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInEdges(labels);
        else {
            return new MultiIterable<Edge>(Arrays.asList(this.getInEdges(labels), this.getOutEdges(labels)));
        }
    }
    private Iterable<Edge> getOutEdges(final String... labels) {
    	return this._getEdges(Direction.OUT, labels);
    }
    private Iterable<Edge> getInEdges(final String... labels) {
    	return this._getEdges(Direction.IN, labels);
    }
    
    List<Edge> toEdges(JSONArray objs) {
    	
    	List<Edge> result = new ArrayList<Edge>();
    	if (objs == null)
    		return result;
    	Iterator<Object>itr = objs.iterator();
    	while (itr.hasNext()) {
    		result.add(new TinkerEdge((JSONObject)itr.next(), environment,
    				graph, vertexProvider, edgeProvider));
    	}
    	return result;
    }
    
    /**
     * <p>This is a JustInTime edge loader</p>
     * <p>TODO: if we had a listEdges() at the database,
     * we can speed this up</p>
     * @param direction
     */
    private void loadEdges(Direction direction) {
    	environment.logDebug("TinkerVertex.loadEdges "+direction);
    	String key;
    	boolean isIn = direction.equals(Direction.IN);
    	//if (isIn)
    	//	key = IVertex.IN_EDGE;
    	//else
    	//	key = IVertex.OUT_EDGE;
    	 key = isIn ? IVertex.IN_EDGE: IVertex.OUT_EDGE;
    	List<String> its = (List<String>)get(key);
    	environment.logDebug("TinkerVertex.loadEdges-1 "+getId()+" "+its);
    	Iterator<String>itr;
    	String id;
    	Edge e;
    	String label;
    	Set<Edge>val;
    	if (its != null && !its.isEmpty()) {

    		itr = its.iterator();
    		while (itr.hasNext()) {
    			e = edgeProvider.get(itr.next());
    			label = e.getLabel();
    	    	environment.logDebug("TinkerVertex.loadEdges-2 "+label+" "+e);
    			if (isIn) {
    				val = this.inEdges.get(label);
    				if (val == null)
    					val = new HashSet<Edge>();
    				val.add(e);
    				inEdges.put(label, val);
    			} else {
    				val = this.outEdges.get(label);
    				if (val == null)
    					val = new HashSet<Edge>();
    				val.add(e);
    				outEdges.put(label, val);
    			}
    		}
    	}
    	if (isIn)
        	environment.logDebug("TinkerVertex.loadEdges+ "+inEdges);
    	else
        	environment.logDebug("TinkerVertex.loadEdges+ "+outEdges);

    }
    private Iterable<Edge> _getEdges(Direction direction, final String... labels) {
    	environment.logDebug("TinkerVertex._getEdges "+direction+" "+labels);
    	boolean isIn = direction.equals(Direction.IN);
    	if (isIn && this.inEdges.isEmpty())
    		loadEdges(Direction.IN);
    	if (!isIn && this.outEdges.isEmpty())
    		loadEdges(Direction.OUT);
        if (labels.length == 0) {
            final List<Edge> totalEdges = new ArrayList<Edge>();
            if (isIn) {
	            for (final Collection<Edge> edges : this.inEdges.values()) {
	                totalEdges.addAll(edges);
	            }
            } else {
	            for (final Collection<Edge> edges : this.outEdges.values()) {
	                totalEdges.addAll(edges);
	            }
             }
            return totalEdges;
        } else if (labels.length == 1) {
        	Set<Edge> ex;
        	if (isIn)
        		ex = this.inEdges.get(labels[0]);
        	else
        		ex = this.outEdges.get(labels[0]);
            final Set<Edge> edges = ex;
            if (null == edges) {
                return Collections.emptyList();
            } else {
                return new ArrayList<Edge>(edges);
            }
        } else {
        	Set<Edge> ex;
            final List<Edge> totalEdges = new ArrayList<Edge>();
            for (final String label : labels) {
            	if (isIn)
            		ex = this.inEdges.get(label);
            	else
            		ex = this.outEdges.get(label);
            	
                final Set<Edge> edges = ex;
                if (null != edges) {
                    totalEdges.addAll(edges);
                }
            }
            return totalEdges;
        }
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new VerticesFromEdgesIterable(this, direction, labels);
    }

    Set<JSONObject> toValues(JSONObject jo) {
    	System.out.println("toValue "+jo.toJSONString());
        Set<JSONObject> result = new HashSet<JSONObject>();
        if (jo == null)
        	return result;
        String key;
        JSONArray val;
        Object o;
        Iterator<String>itr = jo.keySet().iterator();
        Iterator<Object>jtr;
        while (itr.hasNext()) {
        	key = itr.next();
        	o = jo.get(key);
        	if (o instanceof JSONArray) {
        		val = (JSONArray)o;
        		jtr = val.iterator();
        		while (jtr.hasNext())
        			result.add((JSONObject)jtr.next());
        	}
        }
        return result;
    }
    
    private Iterable<Edge> doEdges(boolean isIn, final String...labels) {
        JSONArray edges;
       if (labels.length == 0) {
            final List<JSONObject> totalEdges = new ArrayList<JSONObject>();
            if (isIn)
            	totalEdges.addAll(toValues(inEdges()));
            else
            	totalEdges.addAll(toValues(outEdges()));
            JSONArray ja = new JSONArray();
            Iterator<JSONObject>jx = totalEdges.iterator();
            while (jx.hasNext())
            	ja.add(jx.next());
            return toEdges(ja);
        } else if (labels.length == 1) {
            if (isIn)
            	edges = (JSONArray)this.inEdges().get(labels[0]);
            else
            	edges = (JSONArray)this.outEdges().get(labels[0]);
            if (null == edges) {
                return Collections.emptyList();
            } else {
                return toEdges(edges);
            }
        } else {
            final JSONArray totalEdges = new JSONArray();
            for (final String label : labels) {
            	if (isIn)
            		edges = (JSONArray)this.inEdges().get(label);
            	else
            		edges = (JSONArray)this.inEdges().get(label);
                if (null != edges) {
                    totalEdges.addAll(edges);
                }
            }
            return toEdges(totalEdges);
        }
    }
    private Iterable<Edge> inedges(final String... labels) {
    	return doEdges(true, labels);
    }

    private Iterable<Edge> outedges(final String... labels) {
    	return doEdges(false, labels);
    }

    public VertexQuery query() {
        return new DefaultVertexQuery(this);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(UUID.randomUUID().toString(), this, vertex, label);
    }

    private void _addEdge(boolean isIn, final String label, final Edge edge, boolean isHydrating) {
    	environment.logDebug("_addEdge-1 "+super.toJSONString());
    	String id = (String)edge.getId();
    	if (isIn)
    		this.addToInEdges(id, isHydrating);
    	else
    		this.addToOutEdges(id, isHydrating);
    }
    
    protected void addOutEdge(final String label, final Edge edge, boolean isHydrating) {
    	_addEdge(false, label, edge, isHydrating);
        Set<Edge> edges = this.outEdges.get(label);
        if (null == edges) {
            edges = new HashSet<Edge>();
       }
        edges.add(edge);
        this.outEdges.put(label, edges);
    }

    //////////////////////////////
    //An Edge object, TinkerEdge, extends JSONObject.
    // Thus, we are always dealing with JSONObjects even if parading
    // as TinkerEdge or TinkerVertex objects
    // tq-rethinkdb-provider deals in JSONObjects. It takes them in and
    // it returns them.
    // What goes in a JSONObject ought to remain in the JSON universe
    // Unfortunately, the Blueprints API deals with Java objects;
    //   either we stay with that, or we abandon Blueprints and
    //   make this a hybrid "Blueprint-ish" system.
    //////////////////////////////
    protected void addInEdge(final String label, final Edge edge, boolean isHydrating) {
    	_addEdge(true, label, edge, isHydrating);
        Set<Edge> edges = this.inEdges.get(label);
        if (null == edges) {
            edges = new HashSet<Edge>();
        }
		edges.add(edge);
        this.inEdges.put(label, edges);
	}
    
    /**
     * {@link IVertex}
     * @param e
     */
	@Override
	public void addInEdge(Edge e, boolean isHydrating) {
		addInEdge(e.getLabel(), e, isHydrating);
	}

    /**
     * {@link IVertex}
     * @param e
     */
	@Override
	public void addOutEdge(Edge e, boolean isHydrating) {
		addOutEdge(e.getLabel(), e, isHydrating);
	}

	@Override
	public void setLabel(String label) {
		put(IVertex.LABEL, label);
	}

	@Override
	public String getLabel() {
		return this.getAsString(IVertex.LABEL);
	}

	@Override
	public void setType(String type) {
		put(IVertex.TYPE, type);
	}

	@Override
	public String getType() {
		return this.getAsString(IVertex.TYPE);
	}

	@Override
	public boolean hasInEdges() {
		JSONObject e = this.inEdges();
		return (e != null && !e.isEmpty());
	}

	@Override
	public boolean hasOutEdges() {
		JSONObject e = this.outEdges();
		return (e != null && !e.isEmpty());
	}

	@Override
	public Set<Edge> getOutEdgesByLabel(String label) {
		JSONObject e = this.outEdges();
		JSONArray l = (JSONArray)e.get(label);
		return new HashSet(toEdges(l));
	}

	@Override
	public Set<Edge> getInEdgesByLabel(String label) {
		JSONObject e = this.inEdges();
		JSONArray l = (JSONArray)e.get(label);
		return new HashSet(toEdges(l));
	}

	@Override
	public List<String> listAllOutEdges() {
		return (List<String>)get(IVertex.OUT_EDGE);
	}

	@Override
	public List<String> listAllInEdges() {
		return (List<String>)get(IVertex.IN_EDGE);
	}

	@Override
	public void setInEdges(List<String> edges) {
		put(IVertex.IN_EDGE, edges);
	}

	@Override
	public void setOutEdges(List<String> edges) {
		put(IVertex.OUT_EDGE, edges);
	}

	@Override
	public void addInEdgeId(String id) {
		this.addToInEdges(id, true);
	}

	@Override
	public void addOutEdgeId(String id) {
		this.addToOutEdges(id, true);
	}

}
