package nl.minicom.gitolite.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import nl.minicom.gitolite.manager.git.GitManager;
import nl.minicom.gitolite.manager.io.ConfigReader;
import nl.minicom.gitolite.manager.io.ConfigWriter;
import nl.minicom.gitolite.manager.models.Config;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.google.common.io.Files;

public class ConfigManager {
	
	private final String gitUrl;
	private final GitManager git;
	private final File workingDirectory;
	
	private Config config;

	public ConfigManager(String gitUrl, CredentialsProvider credentialProvider) {
		this.gitUrl = gitUrl;
		this.workingDirectory = Files.createTempDir();
		this.git = new GitManager(workingDirectory, credentialProvider);
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
