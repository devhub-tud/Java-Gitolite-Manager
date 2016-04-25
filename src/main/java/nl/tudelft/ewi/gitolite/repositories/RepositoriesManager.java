package nl.tudelft.ewi.gitolite.repositories;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface RepositoriesManager {

	/**
	 * List all repositories.
	 * @return a list of all the repositories.
	 */
	Collection<? extends Repository> getRepositories();

	/**
	 * Get a specific repository.
	 * @param uri {@code URI} for the repository.
	 * @return The repository instance
	 * @throws RepositoryNotFoundException when the repository could not be found.
	 */
	Repository getRepository(URI uri) throws RepositoryNotFoundException;

	/**
	 * Reload the repositories within this Manager.
	 * @throws IOException If an I/O error occurs.
     */
	void reload() throws IOException;

}
