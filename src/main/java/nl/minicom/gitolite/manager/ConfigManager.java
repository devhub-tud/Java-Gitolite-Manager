package nl.minicom.gitolite.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import nl.minicom.gitolite.manager.git.JGitManager;
import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.io.ConfigReader;
import nl.minicom.gitolite.manager.io.ConfigWriter;
import nl.minicom.gitolite.manager.models.Config;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public class ConfigManager {
	
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
		git.commitChanges();
		git.push();
	}

	private Config readConfig() throws FileNotFoundException, IOException {
		return new ConfigReader().read(new FileReader(getConfigFile()));
	}

	private File getConfigFile() {
		File confDirectory = new File(workingDirectory, "conf");
		if (!confDirectory.exists()) {
			throw new IllegalStateException("Could not open conf/ directory!");
		}
		
		File confFile = new File(confDirectory, "gitolite.conf");
		return confFile;
	}
	
}
