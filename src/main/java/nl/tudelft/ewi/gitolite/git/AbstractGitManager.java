package nl.tudelft.ewi.gitolite.git;

import java.io.File;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public abstract class AbstractGitManager implements GitManager {

	/**
	 * The working directory where we will clone to, and
	 *           manipulate the configuration files in. It's recommended to use a
	 *           temporary directory, unless you wish to keep the git repository.
	 *
	 */
	protected final File workingDirectory;

	protected AbstractGitManager(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	@Override
	public File getWorkingDirectory() {
		return workingDirectory;
	}

	@Override
	public boolean exists() {
		return new File(workingDirectory, ".git").exists();
	}

}
