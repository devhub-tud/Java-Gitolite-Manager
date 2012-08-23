package nl.minicom.gitolite.manager;

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.minicom.gitolite.manager.git.PassphraseCredentialsProvider;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;

public class Test {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		ConfigManager configManager = ConfigManager.create("git@vm:gitolite-admin", new PassphraseCredentialsProvider("gitgit"));
		
		Config config = configManager.getConfig();
		Repository repo = config.createRepository("r" + System.currentTimeMillis());
		repo.setPermission(config.getUser("git"), Permission.ALL);
		
		configManager.applyConfig();
	}

}
