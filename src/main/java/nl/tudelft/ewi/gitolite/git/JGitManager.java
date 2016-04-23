package nl.tudelft.ewi.gitolite.git;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import com.google.common.base.Preconditions;

/**
 * The {@link JGitManager} class is responsible for communicating with the
 * remote git repository containing the gitolite configuration.
 *
 * @author Michael de Jong &lt;<a href="mailto:michaelj@minicom.nl">michaelj@minicom.nl</a>&gt;
 */
@Slf4j
public class JGitManager extends AbstractGitManager implements GitManager {

	/**
	 * The {@link CredentialsProvider} to use to
	 *           authenticate when cloning, pulling or pushing, from or to.
	 */
	private final CredentialsProvider credentialProvider;

	private final Object gitLock = new Object();
	private Git git;

	public JGitManager(File workingDirectory, CredentialsProvider credentialProvider) {
		super(workingDirectory);
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
	public void remove(String filePattern) throws IOException, GitException {
		synchronized (gitLock) {
			RmCommand rm = git.rm();
			rm.addFilepattern(filePattern);
			try {
				rm.call();
			} catch (NoFilepatternException e) {
				throw new IOException(e);
			} catch (GitAPIException e) {
				throw new GitException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.minicom.gitolite.manager.git.GitManager#clone(java.lang.String)
	 */
	@Override
	public void clone(String uri) throws GitException {
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
			} catch (GitAPIException e) {
				throw new GitException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.minicom.gitolite.manager.git.GitManager#init()
	 */
	@Override
	public void init() throws GitException {
		synchronized (gitLock) {
			InitCommand initCommand = Git.init();
			initCommand.setDirectory(workingDirectory);
			try {
				git = initCommand.call();
			} catch (GitAPIException e) {
				throw new GitException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.minicom.gitolite.manager.git.GitManager#pull()
	 */
	@Override
	public boolean pull() throws GitException {
		log.info("Pulling changes from remote git repo");
		synchronized (gitLock) {
			try {
				PullCommand pull = git.pull();
				return !pull.call().getFetchResult().getTrackingRefUpdates().isEmpty();
			} catch (NullPointerException e) {
				throw new ServiceUnavailable(e);
			} catch (GitAPIException e) {
				throw new GitException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.minicom.gitolite.manager.git.GitManager#commitChanges()
	 */
	@Override
	public void commitChanges() throws IOException, GitException {
		synchronized (gitLock) {
			add(git, ".");
			commit(git, "Changed config...");
		}
	}

	private void commit(Git git, String message) throws GitException {
		synchronized (gitLock) {
			log.info("Commiting changes to local git repo");
			CommitCommand commit = git.commit();
			try {
				commit.setMessage(message).call();
			} catch (GitAPIException e) {
				throw new GitException(e);
			}
		}
	}

	private void add(Git git, String pathToAdd) throws IOException, GitException {
		synchronized (gitLock) {
			log.info("Adding changes to commit");
			try {
				git.add().addFilepattern(pathToAdd).setUpdate(true).call();
				git.add().addFilepattern(pathToAdd).setUpdate(false).call();
			} catch (NoFilepatternException e) {
				throw new IOException(e);
			} catch (GitAPIException e) {
				throw new GitException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.minicom.gitolite.manager.git.GitManager#push()
	 */
	@Override
	public void push() throws GitException {
		synchronized (gitLock) {
			try {
				log.info("Pushing changes to remote git repo");
				PushResult pushResult = git.push()
					.setCredentialsProvider(credentialProvider)
					.call().iterator().next();

				for(RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
					checkPushSuccess(update);
				}
			} catch (NullPointerException e) {
				throw new ServiceUnavailable(e);
			} catch (GitAPIException | JGitInternalException e) {
				throw new GitException(e);
			}
		}
	}

	/**
	 * Check if the push succedded (remote is either up to date or the push could be fast forwarded)
	 * @param update {@code RemoteRefUpdate} to check
	 */
	private void checkPushSuccess(RemoteRefUpdate update) {
		switch(update.getStatus()){
			case OK:
			case UP_TO_DATE:
				return;
			default:
				throw new IllegalStateException("Cannot push config to gitolite config: " + update.getStatus());
		}
	}

}