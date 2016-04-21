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
	 * @throws IOException If an I/O error occurs
	 * @throws UnsupportedOperationException If the operation is not supported
	 *  in the {@link Repository} implementation.
	 */
	void delete() throws IOException, UnsupportedOperationException;

	/**
	 * @return the folder where the bare repository resists.
	 *
	 * @throws UnsupportedOperationException If the operation is not supported
	 *  in the {@link Repository} implementation.
	 */
	Path getPath() throws UnsupportedOperationException;

	/**
	 * @return the size for the repository.
	 * @throws IOException If an IO error occurs.
	 * @throws UnsupportedOperationException If the operation is not supported
	 *  in the {@link Repository} implementation.
	 */
	FileSize getSize() throws IOException, UnsupportedOperationException;

}
