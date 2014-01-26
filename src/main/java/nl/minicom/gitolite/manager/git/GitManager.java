package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.IOException;

import nl.minicom.gitolite.manager.exceptions.GitException;
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
	 *            
	 * @throws GitException If an exception occurred while using the Git API.
	 */
	void remove(String filePattern) throws IOException, GitException;

	/**
	 * This method clones a git repository from the specified URI, to the current
	 * working directory.
	 * 
	 * @param uri The URI to clone the git repository from. This cannot be NULL.
	 * 
	 * @throws ServiceUnavailable If the git server is unreachable or otherwise unavailable.
	 * 
	 * @throws GitException If an exception occurred while using the Git API.
	 */
	void clone(String uri) throws ServiceUnavailable, GitException;

	/**
	 * This method initializes a new git repository in the working directory.
	 * 
	 * @throws GitException If an exception occurred while using the Git API.
	 */
	void init() throws GitException;

	/**
	 * This method pulls new commits from the remote git repository.
	 * 
	 * @return True if new commits were found and pulled, false otherwise.
	 * 
	 * @throws ServiceUnavailable If the git server is unreachable or otherwise unavailable.
	 * 
	 * @throws GitException If an exception occurred while using the Git API.
	 */
	boolean pull() throws ServiceUnavailable, GitException;

	/**
	 * Commits all changes to the working directory to the local git repository.
	 * 
	 * @throws IOException If the add or commit operations failed.
	 * 
	 * @throws GitException If an exception occurred while using the Git API.
	 */
	void commitChanges() throws IOException, GitException;

	/**
	 * This method pushes the locally committed changes to the remote git
	 * repository.
	 * 
	 * @throws ServiceUnavailable If the git server is unreachable or otherwise unavailable.
	 * 
	 * @throws GitException If an exception occurred while using the Git API.
	 */
	void push() throws ServiceUnavailable, GitException;

	/**
	 * @return The working directory of this {@link JGitManager} object.
	 */
	File getWorkingDirectory();

}