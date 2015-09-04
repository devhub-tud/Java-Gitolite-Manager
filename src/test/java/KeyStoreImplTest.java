import com.google.common.io.Files;
import nl.tudelft.ewi.gitolite.keystore.Key;
import nl.tudelft.ewi.gitolite.keystore.KeyStoreImpl;
import nl.tudelft.ewi.gitolite.objects.Identifiable;
import static org.junit.Assert.*;

import nl.tudelft.ewi.gitolite.objects.Identifier;
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
	private static String identifiable = "lupa";

	@Before
	public void setUp() {
		folder = Files.createTempDir();
		keyStore = new KeyStoreImpl(folder);
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
		KeyStoreImpl otherKeyStore = new KeyStoreImpl(folder);
		assertEquals(key, otherKeyStore.getKey(identifiable, ""));
		assertThat(otherKeyStore.getKeys(identifiable), Matchers.contains(key));
	}

	@Test
	public void testDelete() throws IOException {
		Key key = keyStore.put(identifiable, "", "contents");
		key.delete();
		KeyStoreImpl otherKeyStore = new KeyStoreImpl(folder);
		assertThat(otherKeyStore.getKeys(identifiable), Matchers.empty());
	}

}
