package nl.tudelft.ewi.gitolite.git;

import java.io.File;

/**
 * {@link GitManager} implementation that returns a {@link NativeGitManager}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class NativeGitManagerFactory implements GitManagerFactory {

	@Override
	public NativeGitManager create(File repositoryFolder) {
		return new NativeGitManager(repositoryFolder);
	}

}
