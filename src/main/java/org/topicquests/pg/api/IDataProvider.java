/**
 * 
 */
package org.topicquests.pg.api;

import org.topicquests.support.api.IResult;
import org.topicquests.pg.api.IEdge;
import org.topicquests.pg.api.IVertex;

/**
 * @author jackpark
 *
 */
public interface IDataProvider extends 	IPostgreSqlProvider {
	
	void setModel(IPostgresBlueprintsModel m);
	
	IResult putVertex(IVertex vertex);
	IResult insertVertexProperty(String vId, String key, String value);
	IResult updateSingleVertexProperty(String vId, String key, String newValue);
	IResult removeVertexProperty(String vId, String key, String value);
	IResult deleteVertexProperty(String vId, String key);
	IResult updateVertexLabel(String vId, String newLabel);
	IResult updateVertexEditTimestamp(String vId, long newTimestamp);
	IResult getVertex(String vId);
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
	
	/*IResult insertRowData(String tableName, String rowID, JSONObject value);
	
	IResult removeRowData(String tableName, String rowID);*/
	
	IResult tableSize(String tableName);
	
	//IResult updateRowData(String tableName, String rowID, JSONObject value);
	
	//IResult get(String tableName, String rowId);
	
	/**
	 * Very dangerous
	 * @return
	 */
	IResult clearDatabase();
}
