/**
 * 
 */
package org.topicquests.pg.blueprints;

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

/**
 * @author jackpark
 *
 */
public class VertexProvider {
	private PostgresBlueprintsEnvironment environment;
	private IPostgresBlueprintsModel client;
	private TinkerGraph graph;
	private EdgeProvider edges;
	private LRUCache cache;
	private String DBNAME, VERTEXTABLENAME;

	/**
	 * 
	 */
	public VertexProvider(PostgresBlueprintsEnvironment env, IPostgresBlueprintsModel m) {
		environment = env;
		cache = new LRUCache(8192);
		client = m;
		DBNAME = environment.getStringProperty("GraphName");
		VERTEXTABLENAME = environment.getStringProperty("VertexTable");
	}

	public void init(EdgeProvider ep, TinkerGraph g) {
		System.out.println("EdgeProvider.init "+ep+" "+g);
		edges = ep;
		graph = g;
	}
	
	public void setModel(IPostgresBlueprintsModel m) {
		client = m;
	}

	void addToCache(Vertex e) {
		synchronized(cache) {
			cache.add(e.getId(), e);
		}
	}
	/**
	 * Can return <code>null</code>
	 * @param id
	 * @return
	 */
	Vertex getFromCache(Object id) {
		synchronized(cache) {
			return (Vertex)cache.get(id);
		}
	}

	public void invalidateCache(Vertex v) {
		synchronized(cache) {
			cache.remove(v.getId());
		}		
	}
	
	public void removeFromCache(Vertex v) {
		System.out.println("REMOVING "+edges);
		//First, deal with edges
		Iterable<Edge> itr = v.getEdges(Direction.BOTH);
		Iterator<Edge> xtr = itr.iterator();
		Edge e;
		while (xtr.hasNext()) {
			e = xtr.next();
			edges.invalidateCache(e);
		}
				
		invalidateCache(v);
	}
	
	public Vertex get(String id) {
		Vertex result = getFromCache(id);
		if (result == null) {
			IResult r = client.getVertex(id);
			System.out.println("GRAPH "+r.getResultObject());
			JSONObject jo = (JSONObject)r.getResultObject();
			if (jo != null) {
				result = new TinkerVertex(jo, environment, graph, this, edges);
				addToCache(result);
			}
		}
		return  result;
	}
	
	public Vertex get(String vertexId, String callingEdgeId) {
		Vertex result = getFromCache(vertexId);
		//TODO
		return result;
	}
	
	/**
	 * 
	 * @param v
	 * @param callingEdgeId can be <code>null</code>
	 * /
	private void populate(Vertex v, String callingEdgeId) {
		String id = (String)v.getId();
		IVertex vert = (IVertex)v;
		List<String>edl = vert.listAllInEdges();
		Iterator<String> itr;
		String eid;
		Edge e;
		IEdge ei;
		if (edges != null && !edl.isEmpty()) {
			itr = edl.iterator();
			while (itr.hasNext()) {
				eid = itr.next();
				e = edges.get(eid, id);
				//TODO
				// This is where we populate that edge with this
				// except that "this" isn't complete
			}
		}
	}
	*/
	
	public void put(Vertex v) {
		client.putVertex((IVertex)v);
	}
	
/*	public void update(Vertex v) {
		String id = (String)v.getId();
		removeFromCache(v);
		environment.logDebug("UPDATE-1 "+jsonToString(v));
		client.updateVertex(id, (JSONObject)v);
		environment.logDebug("UPDATE-2");
		propagateChange(v);
	}
*/	
	String jsonToString(Vertex v) {
		environment.logDebug("JSONSTRING "+v);
		return ((JSONObject)v).toJSONString();
	}
	/*
	void propagateChange(Vertex v) {
		Object vId = v.getId();
		Iterable<Edge> iv = v.getEdges(Direction.IN);
		environment.logDebug("UPDATE-3 "+iv);
		Iterator<Edge> itr = iv.iterator();
		Edge d;
		IEdge dd;
		Vertex vv;
		boolean did = false;
		while (itr.hasNext()) {
			did = false;
			d = itr.next();
			dd = (IEdge)d;
			vv = d.getVertex(Direction.IN);
			if (vv != null && vId.equals(vv.getId())) {
				dd.updateInVertex(v);
				did = true;
			}
			vv = d.getVertex(Direction.OUT);
			if (vv != null && vId.equals(vv.getId())) {
				dd.updateInVertex(v);
				did = true;
			}
			if (did)
				edges.update(d);
		}
	}
	*/
	
	public Iterable<Vertex> getVertices(final String key, final Object value) {
		//TODO
		return null;
	}
	
	/**
	 * <p>This is complex: must deal with its edges</p>
	 * @param v
	 */
	public void remove(Vertex v) {
        for (Edge edge : v.getEdges(Direction.BOTH)) {
            //this.removeEdge(edge);
        }
        //TODO delete this vertex
	}
	
	public long size() {
		IResult r = client.vertexTableSize();
		Object o = r.getResultObject();
		if (o != null)
			return ((Long)r.getResultObject()).longValue();
		else
			return -1;
	}
	
	public boolean containsKey(String id) {
		return (get(id) != null);
	}
	
	public List<Vertex> values() {
		List<Vertex> result = new ArrayList<Vertex>();
		//TODO
		return result;
	}
	
	public void clear() {
		throw new RuntimeException("VertexProvider.clear not implemented");
	}
}
