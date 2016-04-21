package nl.tudelft.ewi.gitolite;

import com.googlecode.concurentlocks.ReadWriteUpdateLock;
import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.tudelft.ewi.gitolite.config.Config;
import nl.tudelft.ewi.gitolite.git.GitManager;
import nl.tudelft.ewi.gitolite.keystore.KeyStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The {@code ManagedConfig} decorates a {@link KeyStore} and a {@link Config} with a
 * {@link GitManager}, in order to {@link ManagedConfig#applyChanges() apply} changes
 * to the remote config.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@AllArgsConstructor
public class ManagedConfig {

	/**
	 * The configuration folder.
	 */
	public static final String CONFDIR_REL_PATH = "conf";

	/**
	 * The configuration file name.
	 */
	public static final String GITOLITE_CONF_FILE = "gitolite.conf";

	/**
	 * The {@code GitManager} to use.
	 */
	private final GitManager gitManager;

	/**
	 * The {@code KeyStore} to use.
	 */
	private final KeyStore keyStore;

	/**
	 * The {@code Config}.
	 */
	private final Config config;

	/**
	 * Locks used for operations on the {@code Config} and {@code KeyStore}.
	 */
	private final ReadWriteUpdateLock readWriteLock = new ReentrantReadWriteUpdateLock();

	/**
	 * Commit and push changes to the remote.
	 */
	@SneakyThrows
	protected void applyChanges() {
		File confDir = new File(gitManager.getWorkingDirectory(), CONFDIR_REL_PATH);
		File configurationFile = new File(confDir, GITOLITE_CONF_FILE);
		try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configurationFile, false))) {
			config.write(bufferedWriter);
			gitManager.commitChanges();
			gitManager.push();
		}
	}

	/**
	 * Perform read operations to the {@link Config} within a {@code ReadLock}.
	 * @param configInteraction function
	 * @param <T> return type
	 * @return return value
	 */
	public <T> T readConfigWithReturn(Function<? super Config, T> configInteraction) {
		readWriteLock.updateLock().lock();
		try {
			return configInteraction.apply(config);
		}
		finally {
			readWriteLock.updateLock().unlock();
		}
	}

	/**
	 * Perform read operations to the {@link Config} within a {@code ReadLock}.
	 * @param configInteraction function
	 */
	public void readConfig(Consumer<? super Config> configInteraction) {
		readWriteLock.updateLock().lock();
		try {
			configInteraction.accept(config);
		}
		finally {
			readWriteLock.updateLock().unlock();
		}
	}

	/**
	 * Perform write operations to the {@link Config} within a {@code WriteLock}.
	 * Applies the changes to the repository when done.
	 * @param configInteraction function
	 * @see ManagedConfig#applyChanges()
	 */
	public void writeConfig(Consumer<? super Config> configInteraction) {
		readWriteLock.writeLock().lock();
		try {
			configInteraction.accept(config);
			applyChanges();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Perform write operations to the {@link Config} within a {@code WriteLock}.
	 * Applies the changes to the repository when done.
	 * @param configInteraction function
	 * @param <T> return type
	 * @return return value
	 * @see ManagedConfig#applyChanges()
	 */
	public <T> T writeConfigWithReturn(Function<? super Config, T> configInteraction) {
		readWriteLock.writeLock().lock();
		try {
			T res = configInteraction.apply(config);
			applyChanges();
			return res;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * Perform read operations to the {@link KeyStore} within a {@code ReadLock}.
	 * @param configInteraction function
	 * @param <T> return type
	 * @return return value
	 */
	public <T> T readKeyStore(Function<? super KeyStore, T> configInteraction) {
		readWriteLock.updateLock().lock();
		try {
			return configInteraction.apply(keyStore);
		}
		finally {
			readWriteLock.updateLock().unlock();
		}
	}

	/**
	 * Perform write operations to the {@link KeyStore} within a {@code WriteLock}.
	 * Applies the changes to the repository when done.
	 * @param configInteraction function
	 * @param <T> return type
	 * @return return value
	 * @see ManagedConfig#applyChanges()
	 */
	@SneakyThrows
	public <T> T writeKeyStoreWithReturn(ThrowingFunction<? super KeyStore, T> configInteraction) {
		readWriteLock.writeLock().lock();
		try {
			T res = configInteraction.apply(keyStore);
			applyChanges();
			return res;
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	public interface ThrowingConsumer<T> {
		void accept(T value) throws IOException, InterruptedException;
	}

	public interface ThrowingFunction<T, R> {
		R apply(T value) throws IOException, InterruptedException;
	}

	/**
	 * Perform write operations to the {@link KeyStore} within a {@code WriteLock}.
	 * Applies the changes to the repository when done.
	 * @param configInteraction function
	 * @see ManagedConfig#applyChanges()
	 */
	@SneakyThrows
	public void writeKeyStore(ThrowingConsumer<? super KeyStore> configInteraction) {
		readWriteLock.writeLock().lock();
		try {
			configInteraction.accept(keyStore);
			applyChanges();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

}
