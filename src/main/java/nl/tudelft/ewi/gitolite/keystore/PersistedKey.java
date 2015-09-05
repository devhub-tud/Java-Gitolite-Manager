package nl.tudelft.ewi.gitolite.keystore;

import java.io.IOException;

/**
 * A Key that is persisted in a {@link KeyStore}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface PersistedKey extends Key {

	/**
	 * Delete the key.
	 * @throws IOException if the key could not be deleted.
	 */
	void delete() throws IOException;

}
