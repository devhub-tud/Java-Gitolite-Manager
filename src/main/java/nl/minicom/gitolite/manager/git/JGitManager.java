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
	public void remove(String filePattern) throws IOException {
		RmCommand rm = git.rm();
		rm.addFilepattern(filePattern);
		try {
			rm.call();
		} 
		catch (NoFilepatternException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void clone(String uri) throws IOException {
		CloneCommand clone = Git.cloneRepository();
		clone.setDirectory(workingDirectory);
		clone.setURI(uri);
		clone.setCredentialsProvider(credentialProvider);
		try {
			this.git = clone.call();
		}
		catch (JGitInternalException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void init() throws IOException {
		InitCommand initCommand = Git.init();
		initCommand.setDirectory(workingDirectory);
		try {
			this.git = initCommand.call();
		}
		catch (JGitInternalException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean pull() throws IOException {
		PullCommand pull = git.pull();
		try {
			return !pull.call().getFetchResult().getTrackingRefUpdates().isEmpty();
		} 
		catch (GitAPIException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void commitChanges() throws IOException {
		add(git, ".");
		commit(git, "Changed config...");
	}

	private void commit(Git git, String message) throws IOException {
		CommitCommand commit = git.commit();
		try {
			commit.setMessage(message).call();
		} 
		catch (GitAPIException e) {
			throw new IOException(e);
		} 
		catch (UnmergedPathException e) {
			throw new IOException(e);
		} 
		catch (JGitInternalException e) {
			throw new IOException(e);
		}
	}

	private void add(Git git, String pathToAdd) throws IOException {
		AddCommand add = git.add();
		try {
			add.addFilepattern(pathToAdd).call();
		} 
		catch (NoFilepatternException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void push() throws IOException {
		PushCommand push = git.push();
		push.setCredentialsProvider(credentialProvider);
		try {
			push.call();
		} 
		catch (JGitInternalException e) {
			throw new IOException(e);
		} 
		catch (InvalidRemoteException e) {
			throw new IOException(e);
		}
	}

	@Override
	public File getWorkingDirectory() {
		return workingDirectory;
	}

}
