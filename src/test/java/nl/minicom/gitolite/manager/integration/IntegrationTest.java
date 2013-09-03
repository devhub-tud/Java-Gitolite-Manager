package nl.minicom.gitolite.manager.integration;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

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
	public void setUp() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		String gitUri = System.getProperty("gitUri");
		boolean runTests = !Strings.isNullOrEmpty(gitUri);
		Assume.assumeTrue(runTests);
		
		manager = ConfigManager.create(gitUri);
		clearEverything();
	}

	@After
	public void tearDown() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		clearEverything();
	}
	
	private void clearEverything() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config = manager.get();

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
		
		manager.apply(config).get();
	}
	
	@Test
	public void testSequentialRepositoryModification() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config = manager.get();
		config.createRepository("test-repo");
		manager.apply(config).get();
		
		config = manager.get();
		Repository repository = config.getRepository("test-repo");
		config.removeRepository(repository);
		manager.apply(config).get();
		
		config = manager.get();
		Assert.assertNull(config.getRepository("test-repo"));
	}
	
	@Test(expected = ExecutionException.class)
	public void testConcurrentRepositoryCreation() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.createRepository("test-repo");
		config2.createRepository("test-repo");
		
		manager.apply(config1);
		manager.apply(config2).get();
	}
	
	@Test(expected = ExecutionException.class)
	public void testConcurrentRepositoryRemoval() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config = manager.get();
		config.createRepository("test-repo");
		manager.apply(config).get();
		
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.removeRepository(config1.getRepository("test-repo"));
		config2.removeRepository(config2.getRepository("test-repo"));
		
		manager.apply(config1);
		manager.apply(config2).get();
	}
	
	@Test
	public void testSequentialGroupModification() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config = manager.get();
		config.createGroup("@test-group").add(config.getUser("git"));
		manager.apply(config).get();
		
		config = manager.get();
		config.removeGroup(config.getGroup("@test-group"));
		manager.apply(config).get();
		
		config = manager.get();
		Assert.assertNull(config.getGroup("@test-group"));
	}
	
	@Test(expected = ExecutionException.class)
	public void testConcurrentGroupCreation() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.createGroup("@test-group").add(config1.getUser("git"));
		config2.createGroup("@test-group").add(config2.getUser("git"));
		
		manager.apply(config1);
		manager.apply(config2).get();
	}
	
	@Test(expected = ExecutionException.class)
	public void testConcurrentGroupRemoval() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config = manager.get();
		config.createGroup("@test-group").add(config.getUser("git"));
		manager.apply(config).get();
		
		Config config1 = manager.get();
		Config config2 = manager.get();
		
		config1.removeGroup(config1.getGroup("@test-group"));
		config2.removeGroup(config2.getGroup("@test-group"));
		
		manager.apply(config1);
		manager.apply(config2).get();
	}
	
	@Test
	public void testSequentialUserModification() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config = manager.get();
		config.createUser("test-user").setKey("key", "value");
		manager.apply(config).get();
		
		config = manager.get();
		config.removeUser(config.getUser("test-user"));
		manager.apply(config).get();
		
		config = manager.get();
		Assert.assertNull(config.getUser("test-user"));
	}
	
	@Test(expected = ExecutionException.class)
	public void testConcurrentUserCreation() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config1 = manager.get();
		Config config2 = manager.get();

		config1.createUser("test-user").setKey("key", "value");
		config2.createUser("test-user").setKey("key", "value");
		
		manager.apply(config1);
		manager.apply(config2).get();
	}
	
	@Test(expected = ExecutionException.class)
	public void testConcurrentUserRemoval() throws IOException, ServiceUnavailable, ModificationException, InterruptedException, ExecutionException {
		Config config = manager.get();
		config.createUser("test-user").setKey("key", "value");
		manager.apply(config);
		
		Config config1 = manager.get();
		Config config2 = manager.get();

		config1.removeUser(config.getUser("test-user"));
		config2.removeUser(config.getUser("test-user"));
		
		manager.apply(config1);
		manager.apply(config2).get();
	}
	
}
