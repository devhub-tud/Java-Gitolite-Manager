package nl.minicom.gitolite.manager.models;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.models.Recorder.Modification;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.base.Preconditions;
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
	
	private final Object lock = new Object();

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
		this.worker = new Worker();
	}
	
	private void ensureAdminRepoIsUpToDate() throws ServiceUnavailable, IOException {
		synchronized (lock) {
			try {
				if (!new File(workingDirectory, ".git").exists()) {
					git.clone(gitUri);
				}
				else {
					git.pull();
				}
			}
			catch (IOException | ServiceUnavailable e) {
				throw new ServiceUnavailable(e);
			}
		}
	}

	/**
	 * This method reads and interprets the configuration repository, and returns
	 * a representation.
	 * 
	 * @return A {@link ConfigRecorder} object, representing the configuration
	 *         repository.
	 * 
	 * @throws ServiceUnavailable If the service could not be reached.
	 * 
	 * @throws IOException If one or more files in the repository could not be
	 *            read.
	 */
	public Config get() throws IOException, ServiceUnavailable {
		ensureAdminRepoIsUpToDate();
		Config config = readConfig();
		config.getRecorder().record();
		return config;
	}
	
	public ListenableFuture<Void> apply(Config config) {
		List<Modification> recording = config.getRecorder().stop();
		return worker.submit(recording);
	}
	
	private void writeAndPush(Config config) throws IOException, ServiceUnavailable {
		synchronized (lock) {
			if (config == null) {
				throw new IllegalStateException("Config has not yet been loaded!");
			}
			
			ConfigWriter.write(config, new FileWriter(getConfigFile()));
			Set<File> writtenKeys = KeyWriter.writeKeys(config, ensureKeyDirectory());
			Set<File> orphanedKeyFiles = listKeys();
			orphanedKeyFiles.removeAll(writtenKeys);
	
			for (File orphanedKeyFile : orphanedKeyFiles) {
				git.remove("keydir/" + orphanedKeyFile.getName());
			}
			git.commitChanges();
	
			try {
				git.push();
			} 
			catch (IOException e) {
				throw new ServiceUnavailable(e);
			}
		}
	}

	private Set<File> listKeys() {
		Set<File> keys = Sets.newHashSet();

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

		return keys;
	}

	private Config readConfig() throws IOException {
		Config config = ConfigReader.read(new FileReader(getConfigFile()));
		KeyReader.readKeys(config, ensureKeyDirectory());
		return config;
	}

	private File getConfigFile() {
		File confDirectory = new File(workingDirectory, CONF_DIRECTORY_NAME);
		if (!confDirectory.exists()) {
			throw new IllegalStateException("Could not open " + CONF_DIRECTORY_NAME + "/ directory!");
		}

		File confFile = new File(confDirectory, CONF_FILE_NAME);
		return confFile;
	}

	private File ensureKeyDirectory() {
		File keyDir = new File(workingDirectory, KEY_DIRECTORY_NAME);
		keyDir.mkdir();
		return keyDir;
	}
	
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
						synchronized(modifications) {
							while (modifications.isEmpty()) {
								modifications.wait();
							}
						}
						
						ensureAdminRepoIsUpToDate();
						Config current = readConfig();
						
						Collection<SettableFuture<Void>> succeeded = Lists.newArrayList();
						
						while (!modifications.isEmpty() && succeeded.size() < MAXIMUM_BATCH_SIZE) {
							Config fallback = current.copy();
							
							UnitOfWork unit = modifications.poll();
							try {
								for (Modification change : unit.getModifications()) {
									change.apply(current);
								}
								succeeded.add(unit.getFuture());
							}
							catch (ModificationException e) {
								unit.getFuture().setException(e);
								current = fallback;
							}
						}
						
						try {
							writeAndPush(current);
						}
						catch (IOException | ServiceUnavailable e) {
							for (SettableFuture<Void> future : succeeded) {
								future.setException(e);
							}
							return null;
						}
						
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

		public ListenableFuture<Void> submit(List<Modification> recording) {
			UnitOfWork unit = new UnitOfWork(recording);
			
			synchronized (modifications) {
				modifications.offer(unit);
				modifications.notify();
			}
			
			return unit.getFuture();
		}
		
	}
	
	private static class UnitOfWork {
		
		private final List<Modification> modifications;
		private final SettableFuture<Void> future;
		
		public UnitOfWork(List<Modification> modifications) {
			this.modifications = Collections.unmodifiableList(modifications);
			this.future = SettableFuture.create();
		}
		
		public List<Modification> getModifications() {
			return modifications;
		}
		
		public SettableFuture<Void> getFuture() {
			return future;
		}
		
	}

}
