package nl.minicom.gitolite.manager.models;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.models.Recorder.Modification;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * The {@link ConfigManager} class is designed to be used by developers who wish
 * to manage their gitolite configuration.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public class ConfigManager {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
	
	private static final String KEY_DIRECTORY_NAME = "keydir";
	private static final String CONF_FILE_NAME = "gitolite.conf";
	private static final String CONF_DIRECTORY_NAME = "conf";

	/**
	 * Constructs a {@link ConfigManager} which is based on the provided URI.
	 * 
	 * @param gitUri The URI of the remote configuration repository.
	 * 
	 * @return A {@link ConfigManager} which allows a developer to manipulate the
	 *         configuration repository.
	 */
	public static ConfigManager create(String gitUri) {
		return create(gitUri, null);
	}

	/**
	 * Constructs a {@link ConfigManager} which is based on the provided URI and
	 * {@link CredentialsProvider}.
	 * 
	 * @param gitUri The URI of the remote configuration repository.
	 * 
	 * @param credentialProvider The {@link CredentialsProvider} which handles
	 *           the authentication of the git user who accesses the remote
	 *           repository containing the configuration.
	 * 
	 * @return A {@link ConfigManager} which allows a developer to manipulate the
	 *         configuration repository.
	 */
	public static ConfigManager create(String gitUri, CredentialsProvider credentialProvider) {
		return create(gitUri, Files.createTempDir(), credentialProvider);
	}

	/**
	 * Constructs a {@link ConfigManager} which is based on the provided URI, a
	 * working directory and {@link CredentialsProvider}.
	 * 
	 * @param gitUri The URI of the remote configuration repository.
	 * 
	 * @param workingDirectory The directory where the configuration repository
	 *           needs to be cloned to.
	 * 
	 * @param credentialProvider The {@link CredentialsProvider} which handles
	 *           the authentication of the git user who accesses the remote
	 *           repository containing the configuration.
	 * 
	 * @return A {@link ConfigManager} which allows a developer to manipulate the
	 *         configuration repository.
	 */
	public static ConfigManager create(String gitUri, File workingDirectory, CredentialsProvider credentialProvider) {
		return new ConfigManager(gitUri, new JGitManager(workingDirectory, credentialProvider));
	}

	private final String gitUri;
	private final GitManager git;
	private final File workingDirectory;
	private final Worker worker;
	
	private final AtomicReference<Config> config; 
	private final Object diskLock = new Object();
	
	/**
	 * Constructs a new {@link ConfigManager} object.
	 * 
	 * @param gitUri The URI to clone from and push changes to.
	 * 
	 * @param gitManager The {@link GitManager} which will handle the git
	 *           operations.
	 */
	ConfigManager(String gitUri, GitManager gitManager) {
		Preconditions.checkNotNull(gitUri);
		Preconditions.checkNotNull(gitManager);

		this.gitUri = gitUri;
		this.git = gitManager;
		this.workingDirectory = git.getWorkingDirectory();
		this.config = new AtomicReference<>();
		this.worker = new Worker();
	}
	
	private void ensureAdminRepoIsUpToDate() throws ServiceUnavailable, IOException {
		try {
			if (!new File(workingDirectory, ".git").exists()) {
				log.info("Cloning from: {} to: {}", gitUri, workingDirectory);
				git.clone(gitUri);
			}
			else {
				log.info("Pulling from: {}", gitUri);
				git.pull();
			}
		}
		catch (IOException | ServiceUnavailable e) {
			throw new ServiceUnavailable(e);
		}
	}
	
	private void ensureAdminRepoPresent() throws IOException, ServiceUnavailable {
		if (!new File(workingDirectory, ".git").exists()) {
			log.info("Cloning from: {} to: {}", gitUri, workingDirectory);
			git.clone(gitUri);
			readConfig();
		}
	}

	/**
	 * This method returns a representation of the current gitolite configuration.
	 * 
	 * @return A {@link Config} object, representing the gitolite configuration.
	 * 
	 * @throws ServiceUnavailable If the service could not be reached.
	 * 
	 * @throws IOException If one or more files in the repository could not be read.
	 */
	public Config get() throws IOException, ServiceUnavailable {
		ensureAdminRepoPresent();
		Config copy = config.get().copy();
		copy.getRecorder().record();
		return copy;
	}
	
	/**
	 * This method applies any changes that were made to the specified {@link Config} 
	 * object to the gitolite server. This method returns a {@link ListenableFuture} 
	 * which can be used to get a notification when the changes have been applied, or 
	 * if the changes could not be applied due to conflicts.
	 * 
	 * @param config
	 * 	The {@link Config} object to apply the changes of to the gitolite server.
	 * 
	 * @return
	 * 	A {@link ListenableFuture} which notifies the owner of completion or failure.
	 */
	public ListenableFuture<Void> applyAsync(Config config) {
		List<Modification> recording = config.getRecorder().stop();
		return worker.submit(recording);
	}
	
	/**
	 * This method applies any changes that were made to the specified {@link Config}
	 * object to the gitolite server. This method blocks until the operation has completed
	 * or failed.
	 * 
	 * @param config
	 * 	The {@link Config} object to apply the changes of to the gitolite server.
	 * 
	 * @throws ModificationException
	 * 	When the changes conflict to other changes, and thus could not be applied.
	 */
	public void apply(Config config) throws ModificationException {
		ListenableFuture<Void> future = applyAsync(config);
		try {
			future.get();
		}
		catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		catch (ExecutionException e) {
			try {
				throw e.getCause();
			}
			catch (ModificationException e1) {
				throw e1;
			}
			catch (Throwable e1) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e1);
			}
		}
	}
	
	private void writeAndPush() throws IOException, ServiceUnavailable {
		Config newConfig = config.get();
		if (newConfig == null) {
			throw new IllegalStateException("Config has not yet been loaded!");
		}
		
		synchronized (diskLock) {
			log.info("Writing Config object to disk");
			
			ConfigWriter.write(newConfig, new FileWriter(getConfigFile()));
			Set<File> writtenKeys = KeyWriter.writeKeys(newConfig, ensureKeyDirectory());
			Set<File> orphanedKeyFiles = listKeys();
			orphanedKeyFiles.removeAll(writtenKeys);
	
			for (File orphanedKeyFile : orphanedKeyFiles) {
				git.remove("keydir/" + orphanedKeyFile.getName());
			}
		}
		
		git.commitChanges();
		git.push();
	}

	private Set<File> listKeys() {
		Set<File> keys = Sets.newHashSet();

		synchronized (diskLock) {
			File keyDir = new File(workingDirectory, "keydir");
			if (keyDir.exists()) {
				File[] keyFiles = keyDir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.getName().endsWith(".pub");
					}
				});
	
				for (File keyFile : keyFiles) {
					keys.add(keyFile);
				}
			}
		}

		return keys;
	}

	private Config readConfig() throws IOException {
		synchronized (diskLock) {
			Config read = ConfigReader.read(new FileReader(getConfigFile()));
			KeyReader.readKeys(read, ensureKeyDirectory());
			config.set(read);
			return read;
		}
	}

	private File getConfigFile() {
		synchronized (diskLock) {
			File confDirectory = new File(workingDirectory, CONF_DIRECTORY_NAME);
			if (!confDirectory.exists()) {
				throw new IllegalStateException("Could not open " + CONF_DIRECTORY_NAME + "/ directory!");
			}
	
			File confFile = new File(confDirectory, CONF_FILE_NAME);
			return confFile;
		}
	}

	private File ensureKeyDirectory() {
		synchronized (diskLock) {
			File keyDir = new File(workingDirectory, KEY_DIRECTORY_NAME);
			keyDir.mkdir();
			return keyDir;
		}
	}
	
	/**
	 * The {@link Worker} class is a simple class which is notified of any incoming {@link Modification}s
	 * and processes them in batches of at most 10.
	 */
	private class Worker {

		protected static final int MAXIMUM_BATCH_SIZE = 10;
		
		private final ScheduledThreadPoolExecutor executor;
		private final Queue<UnitOfWork> modifications;
		
		public Worker() {
			this.modifications = Queues.newConcurrentLinkedQueue();
			this.executor = new ScheduledThreadPoolExecutor(1);
			
			startWorker();
		}
		
		private void startWorker() {
			executor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						waitUntilModificationsArePresent();

						log.debug("Worker found changes");
						Collection<SettableFuture<Void>> succeeded = applyChanges(false);
						
						try {
							log.info("Worker is pushing changes to remote repository");
							writeAndPush();
						}
						catch (IOException | ServiceUnavailable e) {
							log.error("Worker failed to push changes to remote repository, notifying owners", e);
							for (SettableFuture<Void> future : succeeded) {
								future.setException(e);
							}
							return null;
						}
						
						log.debug("Worker is notifying changeset owners");
						for (SettableFuture<Void> future : succeeded) {
							future.set(null);
						}
					} 
					finally {
						startWorker();
					}
					
					return null;
				}
				
			});
		}

		private void waitUntilModificationsArePresent() throws InterruptedException {
			synchronized (modifications) {
				while (modifications.isEmpty()) {
					log.debug("Worker is waiting for changes...");
					modifications.wait();
				}
			}
		}
		
		private Collection<SettableFuture<Void>> applyChanges(boolean update) throws ServiceUnavailable, IOException {
			if (update) {
				log.info("Pulling changes from remote repository");
				ensureAdminRepoIsUpToDate();
			}
			
			Collection<SettableFuture<Void>> succeeded = Lists.newArrayList();
			Config current = config.get().copy();
			
			log.info("Worker is applying {} changeset(s)", modifications.size());
			while (!modifications.isEmpty() && succeeded.size() < MAXIMUM_BATCH_SIZE) {
				Config fallback = current.copy();
				
				UnitOfWork unit = modifications.poll();
				try {
					log.info("Worker is applying {} change(s)", unit.getModifications().size());
					for (Modification change : unit.getModifications()) {
						change.apply(current);
					}
					succeeded.add(unit.getFuture());
				}
				catch (ModificationException e) {
					log.error("Worker failed to apply a changeset, notifying owner");
					unit.getFuture().setException(e);
					current = fallback;
				}
			}
			
			log.info("Worker successfully applied {} changeset", succeeded.size());
			config.set(current);
			return succeeded;
		}

		public ListenableFuture<Void> submit(List<Modification> recording) {
			if (recording == null || recording.isEmpty()) {
				SettableFuture<Void> future = SettableFuture.create();
				future.set(null);
				return future;
			}
			
			log.info("Submitting a new changeset, containing {} changes", recording.size());
			UnitOfWork unit = new UnitOfWork(recording);
			
			synchronized (modifications) {
				modifications.offer(unit);
				modifications.notify();
			}
			
			return unit.getFuture();
		}
		
	}
	
	/**
	 * The {@link UnitOfWork} class is a data object, which holds a reference to the
	 * {@link ImmutableList} of {@link Modification}s which need to be applied, and
	 * an internally created {@link SettableFuture} object, to which others can attach
	 * listeners.
	 */
	private static class UnitOfWork {
		
		private final ImmutableList<Modification> modifications;
		private final SettableFuture<Void> future;
		
		public UnitOfWork(List<Modification> modifications) {
			this.modifications = ImmutableList.copyOf(modifications);
			this.future = SettableFuture.create();
		}
		
		public ImmutableList<Modification> getModifications() {
			return modifications;
		}
		
		public SettableFuture<Void> getFuture() {
			return future;
		}
		
	}

}
