/**
 * 
 */
package devtests;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.blueprints.TinkerGraph;

import com.tinkerpop.blueprints.Vertex;

import net.minidev.json.JSONObject;

/**
 * @author jackpark
 *
 */
public class FirstVertexTest {
	private PostgresBlueprintsEnvironment environment;
	private static final String 
		GRAPH_NAME 	= "blueprints3",
		VERTEX_ID	= "a123",
		VERTEX_TYPE	= "TestType";

	/**
	 * 
	 */
	public FirstVertexTest() {
		System.out.println("Starting");
		environment = new PostgresBlueprintsEnvironment();
		TinkerGraph g = environment.getGraph(GRAPH_NAME);
		IPostgresBlueprintsModel model = g.getModel();
		Vertex v = model.newVertex(VERTEX_ID, "My First Vertex", VERTEX_TYPE, true);
		
		System.out.println("A "+((JSONObject)v).toJSONString());
		System.out.println("B "+g);
		environment.shutDown();
		System.out.println("Did");
		System.exit(0);
	}
//A {"inE":{},"outE":{},"id":"123","label":"My First Vertex","type":"TestType"}
//B tinkergraph[vertices:1 edges:0]
//A {"inEID":{},"outEID":{},"id":"123","label":"My First Vertex","type":"TestType"}
//B tinkergraph[vertices:-1 edges:-1] <-- counting not working

}
