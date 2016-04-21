
import com.google.common.io.Files;
import nl.tudelft.ewi.gitolite.repositories.PathRepositoriesManager;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.empty;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class PathRepositoryManagerTest {


	public static final String TEST_01 = "test-01";
	public static final String TEST_01_GIT = "test-01.git/";
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	PathRepositoriesManager pathRepositoriesManager;

	@Before
	public void setUpRepository() throws GitAPIException, IOException, URISyntaxException {
		fillBareRepository(setUpBareRepository(TEST_01));
		pathRepositoriesManager = new PathRepositoriesManager(temporaryFolder.getRoot());

	}

	@Test
	public void getRepository() throws URISyntaxException {
		assertNotNull(
			pathRepositoriesManager.getRepository(new URI(TEST_01_GIT))
		);
	}

	@Test
	public void computeRepositorySize() throws URISyntaxException, IOException {
		assertNotNull(
			pathRepositoriesManager.getRepository(new URI(TEST_01_GIT)).getSize()
		);
	}

	@Test
	public void testRemoveRepository() throws URISyntaxException, IOException {
		pathRepositoriesManager.getRepository(new URI(TEST_01_GIT)).delete();

		assertThat(
			temporaryFolder.getRoot().listFiles(),
			emptyArray()
		);

		assertThat(
			pathRepositoriesManager.getRepositories(),
			empty()
		);
	}

	private File setUpBareRepository(String name) throws GitAPIException {
		File repoFolder = new File(temporaryFolder.getRoot(), name + ".git");
		Git.init()
			.setDirectory(repoFolder)
			.setBare(true)
			.call();
		return repoFolder;
	}

	private void fillBareRepository(File remote) throws IOException, GitAPIException, URISyntaxException {
		File repoFolder = Files.createTempDir();
		Git git = Git.init().setDirectory(repoFolder).setBare(false).call();

		File README = new File(repoFolder, "README.md");
		Files.write("Hello world".getBytes(), README);
		git.add().addFilepattern("README.md").call();
		git.commit().setMessage("Initial commit").call();

		git.push().setRemote(remote.getAbsolutePath()).call();
		FileUtils.deleteDirectory(repoFolder);
	}

}
