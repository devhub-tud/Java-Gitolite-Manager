package nl.tudelft.ewi.gitolite.keystore;

import com.google.common.io.Files;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link KeyStoreImpl}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(Enclosed.class)
public class KeyStoreTest {

	private final static String identifiable = "foo";

	/**
	 * A {@link Parameterized} test that runs against a set of collected failing keys.
	 */
	@Data
	@RunWith(Parameterized.class)
	@EqualsAndHashCode(callSuper = false)
	public static class IllegalKeyTest extends BaseKeyStoreTest {

		@Parameters(name = "{index}: Illegal key test: {0}")
		public static Collection<Object[]> data() throws IOException {
			File illegalKeyDir = new File("src/test/resources/illegal-keys");
			return java.nio.file.Files.walk(illegalKeyDir.toPath())
				.map(Path::toFile)
				.filter(file -> file.isFile() && file.getPath().contains(".pub"))
				.map(KeyStoreTest::createKeyHolder)
				.map(pathKey -> new Object[] { pathKey })
				.collect(Collectors.toList());
		}

		private final Key pathKey;

		/**
		 * Test that the validation fails for an invalid key.
		 * @throws IOException If an I/O error occurs.
		 */
		@Test(expected = IllegalArgumentException.class)
		public void testKeyValidationFails() throws IOException {
			keyStore.validate(pathKey);
		}

	}

	/**
	 * A {@link Parameterized} test that runs against a set of generated keys.
	 */
	@Data
	@RunWith(Parameterized.class)
	@EqualsAndHashCode(callSuper = false)
	public static class ValidKeyTest extends BaseKeyStoreTest {

		@Parameters(name = "{index}: Valid key test: {0}")
		public static Collection<Object[]> data() throws IOException {
			File illegalKeyDir = new File("src/test/resources/keydir");
			return java.nio.file.Files.walk(illegalKeyDir.toPath())
				.map(Path::toFile)
				.filter(file -> file.isFile() && file.getPath().contains(".pub"))
				.map(KeyStoreTest::createKeyHolder)
				.map(pathKey -> new Object[]{pathKey})
				.collect(Collectors.toList());
		}

		private final Key pathKey;

		/**
		 * Test that the key validation passes.
		 * @throws IOException If an I/O error occurs.
		 */
		@Test
		public void testKeyValidation() throws IOException {
			keyStore.validate(pathKey);
		}

	}

	/**
	 * Test the basic methods of the {@link KeyStoreImpl}.
	 */
	public static class KeyStoreImplTest extends BaseKeyStoreTest {

		@Test
		public void basicAddTest() throws IOException {
			Key key = keyStore.put(createKeyHolder(new File("src/test/resources/keydir/test_rsa.pub")));
			assertEquals(key, keyStore.getKey(identifiable, ""));
			assertThat(keyStore.getKeys(identifiable), Matchers.contains(key));
		}

		@Test
		public void testScan() throws IOException {
			Key key = keyStore.put(createKeyHolder(new File("src/test/resources/keydir/test_rsa.pub")));
			KeyStoreImpl otherKeyStore = new KeyStoreImpl(temporaryFolder.getRoot());
			assertEquals(key, otherKeyStore.getKey(identifiable, ""));
			assertThat(otherKeyStore.getKeys(identifiable), Matchers.contains(key));
		}

		@Test
		public void testDelete() throws IOException {
			PersistedKey key = keyStore.put(createKeyHolder(new File("src/test/resources/keydir/test_rsa.pub")));
			key.delete();
			KeyStoreImpl otherKeyStore = new KeyStoreImpl(temporaryFolder.getRoot());
			assertThat(otherKeyStore.getKeys(identifiable), Matchers.empty());
		}

	}

	@SneakyThrows
	private static KeyHolder createKeyHolder(File contents) {
		return new KeyHolder(identifiable, Files.readFirstLine(contents, Charset.defaultCharset()));
	}

	protected abstract static class BaseKeyStoreTest {

		@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
		protected KeyStoreImpl keyStore;

		@Before
		public void setUp() {
			keyStore = new KeyStoreImpl(temporaryFolder.getRoot());
		}

	}

}
