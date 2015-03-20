package nl.minicom.gitolite.manager.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import nl.minicom.gitolite.manager.git.KeyGenerator;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.KeyReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class KeyReaderTest {

	private static String KEY_CONTENTS;

	private Map<String, String> expectedKeys;

	@BeforeClass
	public static void setKeyContents() throws NoSuchAlgorithmException, IOException {
		KEY_CONTENTS  = KeyGenerator.generateRandomPublicKey();
	}

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
		writeKeyFile(keyDir, "test", null, KEY_CONTENTS );

		KeyReader.readKeys(new Config(), new File(keyDir, "test.pub"));
	}

	@Test
	public void testAddingSingleKeyWithoutNameToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", null, KEY_CONTENTS );

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("", KEY_CONTENTS );
		Assert.assertEquals(expectedKeys, config.getUser("test-user-1").getKeys());
	}

	@Test
	public void testAddingSingleKeyWithNameToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", "iMac", KEY_CONTENTS );

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("iMac", KEY_CONTENTS );

		Assert.assertEquals(expectedKeys, config.getUser("test-user-1").getKeys());
	}

	@Test
	public void testAddingMultipleKeysForTheSameUserToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", "iMac", KEY_CONTENTS );
		writeKeyFile(keyDir, "test-user-1", "MacBook-Air", KEY_CONTENTS );

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("iMac", KEY_CONTENTS );
		expectedKeys.put("MacBook-Air", KEY_CONTENTS );

		Assert.assertEquals(expectedKeys, config.getUser("test-user-1").getKeys());
	}

	@Test
	public void testAddingMultipleKeysForMultipleUsersToConfig() throws IOException {
		Config config = new Config();
		File keyDir = Files.createTempDir();
		writeKeyFile(keyDir, "test-user-1", "iMac", KEY_CONTENTS );
		writeKeyFile(keyDir, "test-user-1", "MacBook-Air", KEY_CONTENTS );
		writeKeyFile(keyDir, "test-user-2", "iMac", KEY_CONTENTS );
		writeKeyFile(keyDir, "test-user-2", "MacBook-Air", KEY_CONTENTS );

		KeyReader.readKeys(config, keyDir);
		expectedKeys.put("iMac", KEY_CONTENTS );
		expectedKeys.put("MacBook-Air", KEY_CONTENTS );

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
