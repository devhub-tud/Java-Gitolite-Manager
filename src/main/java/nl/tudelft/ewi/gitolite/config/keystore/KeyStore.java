package nl.tudelft.ewi.gitolite.config.keystore;

import nl.tudelft.ewi.gitolite.config.objects.Identifiable;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface KeyStore {

	/**
	 * Get a specific key from the {@code KeyStore}.
	 * @param identifiable {@code Identifiable} to store a key for.
	 * @param name name for the key.
	 * @return the key.
	 */
	Key getKey(Identifiable identifiable, String name);

	/**
	 * Get the collection of keys for a user
	 * @param identifiable {@code Identifiable} to store a key for.
	 * @return a collection of keys.
	 */
	Collection<? extends Key> getKeys(Identifiable identifiable);

	/**
	 * Put a key into the key store
	 * @param identifiable {@code Identifiable} to store a key for.
	 * @param name name for the key.
	 * @param contents key contents.
	 * @return the persisted key.
	 */
	Key put(Identifiable identifiable, String name, String contents) throws IOException;

}
