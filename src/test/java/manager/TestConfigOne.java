package manager;

import lombok.SneakyThrows;
import nl.tudelft.ewi.gitolite.ManagedConfig;
import nl.tudelft.ewi.gitolite.ManagedConfigFactory;
import nl.tudelft.ewi.gitolite.config.Config;
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

	@SneakyThrows
	protected ManagedConfig initManagedConfig() {
		return managedConfigFactory.init("mocked-gitolite-admin");
	}

	private static void noop(Object obj){}

	@Test
	public void parseAndStoreConfig() throws IOException, InterruptedException {
		managedConfig.writeConfig(TestConfigOne::noop);
		verifyGitoliteAdminPush();

		managedConfig.readConfig(config ->
			initManagedConfig().readConfig(config2 -> {
				assertEquals(config.getRules(), config2.getRules());
				writeConfig(config);
			})
		);
	}

	@SneakyThrows
	private static void writeConfig(Config config) {
		config.write(System.out);
	}


	@Test
	public void parseAndStoreAlteredConfig() throws IOException, InterruptedException {
		managedConfig.readConfig(config -> {
			Collection<GroupRule> TI1706 = Collections.singleton(config.getGroup("@staff"));
			Collection<Identifier> GIT = Collections.singleton(Identifier.valueOf("git"));

			RepositoryRule repositoryRule = RepositoryRule.builder()
				.identifiable(Identifier.valueOf("FOSS/..*"))
				.rule(new AccessRule(BasePermission.RW_PLUS, TI1706, GIT))
				.build();

			// Update to write lock, push changes...
			managedConfig.writeConfig(writeConfig ->
				writeConfig.addRepositoryRule(repositoryRule));

			verifyGitoliteAdminPush();

			initManagedConfig().readConfig(config2 -> {
				assertEquals(config.getRules(), config2.getRules());
				assertThat(config2.getRules(), Matchers.hasItem(repositoryRule));
				writeConfig(config2);
			});
		});
	}

	@SneakyThrows
	void verifyGitoliteAdminPush() {
		verify(gitManager).commitChanges();
		verify(gitManager).push();
	}

}
