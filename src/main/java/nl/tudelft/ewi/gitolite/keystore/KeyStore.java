package nl.tudelft.ewi.gitolite.keystore;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface KeyStore {

	/**
	 * The empty key name
	 */
	String EMPTY_KEY_NAME = "";

	/**
	 * Get a specific key from the {@code KeyStore}.
	 * @param user {@code user} to store a key for.
	 * @param name name for the key.
	 * @return the key.
	 */
	Key getKey(String user, String name);

	/**
	 * Get the collection of keys for a user
	 * @param user {@code user} to store a key for.
	 * @return a collection of keys.
	 */
	Collection<? extends Key> getKeys(String user);

	/**
	 * Put a key into the key store
	 * @param user {@code User} to store a key for.
	 * @param contents key contents.
	 * @return the persisted key.
	 */
	default Key put(String user, String contents) throws IOException {
		return put(user, EMPTY_KEY_NAME, contents);
	}

	/**
	 * Put a key into the key store
	 * @param user {@code user} to store a key for.
	 * @param name name for the key.
	 * @param contents key contents.
	 * @return the persisted key.
	 */
	Key put(String user, String name, String contents) throws IOException;

}
