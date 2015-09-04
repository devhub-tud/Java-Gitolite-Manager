package nl.tudelft.ewi.gitolite.git;

import java.io.File;

/**
 * Factory to instantiate an implementation for {@code GitManager}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface GitManagerFactory {

	/**
	 * Instantiate a {@code GitManager} in the given folder.
	 * @param repositoryFolder Root folder for the repository.
	 * @return The instantiated GitManager.
	 */
	GitManager create(File repositoryFolder);

}
