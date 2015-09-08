package nl.tudelft.ewi.gitolite.keystore;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface KeyStore {

	/**
	 * Get a specific key from the {@code KeyStore}.
	 * @param user {@code user} to store a key for.
	 * @param name name for the key.
	 * @return the key.
	 */
	PersistedKey getKey(String user, String name);

	/**
	 * Get the collection of keys for a user
	 * @param user {@code user} to store a key for.
	 * @return a collection of keys.
	 */
	Collection<? extends PersistedKey> getKeys(String user);

	/**
	 * Put a key into the key store
	 * @param key to perist.
	 * @return the persisted key.
	 */
	PersistedKey put(Key key) throws IOException;

	/**
	 * @return A list of users.
	 */
	Collection<String> getUsers();

	/**
	 * Scan for new keys.
	 * @throws IOException If an I/O Error occurs
	 */
	void scan() throws IOException;

}
