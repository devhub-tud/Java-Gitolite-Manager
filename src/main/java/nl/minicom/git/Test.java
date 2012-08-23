package nl.minicom.git;

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.minicom.gitolite.Config;
import nl.minicom.gitolite.Permission;
import nl.minicom.gitolite.Repository;

public class Test {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ConfigManager configManager = new ConfigManager("git@vm:gitolite-admin", new PassphraseCredentialsProvider("gitgit"));
		configManager.initialize();
		
		Config config = configManager.getConfig();
		Repository repo = config.createRepository("r" + System.currentTimeMillis());
		repo.setPermission(config.getUser("git"), Permission.ALL);
		
		configManager.applyConfig();
	}

}
