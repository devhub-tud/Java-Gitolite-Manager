package nl.minicom.gitolite.manager.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import nl.minicom.gitolite.manager.git.KeyGenerator;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.KeyWriter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

public class KeyWriterTest {

	private static String KEY_CONTENTS;

	@BeforeClass
	public static void setKeyContents() throws NoSuchAlgorithmException, IOException {
		KEY_CONTENTS  = KeyGenerator.generateRandomPublicKey();
	}

	@Test(expected = NullPointerException.class)
	public void testThatAddingKeysToConfigWhenConfigIsNullExceptionIsThrown() throws IOException {
		KeyWriter.writeKeys(null, Files.createTempDir());
	}

	@Test(expected = NullPointerException.class)
	public void testThatAddingKeysToConfigWhenKeyDirIsNullExceptionIsThrown() throws IOException {
		KeyWriter.writeKeys(new Config(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThatAddingKeysToConfigWhenKeyDirIsNoDirectory() throws IOException {
		File keyDir = Files.createTempDir();
		File file = new File(keyDir, "test.pub");
		file.createNewFile();

		KeyWriter.writeKeys(new Config(), file);
	}

	@Test
	public void testWritingSingleSimpleKeyToKeyDir() throws IOException {
		File keyDir = Files.createTempDir();
		Config config = new Config();
		config.ensureUserExists("test-user").setKey("", KEY_CONTENTS );
		KeyWriter.writeKeys(config, keyDir);

		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user.pub"));
	}

	@Test
	public void testWritingSingleKeyWithNameToKeyDir() throws IOException {
		File keyDir = Files.createTempDir();
		Config config = new Config();
		config.ensureUserExists("test-user").setKey("iMac", KEY_CONTENTS );
		KeyWriter.writeKeys(config, keyDir);

		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user@iMac.pub"));
	}

	@Test
	public void testWritingMultipleKeysForSingleUserToKeyDir() throws IOException {
		File keyDir = Files.createTempDir();
		Config config = new Config();
		config.ensureUserExists("test-user").setKey("iMac", KEY_CONTENTS );
		config.ensureUserExists("test-user").setKey("MacBook-Air", KEY_CONTENTS );
		KeyWriter.writeKeys(config, keyDir);

		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user@iMac.pub"));
		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user@MacBook-Air.pub"));
	}

	@Test
	public void testWritingMultipleKeysForMultipleUsersToKeyDir() throws IOException {
		File keyDir = Files.createTempDir();
		Config config = new Config();
		config.ensureUserExists("test-user-1").setKey("iMac", KEY_CONTENTS );
		config.ensureUserExists("test-user-1").setKey("MacBook-Air", KEY_CONTENTS );
		config.ensureUserExists("test-user-2").setKey("iMac", KEY_CONTENTS );
		config.ensureUserExists("test-user-2").setKey("MacBook-Air", KEY_CONTENTS );
		KeyWriter.writeKeys(config, keyDir);

		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user-1@iMac.pub"));
		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user-1@MacBook-Air.pub"));
		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user-2@iMac.pub"));
		Assert.assertEquals(KEY_CONTENTS , readKey(keyDir, "test-user-2@MacBook-Air.pub"));
	}

	private String readKey(File keyDir, String keyFileName) throws IOException {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(new File(keyDir, keyFileName)));

		String line;
		while ((line = reader.readLine()) != null) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append(line);
		}

		reader.close();

		return builder.toString();
	}

}
