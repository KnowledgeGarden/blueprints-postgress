/**
 * 
 */
package devtests;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.blueprints.TinkerGraph;
import org.topicquests.support.api.IResult;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class FirstComplexTest {
	private PostgresBlueprintsEnvironment environment;
	private static final String 
		GRAPH_NAME 	= "blueprints3",
		VERTEX_ID_1	= "a123",
		VERTEX_ID_2	= Long.toString(System.currentTimeMillis()),
		EDGE_ID		= Long.toString(System.currentTimeMillis()),
		VERTEX_TYPE	= "TestType";
	/**
	 * 
	 */
	public FirstComplexTest() {
		System.out.println("Starting");
		environment = new PostgresBlueprintsEnvironment();
		TinkerGraph g = environment.getGraph(GRAPH_NAME);
		IPostgresBlueprintsModel model = g.getModel();
		Vertex v = model.newVertex(VERTEX_ID_2, "My Second Vertex", VERTEX_TYPE, true);
		System.out.println("A "+((JSONObject)v).toJSONString());
		IResult r = model.getVertex(VERTEX_ID_1);
		if (r.getResultObject() != null) {
			Vertex from = (Vertex)r.getResultObject();
			System.out.println("B "+((JSONObject)from).toJSONString());
			Edge e = model.connectVertices(EDGE_ID, from, v, "likes");
			System.out.println("C "+((JSONObject)e).toJSONString());
			r = model.getVertex(VERTEX_ID_1);
			v = (Vertex)r.getResultObject();
			System.out.println("D "+((JSONObject)v).toJSONString());
			r = model.getVertex(VERTEX_ID_2);
			v = (Vertex)r.getResultObject();
			System.out.println("E "+((JSONObject)v).toJSONString());
			r = model.getEdge(EDGE_ID);
			e = (Edge)r.getResultObject();
			if (e != null)
				System.out.println("F "+((JSONObject)e).toJSONString());
			System.out.println("G "+g);
		}
		environment.shutDown();
		System.out.println("Did");
		System.exit(0);
	}

}
//A {"inE":{},"outE":{},"id":"464","label":"My Second Vertex","type":"TestType"}
//B {"inE":{},"outEID":["998999"],"outE":{},"id":"123","label":"My First Vertex","type":"TestType"}
//C {"inV":{"inE":{},"inEID":["998899"],"outE":{},"id":"464","label":"My Second Vertex","type":"TestType"},"inID":"464","outID":"123","id":"998899","label":"likes"}
//D {"inE":{},"outEID":["998999","998899"],"outE":{},"id":"123","label":"My First Vertex","type":"TestType"}
//E {"inE":{},"inEID":["998899"],"outE":{},"id":"464","label":"My Second Vertex","type":"TestType"}
//F tinkergraph[vertices:8 edges:5]

//D {"outEID":["1507675171011","1507675370671"],"id":"a123","label":"My First Vertex","type":"TestType"}
//E {"inEID":["1507675370671"],"id":"1507675370671","label":"My Second Vertex","type":"TestType"}
//F {"inID":"1507675370671","outID":"a123","id":"1507675370671","label":"likes"}
//G tinkergraph[vertices:3 edges:2]
