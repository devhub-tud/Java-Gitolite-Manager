package nl.minicom.gitolite.manager.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.KeyReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class KeyReaderTest {

	private static final String CONTENT = "Random ssh key...";

	private Map<String, String> expectedKeys;

	@Before
	public void setUp() {
		expectedKeys = Maps.newTreeMap();
	}

	@Test(expected = NullPointerException.class)
	public void testThatAddingKeysToConfigWhenConfigIsNullExceptionIsThrown() throws IOException {
		KeyReader.readKeys(null, Files.createTempDir());
	}

	@Test(expected = NullPointerException.class)
	public void testThatAddingKeysToConfigWhenKeyDirIsNullExceptionIsThrown() throws IOException {
		KeyReader.readKeys(new Config(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThatAddingKeysToConfigWhenKeyDirIsNoDirectory() throws IOException {
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test", null, CONTENT);

		KeyReader.readKeys(new Config(), new File(keyDir, "test.pub"));
	}

	@Test
	public void testAddingSingleKeyWithoutNameToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", null, CONTENT);

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("", CONTENT);
		Assert.assertEquals(expectedKeys, config.getUser("test-user-1").getKeys());
	}

	@Test
	public void testAddingSingleKeyWithNameToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", "iMac", CONTENT);

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("iMac", CONTENT);

		Assert.assertEquals(expectedKeys, config.getUser("test-user-1").getKeys());
	}

	@Test
	public void testAddingMultipleKeysForTheSameUserToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", "iMac", CONTENT);
		writeKeyFile(keyDir, "test-user-1", "MacBook-Air", CONTENT);

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("iMac", CONTENT);
		expectedKeys.put("MacBook-Air", CONTENT);

		Assert.assertEquals(expectedKeys, config.getUser("test-user-1").getKeys());
	}

	@Test
	public void testAddingMultipleKeysForMultipleUsersToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", "iMac", CONTENT);
		writeKeyFile(keyDir, "test-user-1", "MacBook-Air", CONTENT);
		writeKeyFile(keyDir, "test-user-2", "iMac", CONTENT);
		writeKeyFile(keyDir, "test-user-2", "MacBook-Air", CONTENT);

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("iMac", CONTENT);
		expectedKeys.put("MacBook-Air", CONTENT);

		Assert.assertEquals(expectedKeys, config.getUser("test-user-1").getKeys());
		Assert.assertEquals(expectedKeys, config.getUser("test-user-2").getKeys());
	}

	private void writeKeyFile(File keyDir, String userName, String keyName, String content) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(userName);
		if (keyName != null && !keyName.isEmpty()) {
			builder.append("@" + keyName);
		}
		builder.append(".pub");

		FileWriter writer = new FileWriter(new File(keyDir, builder.toString()));
		writer.append(content);
		writer.close();
	}

}
