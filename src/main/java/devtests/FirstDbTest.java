/**
 * 
 */
package devtests;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.blueprints.TinkerGraph;

/**
 * @author jackpark
 *
 */
public class FirstDbTest {
	private PostgresBlueprintsEnvironment environment;
	private static final String GRAPH_NAME = "blueprints3";

	/**
	 * 
	 */
	public FirstDbTest() {
		System.out.println("Starting");
		environment = new PostgresBlueprintsEnvironment();
		TinkerGraph g = environment.getGraph(GRAPH_NAME);
		System.out.println("A "+g);
		environment.shutDown();
		System.out.println("Did");
		System.exit(0);
	}
//A tinkergraph[vertices:0 edges:0]
}
