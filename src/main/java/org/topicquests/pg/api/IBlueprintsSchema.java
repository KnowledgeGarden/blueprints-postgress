/**
 * 
 */
package org.topicquests.pg.api;

/**
 * @author jackpark
 * Defining core nodes + key-value properties
 */
public interface IBlueprintsSchema {
	
	/**
	 * Table names
	 */
	public static final String 
		VERTEX_TABLE	= "vertex",
		VERTEX_PROPS	= "vprops",
		EDGE_TABLE		= "edge",
		EDGE_PROPS		= "eprops";
	
	/**
	 * Main table keys
	 */
	public static final String
		ID				= "id",
		LABEL			= "label",
		CREATOR_ID		= "creatorid",
		TIMESTAMP		= "timestamp",
		EDIT_TIMESTAMP	= "edittimestamp",
		TYPE			= "type",
		VERSION			= "version",
		IN_VERT_ID		= "invertexid",
		OUT_VERT_ID		= "outvertexid",
		KEY				= "key",
		VALUE			= "val";
	/**
	 * Table definitions
	 */
	public static final String[] TABLES = {
		//remove table dependencies before tables
		//	"DROP INDEX IF EXISTS vid",
		//	"DROP INDEX IF EXISTS eid",
		//	"DROP INDEX IF EXISTS vpid",
		//	"DROP INDEX IF EXISTS epid",
		"DROP TABLE IF EXISTS public.vertex CASCADE",
		"DROP TABLE IF EXISTS public.edge CASCADE",
		"DROP TABLE IF EXISTS public.vprops CASCADE",
		"DROP TABLE IF EXISTS public.eprops CASCADE",
		"CREATE TABLE vertex ("
				+ "id VARCHAR(64) PRIMARY KEY,"
				+ "creatorid VARCHAR(64) NOT NULL,"
				+ "timestamp VARCHAR(32) NOT NULL," //technically, long numbers
				+ "edittimestamp VARCHAR(32) NOT NULL," //long
				+ "version VARCHAR(32) NOT NULL," //long
				+ "type VARCHAR(32) DEFAULT '',"
				+ "label VARCHAR(255) DEFAULT '')", 
		"CREATE TABLE vprops ("
				+ "id VARCHAR(64) NOT NULL,"
				+ "key VARCHAR(32) NOT NULL,"
				+ "val VARCHAR(255) NOT NULL)", 
		"CREATE TABLE edge ("
				+ "id VARCHAR(64) PRIMARY KEY,"
				+ "creatorid VARCHAR(64) NOT NULL,"
				+ "timestamp VARCHAR(32) NOT NULL,"
				+ "edittimestamp VARCHAR(32) NOT NULL," //long
				+ "version VARCHAR(32) NOT NULL,"
				+ "invertexid VARCHAR(64) NOT NULL,"
				+ "outvertexid VARCHAR(64) NOT NULL,"
				+ "label VARCHAR(255) DEFAULT '')", 
		"CREATE TABLE eprops ("
				+ "id VARCHAR(64) NOT NULL,"
				+ "key VARCHAR(32) NOT NULL,"
				+ "val VARCHAR(255) NOT NULL)", 
				
		"CREATE UNIQUE INDEX vid ON vertex(id)",
		"CREATE UNIQUE INDEX eid ON edge(id)",
		"CREATE INDEX vpid ON vprops(id)",
		"CREATE INDEX epid ON eprops(id)"
	};
	
	/**
	 * Queries
	 */
	public static final String INSERT_VERTEX =
			"INSERT INTO vertex VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	public static final String INSERT_VERTEX_PROPERTY =
			"INSERT INTO vprops VALUES (?, ?, ?)";

	public static final String INSERT_EDGE =
			"INSERT INTO edge VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	
	public static final String INSERT_EDGE_PROPERTY =
			"INSERT INTO eprops VALUES (?, ?, ?)";

	public static final String GET_VERTEX =
			"SELECT * FROM vertex WHERE id=?";
	
	public static final String LIST_VERTEX_PROPERTIES =
			"SELECT * FROM vprops WHERE id=?";
	
	public static final String GET_EDGE =
			"SELECT * FROM edge WHERE id=?";
	
	public static final String LIST_EDGE_PROPERTIES =
			"SELECT * FROM eprops WHERE id=?";

	public static final String UPDATE_SINGLE_EDGE_PROPERTY =
			"UPDATE edge SET val=? WHERE id=? AND key=?";

	public static final String UPDATE_SINGLE_VERTEX_PROPERTY =
			"UPDATE vprops SET val=? WHERE id=? AND key=?";

	public static final String REMOVE_VERTEX_PROPERTY = 
			"DELEGE FROM vprops where id=? AND key=? AND val=?";

	public static final String REMOVE_EDGE_PROPERTY = 
			"DELEGE FROM eprops where id=? AND key=? AND val=?";

	public static final String DELETE_VERTEX_PROPERTY = 
			"DELEGE FROM vprops where id=? AND key=?";

	public static final String DELETE_EDGE_PROPERTY = 
			"DELEGE FROM eprops where id=? AND key=?";
	
	public static final String UPDATE_VERTEX_LABEL =
			"UPDATE vertex SET label=? where id=?";

	public static final String UPDATE_EDGE_LABEL =
			"UPDATE edge SET label=? where id=?";

}
