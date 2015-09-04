package nl.tudelft.ewi.gitolite.keystore;

import java.io.File;

/**
 * Factory to instantiate an implementation for {@code KeyStore}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface KeyStoreFactory {

	/**
	 * Instantiate a {@code KeyStore} in the given folder.
	 * @param keydir Key directory.
	 * @return The instantiated KeyStore.
	 */
	KeyStore create(File keydir);

}
