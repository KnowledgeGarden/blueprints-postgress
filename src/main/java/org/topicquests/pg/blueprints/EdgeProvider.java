/**
 * 
 */
package org.topicquests.pg.blueprints;

import java.sql.ResultSet;
import java.util.*;
import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IDataProvider;
import org.topicquests.pg.api.IEdge;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.api.IVertex;
import org.topicquests.support.api.IResult;
import org.topicquests.support.util.LRUCache;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

/**
 * @author jackpark
 *
 */
public class EdgeProvider {
	private PostgresBlueprintsEnvironment environment;
	private IPostgresBlueprintsModel client;
	private TinkerGraph graph;
	private LRUCache cache;
	private VertexProvider vertices;
	private String DBNAME, EDGETABLENAME;

	/**
	 * 
	 */
	public EdgeProvider(PostgresBlueprintsEnvironment env, IPostgresBlueprintsModel m) {
		environment = env;
		cache = new LRUCache(8192);
		client = m;
		DBNAME = environment.getStringProperty("GraphName");
		EDGETABLENAME = environment.getStringProperty("EdgeTable");
	}

	public void init(VertexProvider vp, TinkerGraph g) {
		vertices = vp;
		graph = g;
	}
	
	public void setModel(IPostgresBlueprintsModel m) {
		client = m;
	}
	
	void addToCache(Edge e) {
		synchronized(cache) {
			cache.add(e.getId(), e);
		}
	}
	
	public void invalidateCache(Edge e) {
		synchronized(cache) {
			cache.remove(e.getId());
		}		
	}
	public void removeFromCache(Edge e) {
		//First invalidate all vertex cache entries
		Vertex v = e.getVertex(Direction.OUT);
		vertices.invalidateCache(v);
		v = e.getVertex(Direction.IN);
		vertices.invalidateCache(v);
				
		invalidateCache(e);
	}

	/**
	 * Can return <code>null</code>
	 * @param id
	 * @return
	 */
	Edge getFromCache(Object id) {
		synchronized(cache) {
			return (Edge)cache.get(id);
		}
	}
	
	public Edge get(String id) {
		environment.logDebug("EdgeProvider.get- "+id);
		Edge result = getFromCache(id);
		environment.logDebug("EdgeProvider.get-1 "+result);
		if (result == null) {
			IResult r = client.getEdge(id);
			environment.logDebug("EdgeProvider.get-2 "+r.getErrorString()+" | "+(r.getResultObject() == null));
			result = (Edge) r.getResultObject();
			if (result != null)
				addToCache(result);
		}
		if (result != null)
			environment.logDebug("EdgeProvider.get+ "+((JSONObject)result).toJSONString());
		return result;
	}
	
	public Edge get(String edgeId, String callingVertexId) {
		Edge result = getFromCache(edgeId);
		//TODO
		return result;
	}
	
	public void put(Edge e) {
		client.putEdge((IEdge)e);
	}
	
/*	public void update(Edge e) {
		String id = (String)e.getId();
		removeFromCache(e);
		client.updateEdge(id, (JSONObject)e);
	}
*/	
	public long size() {
		IResult r = client.edgeTableSize();
		Object o = r.getResultObject();
		if (o != null)
			return ((Long)r.getResultObject()).longValue();
		else
			return -1;
	}

	public boolean containsKey(String id) {
		return (get(id) != null);
	}

	/**
	 * <p>This is non-trivial: if you remove an edge, you have
	 * to update its in and out Vertex</p>
	 * @param edge
	 */
	public void remove(Edge edge) {
        TinkerVertex outVertex = (TinkerVertex) edge.getVertex(Direction.OUT);
        TinkerVertex inVertex = (TinkerVertex) edge.getVertex(Direction.IN);
        if (null != outVertex && ((IVertex)outVertex).hasOutEdges()) {
            final Set<Edge> edges = ((IVertex)outVertex).getOutEdgesByLabel(edge.getLabel());
            if (null != edges) {
            	//TODO process vertex
            }
        }
        if (null != inVertex && ((IVertex)inVertex).hasInEdges()) {
            final Set<Edge> edges = ((IVertex)inVertex).getInEdgesByLabel(edge.getLabel());
            if (null != edges) {
            	//TODO process vertex
            }
        }
        //TODO
        //delete this edge
	}
	
	public Iterable<Edge> getEdges(final String key, final Object value) {
		//TODO
		return null;
	}
	
	public List<Edge> values() {
		List<Edge>result = new ArrayList<Edge>();
		//TODO
		return result;
	}
	
	public void clear() {
		throw new RuntimeException("EdgeProvider.clear not implemented");
	}
}
