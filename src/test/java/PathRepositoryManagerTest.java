
import nl.tudelft.ewi.gitolite.repositories.PathRepositoriesManager;
import nl.tudelft.ewi.gitolite.repositories.Repository;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class PathRepositoryManagerTest {

	@Test
	@Ignore("Heavy")
	public void test() throws IOException {
		PathRepositoriesManager manager = new PathRepositoriesManager(new File("/private/etc/git-server/repositories"));

		for(Repository repository : manager.getRepositories()) {
			System.out.printf("%s - %s\n", repository.getURI(), repository.getSize());
		}
	}

}
