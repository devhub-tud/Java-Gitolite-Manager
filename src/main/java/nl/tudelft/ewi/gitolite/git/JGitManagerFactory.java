package nl.tudelft.ewi.gitolite.git;

import lombok.Data;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.io.File;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class JGitManagerFactory implements GitManagerFactory {

	private CredentialsProvider credentialsProvider;

	@Override
	public JGitManager create(File repositoryFolder) {
		return new JGitManager(repositoryFolder, credentialsProvider);
	}

}
