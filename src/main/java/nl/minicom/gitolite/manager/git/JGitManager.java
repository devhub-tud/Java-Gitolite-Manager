package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.IOException;

import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * The {@link JGitManager} class is responsible for communicating with the
 * remote git repository containing the gitolite configuration.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public class JGitManager implements GitManager {

	private static final Logger log = LoggerFactory.getLogger(JGitManager.class);
	
	private final File workingDirectory;
	private final CredentialsProvider credentialProvider;

	private final Object gitLock = new Object();
	private Git git;

	/**
	 * Constructs a new {@link JGitManager} object.
	 * 
	 * @param workingDirectory The working directory where we will clone to, and
	 *           manipulate the configuration files in. It's recommended to use a
	 *           temporary directory, unless you wish to keep the git repository.
	 * 
	 * @param credentialProvider The {@link CredentialsProvider} to use to
	 *           authenticate when cloning, pulling or pushing, from or to.
	 */
	public JGitManager(File workingDirectory, CredentialsProvider credentialProvider) {
		Preconditions.checkNotNull(workingDirectory);
		this.workingDirectory = workingDirectory;
		this.credentialProvider = credentialProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#open()
	 */
	@Override
	public void open() throws IOException {
		synchronized (gitLock) {
			git = Git.open(workingDirectory);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#remove(java.lang.String)
	 */
	@Override
	public void remove(String filePattern) throws IOException {
		synchronized (gitLock) {
			RmCommand rm = git.rm();
			rm.addFilepattern(filePattern);
			try {
				rm.call();
			} catch (NoFilepatternException e) {
				throw new IOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#clone(java.lang.String)
	 */
	@Override
	public void clone(String uri) throws IOException, ServiceUnavailable {
		Preconditions.checkNotNull(uri);

		synchronized (gitLock) {
			CloneCommand clone = Git.cloneRepository();
			clone.setDirectory(workingDirectory);
			clone.setURI(uri);
			clone.setCredentialsProvider(credentialProvider);
			try {
				git = clone.call();
			} catch (NullPointerException e) {
				throw new ServiceUnavailable(e);
			} catch (JGitInternalException e) {
				throw new IOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#init()
	 */
	@Override
	public void init() throws IOException {
		synchronized (gitLock) {
			InitCommand initCommand = Git.init();
			initCommand.setDirectory(workingDirectory);
			try {
				git = initCommand.call();
			} catch (JGitInternalException e) {
				throw new IOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#pull()
	 */
	@Override
	public boolean pull() throws IOException, ServiceUnavailable {
		synchronized (gitLock) {
			try {
				PullCommand pull = git.pull();
				return !pull.call().getFetchResult().getTrackingRefUpdates().isEmpty();
			} catch (NullPointerException e) {
				throw new ServiceUnavailable(e);
			} catch (GitAPIException e) {
				throw new IOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#commitChanges()
	 */
	@Override
	public void commitChanges() throws IOException {
		synchronized (gitLock) {
			add(git, ".");
			commit(git, "Changed config...");
		}
	}

	private void commit(Git git, String message) throws IOException {
		synchronized (gitLock) {
			log.info("Commiting changes to local git repo");
			CommitCommand commit = git.commit();
			try {
				commit.setMessage(message).call();
			} catch (GitAPIException e) {
				throw new IOException(e);
			} catch (UnmergedPathException e) {
				throw new IOException(e);
			} catch (JGitInternalException e) {
				throw new IOException(e);
			}
		}
	}

	private void add(Git git, String pathToAdd) throws IOException {
		synchronized (gitLock) {
			log.info("Adding changes to commit");
			AddCommand add = git.add();
			try {
				add.addFilepattern(pathToAdd).call();
			} catch (NoFilepatternException e) {
				throw new IOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#push()
	 */
	@Override
	public void push() throws IOException, ServiceUnavailable {
		synchronized (gitLock) {
			try {
				log.info("Pushing changes to remote git repo");
				PushCommand push = git.push();
				push.setCredentialsProvider(credentialProvider);
				push.call();
			} catch (NullPointerException e) {
				throw new ServiceUnavailable(e);
			} catch (JGitInternalException e) {
				throw new IOException(e);
			} catch (InvalidRemoteException e) {
				throw new IOException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.minicom.gitolite.manager.git.GitManager#getWorkingDirectory()
	 */
	@Override
	public File getWorkingDirectory() {
		return workingDirectory;
	}

}
