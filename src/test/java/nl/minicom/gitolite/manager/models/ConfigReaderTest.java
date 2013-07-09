package nl.minicom.gitolite.manager.models;

import java.io.IOException;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Recorder;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;

import org.junit.Test;

public class ConfigReaderTest extends ConfigReaderTestingUtils {

	@Test(expected = IllegalArgumentException.class)
	public void testCorruptedConfig() throws IOException {
		read("corrupted-config.conf");
	}
	
	@Test
	public void testSimpleConfig() throws IOException {
		Config expected = new Config(new Recorder());
		Repository repo = expected.createRepository("test");
		User user = expected.createUser("test-user");
		repo.setPermission(user, Permission.ALL);
		
		verifyConfigsAreTheSame(expected, read("simple-config.conf"));
	}
	
	@Test
	public void testMultipleUsersAndGroupsConfig() throws IOException {
		Config expected = new Config(new Recorder());
		Repository repo = expected.createRepository("test");
		User user1 = expected.createUser("test-user-1");
		User user2 = expected.createUser("test-user-2");
		User user3 = expected.createUser("test-user-3");
		User user4 = expected.createUser("test-user-4");
		Group group1 = expected.createGroup("@test-group-1");
		Group group2 = expected.createGroup("@test-group-2");

		group1.add(user1);
		group2.add(user2);
		group2.add(user3);
		
		repo.setPermission(group1, Permission.ALL);
		repo.setPermission(group2, Permission.READ_WRITE);
		repo.setPermission(user4, Permission.READ_ONLY);
		
		verifyConfigsAreTheSame(expected, read("multiple-users-and-groups-config.conf"));
	}
	
	@Test
	public void testEmbeddedGroupsConfig() throws IOException {
		Config expected = new Config(new Recorder());
		Repository repo = expected.createRepository("test");
		User user1 = expected.createUser("test-user-1");
		User user2 = expected.createUser("test-user-2");
		User user3 = expected.createUser("test-user-3");
		Group group1 = expected.createGroup("@test-group-1");
		Group group2 = expected.createGroup("@test-group-2");

		group1.add(user1);
		group1.add(user2);
		group2.add(user3);
		group2.add(group1);
		
		repo.setPermission(group1, Permission.ALL);
		repo.setPermission(group2, Permission.READ_WRITE);
		
		verifyConfigsAreTheSame(expected, read("embedded-groups-config.conf"));
	}
	
}
