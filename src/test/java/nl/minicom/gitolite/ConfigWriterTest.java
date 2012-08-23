package nl.minicom.gitolite;

import java.io.IOException;

import nl.minicom.gitolite.Config;
import nl.minicom.gitolite.Group;
import nl.minicom.gitolite.Permission;
import nl.minicom.gitolite.Repository;
import nl.minicom.gitolite.User;

import org.junit.Test;

public class ConfigWriterTest extends ConfigWriterTestingUtils {
	
	@Test
	public void testSimpleCase() throws IOException {
		Config config = new Config();
		Repository repo = config.createRepository("test");
		User user = config.createUser("test-user");
		repo.setPermission(user, Permission.ALL);
		
		validateWrittenConfig("simple-config.conf", config);
	}
	
	@Test
	public void testCaseWithMultipleUsersAndGroups() throws IOException {
		Config config = new Config();
		Repository repo = config.createRepository("test");
		User user1 = config.createUser("test-user-1");
		User user2 = config.createUser("test-user-2");
		User user3 = config.createUser("test-user-3");
		User user4 = config.createUser("test-user-4");
		Group group1 = config.createGroup("@test-group-1");
		Group group2 = config.createGroup("@test-group-2");
		
		group1.add(user1);
		group2.add(user2);
		group2.add(user3);
		
		repo.setPermission(group1, Permission.ALL);
		repo.setPermission(group2, Permission.READ_WRITE);
		repo.setPermission(user4, Permission.READ_ONLY);
		
		validateWrittenConfig("multiple-users-and-groups-config.conf", config);
	}
	
	@Test
	public void testCaseWithEmbeddedGroups() throws IOException {
		Config config = new Config();
		Repository repo = config.createRepository("test");
		User user1 = config.createUser("test-user-1");
		User user2 = config.createUser("test-user-2");
		User user3 = config.createUser("test-user-3");
		Group group1 = config.createGroup("@test-group-1");
		Group group2 = config.createGroup("@test-group-2");
		
		group1.add(user1);
		group1.add(user2);
		group2.add(user3);
		group2.add(group1);
		
		repo.setPermission(group1, Permission.ALL);
		repo.setPermission(group2, Permission.READ_WRITE);
		
		validateWrittenConfig("embedded-groups-config.conf", config);
	}
	
}
