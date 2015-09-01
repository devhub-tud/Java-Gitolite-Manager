import com.google.common.io.Files;
import nl.tudelft.ewi.gitolite.config.keystore.Key;
import nl.tudelft.ewi.gitolite.config.keystore.KeyStoreImpl;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import static org.junit.Assert.*;

import nl.tudelft.ewi.gitolite.config.objects.Identifier;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class KeyStoreImplTest {

	private File folder;
	private KeyStoreImpl keyStore;
	private static Identifiable identifiable = new Identifier("lupa");

	@Before
	public void setUp() {
		folder = Files.createTempDir();
		keyStore = new KeyStoreImpl(folder.toPath());
	}

	@Test
	public void basicAddTest() throws IOException {
		Key key = keyStore.put(identifiable, "", "contents");
		assertEquals(key, keyStore.getKey(identifiable, ""));
		assertThat(keyStore.getKeys(identifiable), Matchers.contains(key));
	}

	@Test
	public void testScan() throws IOException {
		Key key = keyStore.put(identifiable, "", "contents");
		KeyStoreImpl otherKeyStore = new KeyStoreImpl(folder.toPath());
		assertEquals(key, otherKeyStore.getKey(identifiable, ""));
		assertThat(otherKeyStore.getKeys(identifiable), Matchers.contains(key));
	}

	@Test
	public void testDelete() throws IOException {
		Key key = keyStore.put(identifiable, "", "contents");
		key.delete();
		KeyStoreImpl otherKeyStore = new KeyStoreImpl(folder.toPath());
		assertThat(otherKeyStore.getKeys(identifiable), Matchers.empty());
	}

}
