/**
 * 
 */
package devtests;

import org.topicquests.pg.PostgresBlueprintsEnvironment;
import org.topicquests.pg.api.IPostgresBlueprintsModel;
import org.topicquests.pg.blueprints.TinkerGraph;
import org.topicquests.support.api.IResult;

/**
 * @author jackpark
 *
 */
public class ClearDatabase {
	private PostgresBlueprintsEnvironment environment;
	private static final String 
		GRAPH_NAME 	= "blueprints3"; //TODO THIS IS IMPORTANT!
	/**
	 * 
	 */
	public ClearDatabase() {
		System.out.println("Starting");
		environment = new PostgresBlueprintsEnvironment();
		TinkerGraph g = environment.getGraph(GRAPH_NAME);
		IPostgresBlueprintsModel model = g.getModel();
		IResult r = model.clearDatabase();
		System.out.println(r.getErrorString()+" | "+g);
		environment.shutDown();
		System.out.println("Did");
		System.exit(0);
	}

}
