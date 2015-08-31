package nl.tudelft.ewi.gitolite.config.repositories;

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
	void delete() throws IOException;

	/**
	 * @return the folder where the bare repository resists.
	 */
	Path getPath() throws UnsupportedOperationException;

}
