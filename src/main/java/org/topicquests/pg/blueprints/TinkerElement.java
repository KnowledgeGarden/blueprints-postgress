//package com.tinkerpop.blueprints.impls.tg;
package org.topicquests.pg.blueprints;

import java.util.Iterator;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;

import net.minidev.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IVertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
abstract class TinkerElement extends JSONObject implements Element, Serializable {
	protected PostgresBlueprintsEnvironment environment;
    private static final String Iterator = null;
    protected final TinkerGraph graph;
	protected VertexProvider vertexProvider;
	protected EdgeProvider edgeProvider;
	protected boolean isTransaction = false;

    protected TinkerElement(final String id, final PostgresBlueprintsEnvironment env,
    		final TinkerGraph g, final VertexProvider vp, final EdgeProvider ep) {
    	this.environment = env;
        this.graph = g;
        this.vertexProvider = vp;
        this.edgeProvider = ep;
        setId(id);
    }
    
    protected TinkerElement(final JSONObject jo, final PostgresBlueprintsEnvironment env,
    		final TinkerGraph g, final VertexProvider vp, final EdgeProvider ep) {
    	this.environment = env;
        this.graph = g;
        this.vertexProvider = vp;
        this.edgeProvider = ep;
    	String key;
    	Iterator<String> itr = jo.keySet().iterator();
    	while (itr.hasNext()) {
    		key = itr.next();
    		put(key, jo.get(key));
    	}
    }
    
    void setId(String id) {
    	put(IVertex.ID_KEY, id);
    }

    public Set<String> getPropertyKeys() {
        return new HashSet<String>(this.keySet());
    }

    public <T> T getProperty(final String key) {
        return (T) this.get(key);
    }
    
    
    public void setProperty(final String key, final Object value) {
        ElementHelper.validateProperty(this, key, value);
        Object oldValue = this.put(key, value);
    }

    public <T> T removeProperty(final String key) {
        Object oldValue = this.remove(key);
        return (T) oldValue;
    }


    public int hashCode() {
        return this.getId().hashCode();
    }

    public String getId() {
        return this.getAsString(IVertex.ID_KEY);
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

    public void remove() {
        if (this instanceof Vertex)
            this.graph.removeVertex((Vertex) this);
        else
            this.graph.removeEdge((Edge) this);
    }
}
