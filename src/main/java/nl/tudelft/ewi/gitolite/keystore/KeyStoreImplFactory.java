package nl.tudelft.ewi.gitolite.keystore;

import java.io.File;

/**
 * {@code KeyStoreFactory} implementation that returns a {@link KeyStoreImpl}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class KeyStoreImplFactory implements KeyStoreFactory {

	@Override
	public KeyStoreImpl create(File keydir) {
		return new KeyStoreImpl(keydir);
	}

}
