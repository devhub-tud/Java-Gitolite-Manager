package nl.minicom.gitolite.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.io.ConfigReader;
import nl.minicom.gitolite.manager.io.ConfigWriter;
import nl.minicom.gitolite.manager.io.KeyReader;
import nl.minicom.gitolite.manager.io.KeyWriter;
import nl.minicom.gitolite.manager.models.Config;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public class ConfigManager {
	
	private static final String KEY_DIRECTORY_NAME = "keydir";
	private static final String CONF_FILE_NAME = "gitolite.conf";
	private static final String CONF_DIRECTORY_NAME = "conf";

	public static ConfigManager create(String gitUrl) {
		return create(gitUrl, null);
	}
	
	public static ConfigManager create(String gitUrl, CredentialsProvider credentialProvider) {
		return create(gitUrl, Files.createTempDir(), credentialProvider);
	}
	
	public static ConfigManager create(String gitUrl, File workingDirectory, CredentialsProvider credentialProvider) {
		return new ConfigManager(gitUrl, new JGitManager(workingDirectory, credentialProvider));
	}
	
	private final String gitUrl;
	private final GitManager git;
	private final File workingDirectory;
	
	private Config config;

	ConfigManager(String gitUrl, GitManager gitManager) {
		Preconditions.checkNotNull(gitUrl);
		Preconditions.checkNotNull(gitManager);
		
		this.gitUrl = gitUrl;
		this.git = gitManager;
		this.workingDirectory = git.getWorkingDirectory();
	}
	
	public void initialize() {
		git.clone(gitUrl);
	}
	
	public Config getConfig() throws FileNotFoundException, IOException {
		if (git.pull() || config == null) {
			config = readConfig();
		}
		return config;
	}
	
	public void applyConfig() throws IOException {
		if (config == null) {
			throw new IllegalStateException("Config has not yet been loaded!");
		}
		new ConfigWriter().write(config, new FileWriter(getConfigFile()));
		new KeyWriter().writeKeys(config, getKeyDirectory());
		
		git.commitChanges();
		git.push();
	}

	private Config readConfig() throws FileNotFoundException, IOException {
		Config config = new ConfigReader().read(new FileReader(getConfigFile()));
		new KeyReader().readKeys(config, getKeyDirectory());
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

	private File getKeyDirectory() {
		return new File(workingDirectory, KEY_DIRECTORY_NAME);
	}
	
}
