/**
 * 
 */
package devtests;

import org.topicquests.pg.PostgresBlueprintsEnvironment;

/**
 * @author jackpark
 * Note: PostgreSQL must be running
 */
public class BootTest {
	private PostgresBlueprintsEnvironment environment;
	/**
	 * 
	 */
	public BootTest() {
		System.out.println("Starting");
		environment = new PostgresBlueprintsEnvironment();
		System.out.println("A "+environment.getProperties());
		environment.shutDown();
		System.out.println("Did");
		System.exit(0);
	}

}
