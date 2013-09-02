package nl.minicom.gitolite.manager.integration;

import java.io.IOException;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Strings;

public class IntegrationTest {

	private ConfigManager manager;
	
	@Before
	public void setUp() throws IOException, ServiceUnavailable, ModificationException {
		String gitUri = System.getProperty("gitUri");
		boolean runTests = !Strings.isNullOrEmpty(gitUri);
		Assume.assumeTrue(runTests);
		
		manager = ConfigManager.create(gitUri);
		clearEverything();
	}

	@After
	public void tearDown() throws IOException, ServiceUnavailable, ModificationException {
		clearEverything();
	}
	
	private void clearEverything() throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.loadConfig();

		for (User user : config.getUsers()) {
			if (!"git".equals(user.getName())) {
				config.removeUser(user);
			}
		}
		
		for (Group group : config.getGroups()) {
			config.removeGroup(group);
		}
		
		for (Repository repo : config.getRepositories()) {
			if (!"gitolite-admin".equals(repo.getName())) {
				config.removeRepository(repo);
			}
		}
		
		manager.applyChanges(config);
	}
	
	@Test
	public void testSequentialRepositoryModification() throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.loadConfig();
		config.createRepository("test-repo");
		manager.applyChanges(config);
		
		config = manager.loadConfig();
		Repository repository = config.getRepository("test-repo");
		config.removeRepository(repository);
		manager.applyChanges(config);
		
		config = manager.loadConfig();
		Assert.assertNull(config.getRepository("test-repo"));
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentRepositoryCreation() throws IOException, ServiceUnavailable, ModificationException {
		Config config1 = manager.loadConfig();
		Config config2 = manager.loadConfig();
		
		config1.createRepository("test-repo");
		config2.createRepository("test-repo");
		
		manager.applyChanges(config1);
		manager.applyChanges(config2);
	}
	
	@Test(expected = ModificationException.class)
	public void testConcurrentRepositoryRemoval() throws IOException, ServiceUnavailable, ModificationException {
		Config config = manager.loadConfig();
		config.createRepository("test-repo");
		manager.applyChanges(config);
		
		Config config1 = manager.loadConfig();
		Config config2 = manager.loadConfig();
		
		config1.removeRepository(config1.getRepository("test-repo"));
		config2.removeRepository(config2.getRepository("test-repo"));
		
		manager.applyChanges(config1);
		manager.applyChanges(config2);
	}
	
}
