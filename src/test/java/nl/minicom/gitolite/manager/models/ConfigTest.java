package nl.minicom.gitolite.manager.models;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

public class ConfigTest {
	
	@Test
	public void testConstructor() {
		new InternalConfig();
	}
	
	@Test
	public void testIfWeCanCreateNewRepo() {
		Config config = new InternalConfig();
		Repository repo = config.createRepository("test-repo");
		Assert.assertEquals(Sets.newHashSet(repo), config.getRepositories());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatCreateRepositoryMethodThrowsExceptionWhenNameIsNull() {
		new InternalConfig().createRepository(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatCreateRepositoryMethodThrowsExceptionWhenNameIsEmpty() {
		new InternalConfig().createRepository("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatWeCannotCreateTwoReposWithSameName() {
		Config config = new InternalConfig();
		config.createRepository("test-repo");
		config.createRepository("test-repo");
	}
	
	@Test
	public void testThatEnsureRepositoryMethodCreatesNewRepoWhenRepoDoesntExist() {
		Config config = new InternalConfig();
		Repository repo = config.ensureRepositoryExists("test-repo");
		
		Assert.assertEquals(Sets.newHashSet(repo), config.getRepositories());
	}
	
	@Test
	public void testThatEnsureRepositoryMethodDoesNotCreateDuplicateRepositories() {
		Config config = new InternalConfig();
		Repository repo1 = config.ensureRepositoryExists("test-repo");
		Repository repo2 = config.ensureRepositoryExists("test-repo");
		
		Assert.assertEquals(repo1, repo2);
		Assert.assertEquals(Sets.newHashSet(repo1), config.getRepositories());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatEnsureRepositoryExistsMethodThrowsExceptionWhenNameIsNull() {
		new InternalConfig().ensureRepositoryExists(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatEnsureRepositoryExistsMethodThrowsExceptionWhenNameIsEmpty() {
		new InternalConfig().ensureRepositoryExists("");
	}

	@Test
	public void testRemoveRepositoryOnExistingRepository() {
		Config config = new InternalConfig();
		Repository repository = config.createRepository("test-repo");
		
		Assert.assertTrue(config.removeRepository(repository));
		Assert.assertEquals(Sets.<Repository>newHashSet(), config.getRepositories());
	}
	
	@Test(expected = NullPointerException.class)
	public void testRemoveRepositoryOnNullArgument() {
		new InternalConfig().removeRepository(null);
	}
	
	@Test
	public void testHasRepositoryMethodWhenRepositoryExists() {
		Config config = new InternalConfig();
		config.createRepository("test-repo");
		Assert.assertTrue(config.hasRepository("test-repo"));
	}
	
	@Test
	public void testHasRepositoryMethodWhenRepositoryDoesNotExist() {
		Assert.assertFalse(new InternalConfig().hasRepository("test-repo"));
	}
	
	@Test(expected = NullPointerException.class)
	public void testGetRepositoryWithNullAsInput() {
		new InternalConfig().getRepository(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetRepositoryWithEmptyStringAsInput() {
		new InternalConfig().getRepository("");
	}
	
	@Test
	public void testIfWeCanCreateNewGroup() {
		Config config = new InternalConfig();
		InternalGroup group = config.createGroup("@test-group");
		Assert.assertEquals(Sets.newHashSet(group), config.getGroups());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatCreateGroupMethodThrowsExceptionWhenNameIsNull() {
		new InternalConfig().createGroup(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatCreateGroupMethodThrowsExceptionWhenNameIsEmpty() {
		new InternalConfig().createGroup("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatWeCannotCreateTwoGroupsWithSameName() {
		Config config = new InternalConfig();
		config.createGroup("@test-group");
		config.createGroup("@test-group");
	}
	
	@Test
	public void testThatEnsureGroupMethodCreatesNewgroupWhengroupDoesntExist() {
		Config config = new InternalConfig();
		InternalGroup group = config.ensureGroupExists("@test-group");
		
		Assert.assertEquals(Sets.newHashSet(group), config.getGroups());
	}
	
	@Test
	public void testThatEnsureGroupMethodDoesNotCreateDuplicateGroup() {
		Config config = new InternalConfig();
		InternalGroup group1 = config.ensureGroupExists("@test-group");
		Group group2 = config.ensureGroupExists("@test-group");
		
		Assert.assertEquals(group1, group2);
		Assert.assertEquals(Sets.newHashSet(group1), config.getGroups());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatEnsureGroupExistsMethodThrowsExceptionWhenNameIsNull() {
		new InternalConfig().ensureGroupExists(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatEnsureGroupExistsMethodThrowsExceptionWhenNameIsEmpty() {
		new InternalConfig().ensureGroupExists("");
	}

	@Test
	public void testRemoveGroupOnExistingGroup() {
		Config config = new InternalConfig();
		InternalGroup group = config.createGroup("@test-group");
		
		Assert.assertTrue(config.removeGroup(group));
		Assert.assertEquals(Sets.<Group>newHashSet(), config.getGroups());
	}
	
	@Test(expected = NullPointerException.class)
	public void testRemoveGroupOnNullArgument() {
		new InternalConfig().removeGroup(null);
	}
	
	@Test
	public void testHasGroupMethodWhenGroupExists() {
		Config config = new InternalConfig();
		config.createGroup("@test-group");
		Assert.assertTrue(config.hasGroup("@test-group"));
	}
	
	@Test
	public void testHasGroupMethodWhenGroupDoesNotExist() {
		Assert.assertFalse(new InternalConfig().hasGroup("@test-group"));
	}
	
	@Test(expected = NullPointerException.class)
	public void testGetGroupWithNullAsInput() {
		new InternalConfig().getGroup(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetGroupWithEmptyStringAsInput() {
		new InternalConfig().getGroup("");
	}
	
	@Test
	public void testIfWeCanCreateNewUser() {
		Config config = new InternalConfig();
		User user = config.createUser("@test-user");
		Assert.assertEquals(Sets.newHashSet(user), config.getUsers());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatCreateUserMethodThrowsExceptionWhenNameIsNull() {
		new InternalConfig().createUser(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatCreateUserMethodThrowsExceptionWhenNameIsEmpty() {
		new InternalConfig().createUser("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatWeCannotCreateTwoUsersWithSameName() {
		Config config = new InternalConfig();
		config.createUser("@test-user");
		config.createUser("@test-user");
	}
	
	@Test
	public void testThatEnsureUserMethodCreatesNewuserWhenuserDoesntExist() {
		Config config = new InternalConfig();
		User user = config.ensureUserExists("@test-user");
		
		Assert.assertEquals(Sets.newHashSet(user), config.getUsers());
	}
	
	@Test
	public void testThatEnsureUserMethodDoesNotCreateDuplicateUser() {
		Config config = new InternalConfig();
		User user1 = config.ensureUserExists("@test-user");
		User user2 = config.ensureUserExists("@test-user");
		
		Assert.assertEquals(user1, user2);
		Assert.assertEquals(Sets.newHashSet(user1), config.getUsers());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatEnsureUserExistsMethodThrowsExceptionWhenNameIsNull() {
		new InternalConfig().ensureUserExists(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatEnsureUserExistsMethodThrowsExceptionWhenNameIsEmpty() {
		new InternalConfig().ensureUserExists("");
	}

	@Test
	public void testRemoveUserOnExistingUser() {
		Config config = new InternalConfig();
		User user = config.createUser("@test-user");
		
		Assert.assertTrue(config.removeUser(user));
		Assert.assertEquals(Sets.<User>newHashSet(), config.getUsers());
	}
	
	@Test(expected = NullPointerException.class)
	public void testRemoveUserOnNullArgument() {
		new InternalConfig().removeUser(null);
	}
	
	@Test
	public void testHasUserMethodWhenUserExists() {
		Config config = new InternalConfig();
		config.createUser("@test-user");
		Assert.assertTrue(config.hasUser("@test-user"));
	}
	
	@Test
	public void testHasUserMethodWhenUserDoesNotExist() {
		Assert.assertFalse(new InternalConfig().hasUser("@test-user"));
	}
	
	@Test(expected = NullPointerException.class)
	public void testGetUserWithNullAsInput() {
		new InternalConfig().getUser(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetUserWithEmptyStringAsInput() {
		new InternalConfig().getUser("");
	}
	
}
