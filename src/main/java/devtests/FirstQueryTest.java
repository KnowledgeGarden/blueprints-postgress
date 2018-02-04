/**
 * 
 */
package devtests;

import java.util.Iterator;
import java.util.List;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.blueprints.TinkerGraph;
import org.topicquests.support.api.IResult;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class FirstQueryTest {
	private PostgresBlueprintsEnvironment environment;
	private static final String 
		GRAPH_NAME 	= "blueprints3",
		VERTEX_ID_1	= Long.toString(System.currentTimeMillis()),
		VERTEX_ID_2	= Long.toString(System.currentTimeMillis()+2),
		VERTEX_ID_3	= Long.toString(System.currentTimeMillis()+4),
//		EDGE_ID_1		= Long.toString(System.currentTimeMillis()+6),
		VERTEX_TYPE	= "TestType";

	/**
	 * 
	 */
	public FirstQueryTest() {
		System.out.println("Starting");
		environment = new PostgresBlueprintsEnvironment();
		TinkerGraph g = environment.getGraph(GRAPH_NAME);
		IPostgresBlueprintsModel model = g.getModel();
		//build the vertices
		Vertex v1 = model.newVertex(VERTEX_ID_1, "My First Vertex", VERTEX_TYPE, true);
		Vertex v2 = model.newVertex(VERTEX_ID_2, "My Second Vertex", VERTEX_TYPE, true);
		Vertex v3 = model.newVertex(VERTEX_ID_3, "My Third Vertex", VERTEX_TYPE, true);
		//connect them
		Edge e1 = model.connectVertices(null, v1, v2, "likes");
		IResult r = model.getVertex(VERTEX_ID_1);
		v1 = (Vertex)r.getResultObject();
		System.out.println("A "+((JSONObject)v1).toJSONString());
//A {"outEID":["860aaf2e-139b-44b3-b570-8abb6db52118"],"id":"1507913675558","label":"My First Vertex","type":"TestType"}

		System.out.println("B "+((JSONObject)e1).toJSONString());
//B {"inID":"1507913675558","outID":"1507913675560","id":"860aaf2e-139b-44b3-b570-8abb6db52118","label":"likes"}

		Edge e2 = model.connectVertices(null, v2, v3, "causes");
		System.out.println("C "+((JSONObject)e1).toJSONString());
//C {"inID":"1507913675558","outID":"1507913675560","id":"860aaf2e-139b-44b3-b570-8abb6db52118","label":"likes"}

		r = model.getVertex(VERTEX_ID_2);
		v2 = (Vertex)r.getResultObject();
		System.out.println("D "+((JSONObject)v2).toJSONString());
//D {"inEID":["860aaf2e-139b-44b3-b570-8abb6db52118"],"outEID":["d162d647-48e6-4fcc-81c5-cdfb38cea4ab"],"id":"1507913675560","label":"My Second Vertex","type":"TestType"}

		VertexQuery vq = v1.query();
		
		String [] labels = new String [] {"likes"};
		vq = vq.direction(Direction.OUT).labels(labels);
		System.out.println("E "+vq.count());
//E 1		
		Iterable<Edge> itx = vq.edges();
		Iterator<Edge> itr = itx.iterator();
		Edge edx=null;
		if (itr.hasNext())
			edx = itr.next();
// v1 out -- likes  -- v2 -- out -- causes -- v3
		System.out.println("E1 "+((JSONObject)edx).toJSONString());
//E1 {"inID":"1507913675558","outID":"1507913675560","id":"860aaf2e-139b-44b3-b570-8abb6db52118","label":"likes"}

		v2 = edx.getVertex(Direction.OUT);

		System.out.println("F "+((JSONObject)v2).toJSONString());
//F {"inEID":["860aaf2e-139b-44b3-b570-8abb6db52118"],"id":"1507913675560","label":"My Second Vertex","type":"TestType"}
//MISSING outEID
//NOTICE difference between D and F
//outID is in the database; failed to load it
//TinkerVertex line 134 coming in null; -- not loading it properly
		vq = v2.query();
		labels = new String [] {"causes"};
		vq = vq.direction(Direction.OUT).labels(labels);
		System.out.println("G "+vq.count());
		itx = vq.edges();
		itr = itx.iterator();
		if (itr.hasNext())
			edx = itr.next();
		System.out.println("G1 "+((JSONObject)edx).toJSONString());
//G1 {"inID":"1507765395915","outID":"1507765395917","id":"d82892f6-9215-4833-9b47-a89d6bbb2030","label":"likes"}
//WRONG
		v3 = edx.getVertex(Direction.OUT);
		System.out.println("H "+((JSONObject)v3).toJSONString());
//H {"inEID":["0cf79ab6-1136-4b03-b597-b4a6d452d6b2"],"id":"1507765240168","label":"My Second Vertex","type":"TestType"}
//should be My third vertex
		System.out.println("M "+g);
		environment.shutDown();
		System.out.println("Did");
		System.exit(0);

	}

}
