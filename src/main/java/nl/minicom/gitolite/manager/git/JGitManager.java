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
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.base.Preconditions;

public class JGitManager implements GitManager {
	
	private final File workingDirectory;
	private final CredentialsProvider credentialProvider;
	
	private Git git;

	public JGitManager(File workingDirectory, CredentialsProvider credentialProvider) {
		Preconditions.checkNotNull(workingDirectory);
		this.workingDirectory = workingDirectory;
		this.credentialProvider = credentialProvider;
	}
	
	@Override
	public void open() throws IOException {
		this.git = Git.open(workingDirectory);
	}
	
	@Override
	public void remove(String filePattern) {
		RmCommand rm = git.rm();
		rm.addFilepattern(filePattern);
		try {
			rm.call();
		} 
		catch (NoFilepatternException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void clone(String uri) {
		CloneCommand clone = Git.cloneRepository();
		clone.setDirectory(workingDirectory);
		clone.setURI(uri);
		clone.setCredentialsProvider(credentialProvider);
		this.git = clone.call();
	}
	
	@Override
	public void init() {
		InitCommand initCommand = Git.init();
		initCommand.setDirectory(workingDirectory);
		this.git = initCommand.call();
	}
	
	@Override
	public boolean pull() {
		PullCommand pull = git.pull();
		try {
			return !pull.call().getFetchResult().getTrackingRefUpdates().isEmpty();
		} 
		catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
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

	@Override
	public void push() {
		PushCommand push = git.push();
		push.setCredentialsProvider(credentialProvider);
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

	@Override
	public File getWorkingDirectory() {
		return workingDirectory;
	}

}
