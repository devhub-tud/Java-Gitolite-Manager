package nl.minicom.gitolite.manager;

import java.io.IOException;

import javax.naming.ServiceUnavailableException;

import nl.minicom.gitolite.manager.git.PassphraseCredentialsProvider;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Repository;

class Test {

	public static void main(String[] args) throws IOException, ServiceUnavailableException {

		ConfigManager manager = ConfigManager.create("git@dea.hartveld.com:gitolite-admin", new PassphraseCredentialsProvider("passphrase"));

		Config config = manager.getConfig();
		for (Repository repository : config.getRepositories()) {
			System.err.println(repository.getName());
		}

	}

}
