package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.base.Preconditions;

public class GitManager {
	
	private final File workingDirectory;
	private final CredentialsProvider credentialProvider;
	
	private Git git;

	public GitManager(File workingDirectory, CredentialsProvider credentialProvider) {
		Preconditions.checkNotNull(workingDirectory);
		this.workingDirectory = workingDirectory;
		this.credentialProvider = credentialProvider;
	}
	
	public void open() throws IOException {
		this.git = Git.open(workingDirectory);
	}
	
	public void clone(String uri) {
		CloneCommand clone = Git.cloneRepository();
		clone.setDirectory(workingDirectory);
		clone.setURI(uri);
		if (credentialProvider != null) {
			clone.setCredentialsProvider(credentialProvider);
		}
		this.git = clone.call();
	}
	
	public void init() {
		InitCommand initCommand = Git.init();
		initCommand.setDirectory(workingDirectory);
		this.git = initCommand.call();
	}
	
	public boolean pull() {
		PullCommand pull = git.pull();
		try {
			return !pull.call().getFetchResult().getTrackingRefUpdates().isEmpty();
		} 
		catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void commitChanges() {
		add(git, ".");
		commit(git, "Changed config...");
	}

	private void commit(Git git, String message) {
		CommitCommand commit = git.commit();
		try {
			commit.setMessage(message).call();
		} 
		catch (GitAPIException e) {
			throw new RuntimeException(e);
		} 
		catch (UnmergedPathException e) {
			throw new RuntimeException(e);
		} 
		catch (JGitInternalException e) {
			throw new RuntimeException(e);
		}
	}

	private void add(Git git, String pathToAdd) {
		AddCommand add = git.add();
		try {
			add.addFilepattern(pathToAdd).call();
		} 
		catch (NoFilepatternException e) {
			throw new RuntimeException(e);
		}
	}

	public void push() {
		PushCommand push = git.push();
		if (credentialProvider != null) {
			push.setCredentialsProvider(credentialProvider);
		}
		try {
			push.call();
		} 
		catch (JGitInternalException e) {
			throw new RuntimeException(e);
		} 
		catch (InvalidRemoteException e) {
			throw new RuntimeException(e);
		}
	}

}
