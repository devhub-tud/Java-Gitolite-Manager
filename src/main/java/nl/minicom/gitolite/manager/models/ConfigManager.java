package nl.minicom.gitolite.manager.models;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.exceptions.ServiceUnavailable;
import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.models.Recorder.Modification;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

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
		git = gitManager;
		workingDirectory = git.getWorkingDirectory();
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
	public Config loadConfig() throws IOException, ServiceUnavailable {
		ensureAdminRepoIsUpToDate();
		return readConfig();
	}

	public void applyChanges(Config config) throws IOException, ModificationException, ServiceUnavailable {
		List<Modification> changes = config.getRecorder().stop();
		
		ensureAdminRepoIsUpToDate();
		Config current = readConfig();
		
		for (Modification change : changes) {
			change.apply(current);
		}
		
		writeAndPush(current);
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

}
