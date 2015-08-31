package nl.tudelft.ewi.gitolite.config.keystore;

import nl.tudelft.ewi.gitolite.config.objects.Identifiable;

import java.io.IOException;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface Key {

	/**
	 * @return the Identifiable for this Key
	 */
	Identifiable getIdentifiable();

	/**
	 * @return the name for this key.
	 */
	String getName();

	/**
	 * @return the contents for this key.
	 * @throws IOException if the contents could not be read
	 */
	String getContents() throws IOException;

	/**
	 * Delete the key.
	 * @throws IOException if the key could not be deleted.
	 */
	void delete() throws IOException;

}
