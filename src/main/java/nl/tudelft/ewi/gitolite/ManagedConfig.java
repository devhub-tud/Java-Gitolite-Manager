package nl.tudelft.ewi.gitolite;

import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.keystore.KeyStore;

import javax.inject.Inject;
import java.io.IOException;

/**
 * The {@code ManagedConfig} decorates a {@link KeyStore} and a {@link Config} with a
 * {@link GitManager}, in order to {@link ManagedConfig#applyChanges() apply} changes
 * to the remote config.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ManagedConfig implements KeyStore, Config {

	/**
	 * The {@code GitManager} to use.
	 */
	private final GitManager gitManager;

	/**
	 * The {@code KeyStore} to use.
	 */
	@Delegate private final KeyStore keyStore;

	/**
	 * The {@code Config}.
	 */
	@Delegate private final Config config;

	/**
	 * Commit and push changes to the remote.
	 * @throws InterruptedException If the thread was interrupted.
	 * @throws IOException If an I/O error occurred.
	 */
	public void applyChanges() throws InterruptedException, IOException {
		gitManager.commitChanges();
		gitManager.push();
	}

}
