/**
 * 
 */
package org.topicquests.pg.persist;

import java.util.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.topicquests.pg.PostgreSqlProvider;
import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IDataProvider;
import org.topicquests.pg.api.IEdge;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.api.IVertex;
import org.topicquests.pg.blueprints.TinkerVertex;
import org.topicquests.pg.api.IBlueprintsSchema;
import org.topicquests.pg.api.IConstants;
import org.topicquests.support.ResultPojo;
import org.topicquests.support.api.IResult;
import org.topicquests.support.util.LRUCache;

import com.tinkerpop.blueprints.Vertex;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class PostgresDatabase extends PostgreSqlProvider implements IDataProvider {
	private IPostgresBlueprintsModel model;
	private LRUCache vCache;
	private LRUCache eCache;
	
	/**
	 * @param env
	 * @param dbUrl
	 * @param dbPort
	 * @param dbName
	 * @param userName
	 * @param pwd
	 */
	public PostgresDatabase(PostgresBlueprintsEnvironment env, String dbUrl, String dbPort, String dbName, String userName,
			String pwd) {
		super(dbName);
		vCache = new LRUCache(8192);
		eCache = new LRUCache(8192);
		validateDatabase(IBlueprintsSchema.TABLES);
	}

	/* (non-Javadoc)
	 * @see org.topicquests.pg.api.IDataProvider#tableSize(java.lang.String, java.lang.String)
	 */
	@Override
	public IResult tableSize(String tableName) {
		String sql = "SELECT COUNT(id) FROM "+tableName;
		return super.executeCount(sql);
	}

	@Override
	public IResult clearDatabase() {
		IResult result = new ResultPojo();
		Connection conn = null;
		Statement s = null;
		try {
			conn = super.getConnection();
			String[] sql = IBlueprintsSchema.TABLES;
			int len = sql.length;
			System.out.println("Clear-3a "+len);
			s = conn.createStatement();
			for (int i = 0; i < len; i++) {
				logDebug(sql[i]);
				System.out.println("EXPORTING "+sql[i]);
				s.execute(sql[i]);
			}
		} catch (Exception x) {
			logError(x.getMessage(), x);
			result.addErrorString(x.getMessage());
		} finally {
			try {
				s.close();
			} catch (Exception e) {
				logError(e.getMessage(), e);
				result.addErrorString(e.getMessage());				
			}
			super.closeConnection(conn, result);
		}
		return result;	
	}

	@Override
	public IResult putVertex(IVertex vertex) {
		IResult result = new ResultPojo();
		String id = (String)vertex.getId();
		String label = vertex.getLabel();
		String type = vertex.getType();
		List<String> inEdges = vertex.listAllInEdges();
		List<String> outEdges = vertex.listAllOutEdges();
		//The table wants creatorId and timestamps
		//We don't support those yet
		String creator="System";
		String ts = Long.toString(System.currentTimeMillis());
		String ets = ts;
		String version = ts;
		if (type == null) type = "";
		Map<String, Object>m = new HashMap<String,Object>();
		Set<String>keys = vertex.getPropertyKeys();
		Iterator<String>itr = keys.iterator();
		String key;
		//isolate node properties from id and label
		while (itr.hasNext()) {
			key = itr.next();
			if (isPropertyKey(key))
				m.put(key, vertex.getProperty(key));
		}
		Connection conn = null;
		String sql = IBlueprintsSchema.INSERT_VERTEX;
		String []vals = new String [] {id, creator, ts, ets, version, type, label};
		try {
			conn = super.getConnection();
			IResult r = super.executeSQL(conn, sql, vals);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			//now the edges
			sql = IBlueprintsSchema.INSERT_VERTEX_PROPERTY;
			vals = new String [3];
			vals[0] = id;
			int len;
			if (inEdges != null && !inEdges.isEmpty()) {
				vals[1] = IVertex.IN_EDGE;
				len = inEdges.size();
				for (int i=0;i<len;i++) {
					vals[2] = inEdges.get(i);
					r = super.executeSQL(conn, sql, vals);
					if (r.hasError())
						result.addErrorString(r.getErrorString());
				}
			}
			if (outEdges != null && !outEdges.isEmpty()) {
				vals[1] = IVertex.OUT_EDGE;
				len = outEdges.size();
				for (int i=0;i<len;i++) {
					vals[2] = outEdges.get(i);
					r = super.executeSQL(conn, sql, vals);
					if (r.hasError())
						result.addErrorString(r.getErrorString());
				}
			}
			logDebug("PostgresDatabase.putVertex "+m);
			if (m.size() > 0) {
				//Could be other properties than edges, label, id
				//The game is to craft a List<String> of SQL statements
				// and send them in to executeMultiSQL
				itr = m.keySet().iterator();
				Object valux;
				boolean isIn = false;
				boolean isOut = false; 
				List<String> idx;
				while (itr.hasNext()) {
					key = itr.next();
					valux = m.get(key);
					vals[1] = key;
					if (valux instanceof List) {
						idx = (List<String>)valux;
						len = idx.size();
						for (int i=0;i<len;i++) {
							vals[2] = idx.get(i);
							r = super.executeSQL(conn, sql, vals);
							if (r.hasError())
								result.addErrorString(r.getErrorString());							
						}
						
					} else {
						vals[2] = (String) valux;
						r = super.executeSQL(conn, sql, vals);
						if (r.hasError())
							result.addErrorString(r.getErrorString());
					}
				}
			}
		} catch (Exception e) {
			logError(e.getMessage(), e);
			result.addErrorString(e.getMessage());
		} finally {
			super.closeConnection(conn, result);
		}

		return result;
	}

	/**
	 * <p>Return <code>true</code> if <code>key</code> not an id or label field</p>
	 * <p>All others are considered key-value properties of a node</p>
	 * @param key
	 * @return
	 */
	boolean isPropertyKey(String key) {
		boolean truth = (!key.equals(IConstants.ID_FIELD) &&
						 !key.equals(IConstants.LABEL_FIELD) && 
						 !key.equals(IConstants.TYPE_FIELD) && 
						 !key.equals(IVertex.IN_EDGE) && 
						 !key.equals(IVertex.OUT_EDGE) &&
						 !key.equals(IEdge.IN_VERTEX) &&
						 !key.equals(IEdge.OUT_VERTEX));
		return truth;
	}


	@Override
	public IResult insertVertexProperty(String vId, String key, String value) {
		vCache.remove(vId);
		String sql = IBlueprintsSchema.INSERT_VERTEX_PROPERTY;
		String [] vals = new String [] {vId, key, value};
		logDebug("PostgresDatabase.insertVertexProperty "+vId+" | "+key+" | "+value);
		return super.executeSQL(sql, vals);
	}



	@Override
	public IResult updateSingleVertexProperty(String vId, String key, String newValue) {
		vCache.remove(vId);
		String sql = IBlueprintsSchema.UPDATE_SINGLE_VERTEX_PROPERTY;
		String [] vals = new String [] {newValue, vId, key};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult removeVertexProperty(String vId, String key, String value) {
		vCache.remove(vId);
		String sql = IBlueprintsSchema.REMOVE_VERTEX_PROPERTY;
		String [] vals = new String [] {vId, key, value};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult deleteVertexProperty(String vId, String key) {
		vCache.remove(vId);
		String sql = IBlueprintsSchema.DELETE_VERTEX_PROPERTY;
		String [] vals = new String [] {vId, key};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult updateVertexLabel(String vId, String newLabel) {
		vCache.remove(vId);
		String sql = IBlueprintsSchema.UPDATE_VERTEX_LABEL;
		String [] vals = new String [] {vId, newLabel};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult updateVertexEditTimestamp(String vId, long newTimestamp) {
		vCache.remove(vId);
		IResult result = new ResultPojo();
		//TODO
		return result;
	}



	@Override
	public IResult getVertex(String vId) {
		return _getVertex(vId, true, true);
	}
	
	IResult _getVertex(String vId, boolean isHydrate, boolean isLast) {
		IResult result = new ResultPojo();
		IVertex v = (IVertex)vCache.get(vId);
		logDebug("PostgresDatabase._get- "+vId+" "+v);
		if (v == null) {
			String sql = IBlueprintsSchema.GET_VERTEX;
			String [] vals = new String [] {vId};
			Connection conn = null;
			try {
				conn = super.getConnection();
				IResult r = super.executeSelect(conn, sql, vals);
				if (r.hasError())
					result.addErrorString(r.getErrorString());
				ResultSet rs = (ResultSet)r.getResultObject();
				if (rs != null) {
					try {
						if (rs.next()) {
							String id = rs.getString(IBlueprintsSchema.ID);
							String label = rs.getString(IBlueprintsSchema.LABEL);
							String type = rs.getString(IBlueprintsSchema.TYPE);
							v = (IVertex)model.newVertex(id, label, type, false);
							result.setResultObject(v);
						}
						closeResultSet(rs, result);
						rs = null;
						if (v != null) {
							//now fetch the rest of the properties, which might include edges
							sql = IBlueprintsSchema.LIST_VERTEX_PROPERTIES;
							r = super.executeSelect(conn, sql, vals);
							if (r.hasError())
								result.addErrorString(r.getErrorString());
							rs = (ResultSet)r.getResultObject();
							String key, vx;
							Map<String, Object> m = new HashMap<String,Object>();
							Object o;
							List<String>lx;
							if (rs != null) {
								while (rs.next()) {
									key = rs.getString(IBlueprintsSchema.KEY);
									vx = rs.getString(IBlueprintsSchema.VALUE);
									logDebug("_GET "+vId+" "+key+" "+vx);
									if (key.equals(IVertex.IN_EDGE))
										v.addInEdgeId(vx);
									else if (key.equals(IVertex.OUT_EDGE))
										v.addOutEdgeId(vx);
									else {
										if (m.containsKey(key)) {
											o = m.get(key);
											if (o instanceof List) {
												lx = (List<String>)o;
												lx.add(vx);
											} else {
												lx = new ArrayList<String>();
												lx.add((String)o);
												lx.add(vx);
											}
											v.setProperty(key, lx);
										} else {
											m.put(key, vx);
											//set it by default
											v.setProperty(key, vx);
										}
									}
									
								}
							}
							if (isHydrate) {
								hydrateVertex(v, conn, result);
							}
							result.setResultObject(v);
							vCache.add(vId, v);
						}
					} catch (Exception e) {
						logError(e.getMessage(), e);
						result.addErrorString(e.getMessage());
					} finally {
						closeResultSet(rs, result);
					}
				}
			} catch (Exception e) {
				logError(e.getMessage(), e);
				result.addErrorString(e.getLocalizedMessage());
			} finally {
				if (isLast)
					super.closeConnection(conn, result);
			}
		} else {
			result.setResultObject(v);
		}
		return result;		
	}

	private void hydrateVertex(IVertex v, Connection conn, IResult r) {
		//in edges first
		List<String> edges = v.listAllInEdges();
		Iterator<String>itr;
		String edgeId;
		IEdge edg;
		IResult rx;
		//inEdges mean v is the outVertex for the edge
		if (edges != null && !edges.isEmpty()) {
			itr = edges.iterator();
			while (itr.hasNext()) {
				edgeId = itr.next();
				rx = this.getEdge(conn, edgeId, v, false);
				if (rx.hasError())
					r.addErrorString(rx.getErrorString());
				edg = (IEdge)rx.getResultObject();
				if (edg != null)
					v.addInEdge(edg, true);
				else
					r.addErrorString("MISSING EDGE "+edgeId+" for vertex "+v.getId());
			}
		}
		//out edges
		edges = v.listAllOutEdges();
		if (edges != null && !edges.isEmpty()) {
			itr = edges.iterator();
			while (itr.hasNext()) {
				edgeId = itr.next();
				rx = this.getEdge(conn, edgeId, v, true);
				if (rx.hasError())
					r.addErrorString(rx.getErrorString());
				edg = (IEdge)rx.getResultObject();
				if (edg != null)
					v.addOutEdge(edg, true);
				else
					r.addErrorString("MISSING EDGE "+edgeId+" for vertex "+v.getId());
			}
		}
	}

	@Override
	public IResult hydrateVertex(IVertex vertex) {
		IResult result = new ResultPojo();
		//TODO
		return result;
	}



	@Override
	public IResult putEdge(IEdge edge) {
		IResult result = new ResultPojo();
		String id = (String)edge.getId();
		String label = edge.getLabel();
		if (label == null) label = "";
		//The table wants creatorId and timestamps
		//We don't support those yet
		String creator="System";
		String ts = Long.toString(System.currentTimeMillis());
		String ets = ts;
		String version = ts;
		String inVId = edge.getInVertexId();
		String outVId = edge.getOutVertexId();
		String sql = IBlueprintsSchema.INSERT_EDGE;
		String [] vals= new String [] {id, creator, ts, ets, version, inVId, outVId, label };
		Connection conn=null;
		try {
			conn = super.getConnection();
			//insert the edge
			IResult r = super.executeSQL(conn, sql, vals);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			//properties
			Map<String, Object>m = new HashMap<String,Object>();
			Set<String>keys = edge.getPropertyKeys();
			Iterator<String>itr = keys.iterator();
			String key;
			//isolate edge properties from id and label
			while (itr.hasNext()) {
				key = itr.next();
				if (isPropertyKey(key))
					m.put(key, edge.getProperty(key));
			}
			logDebug("PostgresDatabase.putEdge "+m);
			if (m.size() > 0) {
				sql = IBlueprintsSchema.INSERT_EDGE_PROPERTY;
				vals = new String [3];
				vals[0] = id;
				int len;
				//Could be other properties than edges, label, id
				//The game is to craft a List<String> of SQL statements
				// and send them in to executeMultiSQL
				itr = m.keySet().iterator();
				Object valux;
				List<String> idx;
				while (itr.hasNext()) {
					key = itr.next();
					valux = m.get(key);
					vals[1] = key;
					if (valux instanceof List) {
						idx = (List<String>)valux;
						len = idx.size();
						for (int i=0;i<len;i++) {
							vals[2] = idx.get(i);
							r = super.executeSQL(conn, sql, vals);
							if (r.hasError())
								result.addErrorString(r.getErrorString());							
						}
						
					} else {
						vals[2] = (String) valux;
						r = super.executeSQL(conn, sql, vals);
						if (r.hasError())
							result.addErrorString(r.getErrorString());
					}
				}
			}
		} catch (Exception e) {
			logError(e.getMessage(), e);
			result.addErrorString(e.getMessage());
		} finally {
			super.closeConnection(conn, result);
		}
		return result;
	}



	@Override
	public IResult insertEdgeProperty(String eId, String key, String value) {
		eCache.remove(eId);
		String sql = IBlueprintsSchema.INSERT_EDGE_PROPERTY;
		String [] vals = new String [] {eId, key, value};
		logDebug("PostgresDatabase.insertEdgeProperty "+eId+" | "+key+" | "+value);
		return super.executeSQL(sql, vals);
	}



	@Override
	public IResult updateSingleEdgeProperty(String eId, String key, String newValue) {
		eCache.remove(eId);
		String sql = IBlueprintsSchema.UPDATE_SINGLE_EDGE_PROPERTY;
		String [] vals = new String [] {newValue, eId, key};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult removeEdgeProperty(String eId, String key, String value) {
		eCache.remove(eId);
		String sql = IBlueprintsSchema.REMOVE_EDGE_PROPERTY;
		String [] vals = new String [] {eId, key, value};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult deleteEdgeProperty(String eId, String key) {
		eCache.remove(eId);
		String sql = IBlueprintsSchema.DELETE_EDGE_PROPERTY;
		String [] vals = new String [] {eId, key};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult updateEdgeLabel(String eId, String newLabel) {
		eCache.remove(eId);
		String sql = IBlueprintsSchema.UPDATE_EDGE_LABEL;
		String [] vals = new String [] {eId, newLabel};
		return super.executeUpdate(sql, vals);
	}



	@Override
	public IResult updateEdgeEditTimestamp(String vId, long newTimestamp) {
		IResult result = new ResultPojo();
		//TODO
		return result;
	}



	@Override
	public IResult getEdge(String eId) {
		IResult result = new ResultPojo();
		IEdge e = (IEdge)eCache.get(eId);
		logDebug("PostgresDatabase.getEdge- "+eId+" "+e);
		if (e != null) {
			result.setResultObject(e);
			return result;
		}
		Connection conn=null;
		ResultSet rs = null;
		try {
			conn = super.getConnection();
			e = this._getEdge(conn, eId, result);
			if (e == null)
				return result;
			result.setResultObject(e);
			String outVId = e.getOutVertexId();
			String inVId = e.getInVertexId();
			IResult r = this._getVertex(outVId, false, false);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			e.setOutVertex((Vertex)r.getResultObject());
			r = this._getVertex(inVId, false, false);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			e.setInVertex((Vertex)r.getResultObject());
			System.out.println("PostgresDatabase.getVertex-");
			System.out.println(((JSONObject)e).toJSONString());
			eCache.add(eId, e);
		} catch (Exception x) {
			logError(x.getMessage(), x);
			result.addErrorString(x.getMessage());
		} finally {
			super.closeConnection(conn, result);
		}
		return result;
	}

	private IEdge _getEdge(Connection conn, String eId, IResult rx) {
		IEdge result = null;
		ResultSet rs = null;
		String sql = IBlueprintsSchema.GET_EDGE;
		String [] vals = new String [] {eId};
		IResult r = super.executeSelect(conn, sql, vals);
		if (r.hasError())
			rx.addErrorString(r.getErrorString());
		rs = (ResultSet)r.getResultObject();
		logDebug("PostgresDatabase._getEdge- "+sql+" "+rs);
		if (rs != null) {
			try {
				IEdge e = null;
				String id=null, label=null, inId=null, outId=null;
				boolean go = rs.next();
				logDebug("PostgresDatabase._getEdge-a "+eId+" "+go);
				if (go) {
					id = rs.getString(IBlueprintsSchema.ID);
					label = rs.getString(IBlueprintsSchema.LABEL);
					inId = rs.getString(IBlueprintsSchema.IN_VERT_ID);
					outId = rs.getString(IBlueprintsSchema.OUT_VERT_ID);
					logDebug("PostgresDatabase._getEdge2 "+id+" "+label);
					result = (IEdge)model.newEdgeShell(id, null, null, label);
					result.setInVertexId(inId);
					result.setOutVertexId(outId);
				}
				super.closeResultSet(rs, rx);
				rs = null;

				//Apparently, result.toString crashes
				logDebug("PostgresDatabase._getEdge-3 "+(result == null));

				//fill in the edge properties
				if (result != null) {
					sql = IBlueprintsSchema.LIST_EDGE_PROPERTIES;
					r = super.executeSelect(conn, sql, vals);
					if (r.hasError())
						rx.addErrorString(r.getErrorString());
					rs = (ResultSet)r.getResultObject();
					String key, vx;
					Map<String, Object> m = new HashMap<String,Object>();
					Object o;
					List<String>lx;
					if (rs != null) {
						while (rs.next()) {
							key = rs.getString(IBlueprintsSchema.KEY);
							vx = rs.getString(IBlueprintsSchema.VALUE);
							if (m.containsKey(key)) {
								o = m.get(key);
								if (o instanceof List) {
									lx = (List<String>)o;
									lx.add(vx);
								} else {
									lx = new ArrayList<String>();
									lx.add((String)o);
									lx.add(vx);
								}
								result.setProperty(key, lx);
							} else {
								m.put(key, vx);
								//set it by default
								result.setProperty(key, vx);
							}						
						}
					}
				}
				
			} catch (Exception e) {
				logError(e.getMessage(), e);
				rx.addErrorString(e.getMessage());
			} finally {
				super.closeResultSet(rs, rx);
			}
		}
		logDebug("PostgresDatabase._getEdge+ "+(result == null));
		return result;
	}
	/** NEVER CALLED
	 * <p>Fetch an IEdge with a given vertex</p>
	 * <p>This entails a recursive call to getVertex without hydrating</p>
	 * @param conn
	 * @param eId
	 * @param vertex
	 * @param isInVertex either <code>vertex</code> is the in or the out vertex
	 * @return
	 */
	private IResult getEdge(Connection conn, String eId, IVertex vertex, boolean isInVertex) {
		IResult result = new ResultPojo();
		IEdge e = (IEdge)eCache.get(eId);
		logDebug("postgresDatabase.getEEdge- "+eId+" "+e);
		//TODO this iz NEVER called
		if (e != null) {
			result.setResultObject(e);
			return result;
		}
		ResultSet rs = null;
		String sql;
		String [] vals = new String [] {eId};
		e = this._getEdge(conn, eId, result);
		if (e == null)
			return result;
		else
			result.setResultObject(e);
		IResult r;
		IVertex inV=null;
		IVertex outV=null;
		if (isInVertex)
			inV = vertex;
		else
			outV = vertex;
		String theVertId = e.getInVertexId();
		if (isInVertex)
			theVertId = e.getOutVertexId();
		if (theVertId != null) {
			r = this._getVertex(theVertId, false, false);
			if (r.hasError())
				result.addErrorString(r.getErrorString());
			IVertex x = (IVertex)r.getResultObject();
			if (x != null) {
				if (isInVertex)
					outV = x;
				else
					inV = x;
			} else {
				//TODO error
			}
			e.setInVertex(inV);
			e.setOutVertex(outV);
			eCache.add(eId, e);
		}
		return result;
	}
	


	@Override
	public IResult hydrateEdge(IVertex edge) {
		IResult result = new ResultPojo();
		//TODO
		return result;
	}



	@Override
	public void setModel(IPostgresBlueprintsModel m) {
		model = m;
	}


/*
	@Override
	public IResult updateRowData(String tableName, String rowID, JSONObject value) {
		IResult result = new ResultPojo();
		//TODO
		return result;
	}
*/
}
