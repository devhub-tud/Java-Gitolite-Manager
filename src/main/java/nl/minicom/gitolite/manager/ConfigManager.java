package nl.minicom.gitolite.manager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.io.ConfigReader;
import nl.minicom.gitolite.manager.io.ConfigWriter;
import nl.minicom.gitolite.manager.io.KeyReader;
import nl.minicom.gitolite.manager.io.KeyWriter;
import nl.minicom.gitolite.manager.models.Config;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

/**
 * The {@link ConfigManager} class is designed to be used by developers who wish to 
 * manage their gitolite configuration.
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
	 * @param gitUri
	 * 		The URI of the remote configuration repository.
	 * 
	 * @return
	 * 		A {@link ConfigManager} which allows a developer to 
	 * 		manipulate the configuration repository.
	 */
	public static ConfigManager create(String gitUri) {
		return create(gitUri, null);
	}
	
	/**
	 * Constructs a {@link ConfigManager} which is based on the provided URI 
	 * and {@link CredentialsProvider}.
	 * 
	 * @param gitUri
	 * 		The URI of the remote configuration repository.
	 * 
	 * @param credentialProvider
	 * 		The {@link CredentialsProvider} which handles the authentication of 
	 * 		the git user who accesses the remote repository containing the configuration.
	 * 
	 * @return
	 * 		A {@link ConfigManager} which allows a developer to 
	 * 		manipulate the configuration repository.
	 */
	public static ConfigManager create(String gitUri, CredentialsProvider credentialProvider) {
		return create(gitUri, Files.createTempDir(), credentialProvider);
	}

	/**
	 * Constructs a {@link ConfigManager} which is based on the provided URI, 
	 * a working directory and {@link CredentialsProvider}.
	 * 
	 * @param gitUri
	 * 		The URI of the remote configuration repository.
	 * 
	 * @param workingDirectory
	 * 		The directory where the configuration repository needs to be cloned to.
	 * 
	 * @param credentialProvider
	 * 		The {@link CredentialsProvider} which handles the authentication of 
	 * 		the git user who accesses the remote repository containing the configuration.
	 * 
	 * @return
	 * 		A {@link ConfigManager} which allows a developer to 
	 * 		manipulate the configuration repository.
	 */
	public static ConfigManager create(String gitUri, File workingDirectory, CredentialsProvider credentialProvider) {
		return new ConfigManager(gitUri, new JGitManager(workingDirectory, credentialProvider));
	}
	
	private final String gitUri;
	private final GitManager git;
	private final File workingDirectory;
	
	private Config config;

	ConfigManager(String gitUri, GitManager gitManager) {
		Preconditions.checkNotNull(gitUri);
		Preconditions.checkNotNull(gitManager);
		
		this.gitUri = gitUri;
		this.git = gitManager;
		this.workingDirectory = git.getWorkingDirectory();
	}
	
	/**
	 * This method reads and interprets the configuration repository, and returns a representation.
	 * 
	 * @return
	 * 		A {@link Config} object, representing the configuration repository.
	 * 
	 * @throws FileNotFoundException
	 * 		If the gitolite.conf file could not be located in the repository.
	 * 
	 * @throws IOException
	 * 		If one or more files in the repository could not be read.
	 */
	public Config getConfig() throws FileNotFoundException, IOException {
		if (!new File(workingDirectory, ".git").exists()) {
			git.clone(gitUri);
		}
		
		if (git.pull() || config == null) {
			config = readConfig();
		}
		return config;
	}
	
	/**
	 * This method 
	 * @throws IOException
	 */
	public void applyConfig() throws IOException {
		if (config == null) {
			throw new IllegalStateException("Config has not yet been loaded!");
		}
		new ConfigWriter().write(config, new FileWriter(getConfigFile()));
		Set<File> writtenKeys = new KeyWriter().writeKeys(config, ensureKeyDirectory());
		Set<File> orphanedKeyFiles = listKeys();
		orphanedKeyFiles.removeAll(writtenKeys);
		
		for (File orphanedKeyFile : orphanedKeyFiles) {
			git.remove("keydir/" + orphanedKeyFile.getName());
		}
		
		git.commitChanges();
		git.push();
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

	private Config readConfig() throws FileNotFoundException, IOException {
		Config config = new ConfigReader().read(new FileReader(getConfigFile()));
		new KeyReader().readKeys(config, ensureKeyDirectory());
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
