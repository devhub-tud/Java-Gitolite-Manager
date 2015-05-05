package nl.minicom.gitolite.manager.models;


import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

public class ConfigTest {

	private static final String TEST_REPO = "test-repo";
	private static final String TEST_USER = "test-user";
	private static final String TEST_GROUP = "@test-group";

	@Test
	public void testConstructor() {
		new Config();
	}
	
	@Test
	public void testIfWeCanCreateNewRepo() {
		Config config = new Config();
		Repository repo = config.createRepository(TEST_REPO);
		Assert.assertEquals(Sets.newHashSet(repo), config.getRepositories());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatCreateRepositoryMethodThrowsExceptionWhenNameIsNull() {
		new Config().createRepository(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatCreateRepositoryMethodThrowsExceptionWhenNameIsEmpty() {
		new Config().createRepository("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatWeCannotCreateTwoReposWithSameName() {
		Config config = new Config();
		config.createRepository(TEST_REPO);
		config.createRepository(TEST_REPO);
	}
	
	@Test
	public void testThatEnsureRepositoryMethodCreatesNewRepoWhenRepoDoesntExist() {
		Config config = new Config();
		Repository repo = config.ensureRepositoryExists(TEST_REPO);
		
		Assert.assertEquals(Sets.newHashSet(repo), config.getRepositories());
	}
	
	@Test
	public void testThatEnsureRepositoryMethodDoesNotCreateDuplicateRepositories() {
		Config config = new Config();
		Repository repo1 = config.ensureRepositoryExists(TEST_REPO);
		Repository repo2 = config.ensureRepositoryExists(TEST_REPO);
		
		Assert.assertEquals(repo1, repo2);
		Assert.assertEquals(Sets.newHashSet(repo1), config.getRepositories());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatEnsureRepositoryExistsMethodThrowsExceptionWhenNameIsNull() {
		new Config().ensureRepositoryExists(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatEnsureRepositoryExistsMethodThrowsExceptionWhenNameIsEmpty() {
		new Config().ensureRepositoryExists("");
	}

	@Test
	public void testRemoveRepositoryOnExistingRepository() {
		Config config = new Config();
		Repository repository = config.createRepository(TEST_REPO);
		
		Assert.assertTrue(config.removeRepository(repository));
		Assert.assertEquals(Sets.<Repository>newHashSet(), config.getRepositories());
	}
	
	@Test(expected = NullPointerException.class)
	public void testRemoveRepositoryOnNullArgument() {
		new Config().removeRepository(null);
	}
	
	@Test
	public void testHasRepositoryMethodWhenRepositoryExists() {
		Config config = new Config();
		config.createRepository(TEST_REPO);
		Assert.assertTrue(config.hasRepository(TEST_REPO));
	}
	
	@Test
	public void testHasRepositoryMethodWhenRepositoryDoesNotExist() {
		Assert.assertFalse(new Config().hasRepository(TEST_REPO));
	}
	
	@Test(expected = NullPointerException.class)
	public void testGetRepositoryWithNullAsInput() {
		new Config().getRepository(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetRepositoryWithEmptyStringAsInput() {
		new Config().getRepository("");
	}
	
	@Test
	public void testIfWeCanCreateNewGroup() {
		Config config = new Config();
		Group group = config.createGroup(TEST_GROUP);
		Assert.assertEquals(Sets.newHashSet(group), config.getGroups());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatCreateGroupMethodThrowsExceptionWhenNameIsNull() {
		new Config().createGroup(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatCreateGroupMethodThrowsExceptionWhenNameIsEmpty() {
		new Config().createGroup("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatWeCannotCreateTwoGroupsWithSameName() {
		Config config = new Config();
		config.createGroup(TEST_GROUP);
		config.createGroup(TEST_GROUP);
	}
	
	@Test
	public void testThatEnsureGroupMethodCreatesNewgroupWhengroupDoesntExist() {
		Config config = new Config();
		Group group = config.ensureGroupExists(TEST_GROUP);
		
		Assert.assertEquals(Sets.newHashSet(group), config.getGroups());
	}
	
	@Test
	public void testThatEnsureGroupMethodDoesNotCreateDuplicateGroup() {
		Config config = new Config();
		Group group1 = config.ensureGroupExists(TEST_GROUP);
		Group group2 = config.ensureGroupExists(TEST_GROUP);
		
		Assert.assertEquals(group1, group2);
		Assert.assertEquals(Sets.newHashSet(group1), config.getGroups());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatEnsureGroupExistsMethodThrowsExceptionWhenNameIsNull() {
		new Config().ensureGroupExists(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatEnsureGroupExistsMethodThrowsExceptionWhenNameIsEmpty() {
		new Config().ensureGroupExists("");
	}

	@Test
	public void testRemoveGroupOnExistingGroup() {
		Config config = new Config();
		Group group = config.createGroup(TEST_GROUP);
		
		Assert.assertTrue(config.removeGroup(group));
		Assert.assertEquals(Sets.<Group>newHashSet(), config.getGroups());
	}
	
	@Test(expected = NullPointerException.class)
	public void testRemoveGroupOnNullArgument() {
		new Config().removeGroup(null);
	}
	
	@Test
	public void testHasGroupMethodWhenGroupExists() {
		Config config = new Config();
		config.createGroup(TEST_GROUP);
		Assert.assertTrue(config.hasGroup(TEST_GROUP));
	}
	
	@Test
	public void testHasGroupMethodWhenGroupDoesNotExist() {
		Assert.assertFalse(new Config().hasGroup(TEST_GROUP));
	}
	
	@Test(expected = NullPointerException.class)
	public void testGetGroupWithNullAsInput() {
		new Config().getGroup(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetGroupWithEmptyStringAsInput() {
		new Config().getGroup("");
	}
	
	@Test
	public void testIfWeCanCreateNewUser() {
		Config config = new Config();
		User user = config.createUser(TEST_USER);
		Assert.assertEquals(Sets.newHashSet(user), config.getUsers());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatCreateUserMethodThrowsExceptionWhenNameIsNull() {
		new Config().createUser(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatCreateUserMethodThrowsExceptionWhenNameIsEmpty() {
		new Config().createUser("");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatWeCannotCreateTwoUsersWithSameName() {
		Config config = new Config();
		config.createUser(TEST_USER);
		config.createUser(TEST_USER);
	}
	
	@Test
	public void testThatEnsureUserMethodCreatesNewuserWhenuserDoesntExist() {
		Config config = new Config();
		User user = config.ensureUserExists(TEST_USER);
		
		Assert.assertEquals(Sets.newHashSet(user), config.getUsers());
	}
	
	@Test
	public void testThatEnsureUserMethodDoesNotCreateDuplicateUser() {
		Config config = new Config();
		User user1 = config.ensureUserExists(TEST_USER);
		User user2 = config.ensureUserExists(TEST_USER);
		
		Assert.assertEquals(user1, user2);
		Assert.assertEquals(Sets.newHashSet(user1), config.getUsers());
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatEnsureUserExistsMethodThrowsExceptionWhenNameIsNull() {
		new Config().ensureUserExists(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatEnsureUserExistsMethodThrowsExceptionWhenNameIsEmpty() {
		new Config().ensureUserExists("");
	}

	@Test
	public void testRemoveUserOnExistingUser() {
		Config config = new Config();
		User user = config.createUser(TEST_USER);
		
		Assert.assertTrue(config.removeUser(user));
		Assert.assertEquals(Sets.<User>newHashSet(), config.getUsers());
	}
	
	@Test(expected = NullPointerException.class)
	public void testRemoveUserOnNullArgument() {
		new Config().removeUser(null);
	}
	
	@Test
	public void testHasUserMethodWhenUserExists() {
		Config config = new Config();
		config.createUser(TEST_USER);
		Assert.assertTrue(config.hasUser(TEST_USER));
	}
	
	@Test
	public void testHasUserMethodWhenUserDoesNotExist() {
		Assert.assertFalse(new Config().hasUser(TEST_USER));
	}
	
	@Test(expected = NullPointerException.class)
	public void testGetUserWithNullAsInput() {
		new Config().getUser(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetUserWithEmptyStringAsInput() {
		new Config().getUser("");
	}
	
}
