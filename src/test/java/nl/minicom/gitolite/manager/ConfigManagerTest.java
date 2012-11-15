package nl.minicom.gitolite.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import javax.naming.ServiceUnavailableException;

import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Identifiable;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class ConfigManagerTest {

	@Test
	public void testLoadConfiguration() throws IOException, ServiceUnavailableException {
		GitManager git = prepareRepository();
		ConfigManager manager = ConfigManager.create(git.getWorkingDirectory().getAbsolutePath());
		Config config = manager.getConfig();

		Repository repo = config.getRepository("test");
		ImmutableMultimap<Permission, Identifiable> permissions = repo.getPermissions();

		Iterator<Identifiable> allPermissionIter = permissions.get(Permission.ALL).iterator();
		Iterator<Identifiable> readWritePermissionIter = permissions.get(Permission.READ_WRITE).iterator();
		Assert.assertEquals(Sets.newHashSet(Permission.ALL, Permission.READ_WRITE), permissions.keySet());
		Assert.assertEquals("@all", allPermissionIter.next().getName());

		User user = (User) readWritePermissionIter.next();
		Assert.assertEquals("test-user", user.getName());
		Assert.assertEquals("SSH key...", user.getKeys().get(""));
	}

	@Test
	public void testModifyingConfiguration() throws IOException, ServiceUnavailableException {
		GitManager git = prepareRepository();

		String originalLocation = git.getWorkingDirectory().getAbsolutePath();
		ConfigManager manager = ConfigManager.create(originalLocation, Files.createTempDir(), null);
		Config config = manager.getConfig();

		config.removeUser(config.getUser("test-user"));
		manager.applyConfig();

		File workingDirectory = Files.createTempDir();
		ConfigManager verifier = ConfigManager.create(originalLocation, workingDirectory, null);
		Config configToVerify = verifier.getConfig();

		Repository repo = configToVerify.getRepository("test");
		ImmutableMultimap<Permission, Identifiable> permissions = repo.getPermissions();

		Iterator<Identifiable> allPermissionIter = permissions.get(Permission.ALL).iterator();
		Assert.assertEquals(Sets.newHashSet(Permission.ALL), permissions.keySet());
		Assert.assertEquals("@all", allPermissionIter.next().getName());

		Assert.assertFalse(new File(workingDirectory, "test-user.pub").exists());
	}

	private GitManager prepareRepository() throws IOException {
		File workingDirectory = Files.createTempDir();
		GitManager git = new JGitManager(workingDirectory, null);
		git.init();

		File confDir = new File(workingDirectory, "conf");
		confDir.mkdir();

		File keyDir = new File(workingDirectory, "keydir");
		keyDir.mkdir();

		FileWriter writer = new FileWriter(new File(confDir, "gitolite.conf"));
		writer.write("repo test\n");
		writer.write("    RW+ = @all\n");
		writer.write("    RW  = test-user");
		writer.close();

		writer = new FileWriter(new File(keyDir, "test-user.pub"));
		writer.write("SSH key...");
		writer.close();

		git.commitChanges();
		return git;
	}

}
