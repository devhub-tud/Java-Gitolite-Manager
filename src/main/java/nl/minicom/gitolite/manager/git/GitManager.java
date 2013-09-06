package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.IOException;

import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;

/**
 * This interface is designed to be extended to support different java-git
 * libraries.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public interface GitManager {

	/**
	 * This method attempts to open an existing git repository in the working
	 * directory.
	 * 
	 * @throws IOException If the working directory does not contain a readable
	 *            git repository.
	 */
	void open() throws IOException;

	/**
	 * Remove a file from the repository in the working directory.
	 * 
	 * @param filePattern The pattern matching all the files which need to be
	 *           removed.
	 * 
	 * @throws IOException If no matching file could be found, or the file could
	 *            not be removed.
	 */
	void remove(String filePattern) throws IOException;

	/**
	 * This method clones a git repository from the specified URI, to the current
	 * working directory.
	 * 
	 * @param uri The URI to clone the git repository from. This cannot be NULL.
	 * 
	 * @throws IOException If the clone operation failed, or if the working
	 *            directory is not ready for a new git repository.
	 *            
	 * @throws ServiceUnavailable If the git server is unreachable or otherwise unavailable.
	 */
	void clone(String uri) throws IOException, ServiceUnavailable;

	/**
	 * This method initializes a new git repository in the working directory.
	 * 
	 * @throws IOException If no new git repository could be initialized.
	 */
	void init() throws IOException;

	/**
	 * This method pulls new commits from the remote git repository.
	 * 
	 * @return True if new commits were found and pulled, false otherwise.
	 * 
	 * @throws IOException If the pull operation failed.
	 * 
	 * @throws ServiceUnavailable If the git server is unreachable or otherwise unavailable.
	 */
	boolean pull() throws IOException, ServiceUnavailable;

	/**
	 * Commits all changes to the working directory to the local git repository.
	 * 
	 * @throws IOException If the add or commit operations failed.
	 */
	void commitChanges() throws IOException;

	/**
	 * This method pushes the locally committed changes to the remote git
	 * repository.
	 * 
	 * @throws IOException If the push operation failed.
	 * 
	 * @throws ServiceUnavailable If the git server is unreachable or otherwise unavailable.
	 */
	void push() throws IOException, ServiceUnavailable;

	/**
	 * @return The working directory of this {@link JGitManager} object.
	 */
	File getWorkingDirectory();

}