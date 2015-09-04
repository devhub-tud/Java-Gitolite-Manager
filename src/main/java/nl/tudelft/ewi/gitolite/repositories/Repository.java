package nl.tudelft.ewi.gitolite.repositories;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface Repository {

	/**
	 * @return return the relative repository path, used as repository identifier.
	 */
	URI getURI();

	/**
	 * Delete the repository.
	 *
	 * @throws IOException
	 */
	void delete() throws IOException, UnsupportedOperationException;

	/**
	 * @return the folder where the bare repository resists.
	 */
	Path getPath() throws UnsupportedOperationException;

	/**
	 * @return the size for the repository
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 */
	FileSize getSize() throws IOException, UnsupportedOperationException;

}
