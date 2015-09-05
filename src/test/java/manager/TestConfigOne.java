package manager;

import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.ManagedConfigFactory;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.git.GitManagerFactory;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.permission.BasePermission;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class TestConfigOne implements GitManagerFactory {

	@Mock private GitManager gitManager;
	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	private ManagedConfigFactory managedConfigFactory;
	private ManagedConfig managedConfig;

	@Override
	public GitManager create(File repositoryFolder) {
		when(gitManager.getWorkingDirectory()).thenReturn(repositoryFolder);
		return gitManager;
	}

	@Before
	public void setUp() throws IOException, InterruptedException {
		FileUtils.copyDirectory(new File("src/test/resources/gitolite-admin-1"), temporaryFolder.getRoot());
		managedConfigFactory = new ManagedConfigFactory()
			.gitManagerFactory(this)
			.repositoryFolder(temporaryFolder.getRoot());
		managedConfig = initManagedConfig();
	}

	protected ManagedConfig initManagedConfig() throws IOException, InterruptedException {
		return managedConfigFactory.init("mocked-gitolite-admin");
	}

	@Test
	public void parseAndStoreConfig() throws IOException, InterruptedException {
		managedConfig.applyChanges();
		verify(gitManager).commitChanges();
		verify(gitManager).push();

		ManagedConfig pass2 = initManagedConfig();
		assertEquals(managedConfig.getRules(), pass2.getRules());
		managedConfig.write(System.out);
	}

	@Test
	public void parseAndStoreAlteredConfig() throws IOException, InterruptedException {
		Collection<GroupRule> TI1706 = Collections.singleton(managedConfig.getGroup("@staff"));
		Collection<Identifier> GIT = Collections.singleton(new Identifier("git"));

		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(new Identifier("FOSS/..*"))
			.rule(new AccessRule(BasePermission.RW_PLUS, TI1706, GIT))
			.build();

		managedConfig.addRepositoryRule(repositoryRule);

		managedConfig.applyChanges();
		verify(gitManager).commitChanges();
		verify(gitManager).push();

		ManagedConfig pass2 = initManagedConfig();
		assertEquals(managedConfig.getRules(), pass2.getRules());
		assertThat(pass2.getRules(), Matchers.hasItem(repositoryRule));
		pass2.write(System.out);
	}

}
