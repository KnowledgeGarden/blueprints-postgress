//package com.tinkerpop.blueprints.impls.tg;
package org.topicquests.pg.blueprints;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

import net.minidev.json.JSONObject;

import java.util.HashMap;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IEdge;



/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author park -- adapted to new project
 */
class TinkerEdge extends TinkerElement implements IEdge {
	
    private Vertex inVertex=null;
    private Vertex outVertex=null;
    
    protected TinkerEdge(final String id, final Vertex outVertex, final Vertex inVertex, final String label,
    		final PostgresBlueprintsEnvironment env,
    		final TinkerGraph g, final VertexProvider vp, final EdgeProvider ep) {
        super(id, env, g, vp, ep);
        setLabel(label);
        this.inVertex = inVertex;
        this.outVertex = outVertex;
        if (inVertex != null)
        	put(IEdge.IN_VERTEX, (String)inVertex.getId());
 
        if (outVertex != null)
        	put(IEdge.OUT_VERTEX, (String)outVertex.getId());
    }
    
    protected TinkerEdge(final JSONObject jo, final PostgresBlueprintsEnvironment env,
    		final TinkerGraph g, final VertexProvider vp, final EdgeProvider ep) {
    	super(jo, env, g, vp, ep);
    }
    
    public void setId(String id) {
    	super.setId(id);
    }

    public void setLabel(String label) {
    	put(IEdge.LABEL, label);
    }
    
    public void setOutVertex(Vertex v) {
    	this.outVertex = v;
    }
    
    public void setInVertex(Vertex v) {
    	this.inVertex = v;
    }
    
    public Vertex getOutVertex() {
    	return this.outVertex;
    }
    
    public Vertex getInVertex() {
    	return this.inVertex;
    }

    public String getLabel() {
        return this.getAsString(IEdge.LABEL);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.IN)) {
        	if (inVertex == null)
        		inVertex = _getVertex(Direction.IN);
            return inVertex;
        } else if (direction.equals(Direction.OUT)) {
        	if (outVertex == null)
        		outVertex = _getVertex(Direction.OUT);
            return outVertex;
    	}else
            throw ExceptionFactory.bothIsNotSupported();
    }

    /**
     * Fetch if needed
     * @param d
     * @return
     */
    Vertex _getVertex(Direction d) {
    	String id;
    	if (d.equals(Direction.IN))
    		id = super.getAsString(IEdge.IN_VERTEX);
    	else
    		id = super.getAsString(IEdge.OUT_VERTEX);
    	environment.logDebug("TinkerEdge._getVertex "+id);
    	return vertexProvider.get(id);
    }
    
    public String toString() {
        return StringFactory.edgeString(this);
    }

	@Override
	public void updateInVertex(Vertex v) {
		setInVertex(v);
	}

	@Override
	public void updateOutVertex(Vertex v) {
		setOutVertex(v);
	}

	@Override
	public String getInVertexId() {
		return super.getAsString(IEdge.IN_VERTEX);
	}

	@Override
	public String getOutVertexId() {
		return super.getAsString(IEdge.OUT_VERTEX);
	}
	/*
	@Override
	public void startTransaction() {
		isTransaction = true;
	}

    public void endTransaction() {
    	isTransaction = false;
    	//update this object
    	updateThis(false);
    }
    */

	@Override
	public void setInVertexId(String id) {
		put(IEdge.IN_VERTEX, id);
	}

	@Override
	public void setOutVertexId(String id) {
	    put(IEdge.OUT_VERTEX, id);
	}

}
