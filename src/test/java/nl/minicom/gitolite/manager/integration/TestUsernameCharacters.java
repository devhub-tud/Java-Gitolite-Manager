package nl.minicom.gitolite.manager.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigManager;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;

@RunWith(Parameterized.class)
public class TestUsernameCharacters {

	private static final Logger log = LoggerFactory.getLogger(TestUsernameCharacters.class);
	
	private static final String CLONE_URL = "https://github.com/devhub-tud/Java-Gitolite-Manager.git";

	private static String baseUrl;
	private static String gitUri;
	private static String adminUsername;

	@BeforeClass
	public static void beforeClass() {
		Assume.assumeTrue(Strings.isNullOrEmpty(System.getProperty("skipIntegrationTests")));
		baseUrl = System.getProperty("gitUri", "ssh://git@localhost:2222/");
		gitUri = baseUrl.concat("gitolite-admin");
		adminUsername = System.getProperty("gitAdmin", "git");
	}
	
    @Parameters(name = "{index}: testUsername({0})")
    public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "Username" }, { "User-name" },
			{ "Username-" }, { "User+name" }, { "Username+" },
			{ "User.name" }, { "Username." }, { "User_name" },
			{ "Username_" } });
    }
	
	private ConfigManager manager;

	private File stagingDirectory;

	@Before
	public void setUp() throws Exception {
		resetSshSessionFactory();
		manager = ConfigManager.create(gitUri);
		stagingDirectory = Files.createTempDir();
		clearEverything();
	}

	@After
	public void tearDown() throws Exception {
		resetSshSessionFactory();
		clearEverything();
		stagingDirectory.delete();
	}
	
	private final String username;
	
	public TestUsernameCharacters(final String username) {
		this.username = username;
	}

	private void clearEverything() throws Exception {
		Config config = manager.get();

		for (User user : config.getUsers()) {
			if (!adminUsername.equals(user.getName())) {
				config.removeUser(user);
			}
		}
		
		for (Group group : config.getGroups()) {
			config.removeGroup(group);
		}
		
		for (Repository repo : config.getRepositories()) {
			if (!"gitolite-admin".equals(repo.getName())) {
				config.removeRepository(repo);
			}
		}
		
		manager.apply(config);
	}

	@Test
	public void testUsername() throws Exception {
		final KeyPair keyPair = KeyPair.genKeyPair( new JSch(), KeyPair.RSA);
		
		Config config = manager.get();
		User user = config.createUser(username);
		String keyContents = getPublicKeyContents(keyPair);
		user.setKey("Key1", keyContents);
		Repository repository = config.createRepository("bliep");
		repository.setPermission(user, Permission.READ_WRITE);
		manager.apply(config);

		Git repo = Git.cloneRepository()
			.setBare(false)
			.setDirectory(stagingDirectory)
			.setURI(CLONE_URL)
			.setProgressMonitor(new LogProgressMonitor("cloning from " + CLONE_URL))
			.call();
		
		setKeyForSsh(keyPair);

		String repoUrl = baseUrl + repository.getName();
		
		repo.push()
			.setRemote(repoUrl)
			.setPushAll()
			.setPushTags()
			.setProgressMonitor(new LogProgressMonitor("pushing to " + baseUrl))
			.call();
	}
	
	private void resetSshSessionFactory() {
		SshSessionFactory.setInstance(new DefaultSshSessionFactory());	
	}
	
	private void setKeyForSsh(KeyPair keyPair) {
		SshSessionFactory.setInstance(new PublicKeySessionFactory(keyPair));
	}
	
	private static String getPublicKeyContents(final KeyPair keypair) {
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			keypair.writePublicKey(out, "");
			return out.toString();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	static class DefaultSshSessionFactory extends JschConfigSessionFactory {
		
		@Override
		protected void configure(Host hc, Session session) {
		}
		
	}
	
	static class PublicKeySessionFactory extends DefaultSshSessionFactory {
		
		private final KeyPair keyPair;
		
		public PublicKeySessionFactory(final KeyPair keyPair) {
			this.keyPair = keyPair;
		}

		@Override
		protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException {
			JSch jSch = super.getJSch(hc, fs);
			jSch.removeAllIdentity();
			try (ByteArrayOutputStream bas = new ByteArrayOutputStream();
				ByteArrayOutputStream bis = new ByteArrayOutputStream()) {
				keyPair.writePrivateKey(bas);
				keyPair.writePublicKey(bis, "");
				jSch.addIdentity("another", bas.toByteArray(), bis.toByteArray(), (byte[]) null);
			}
			catch (Exception e){
				log.warn(e.getMessage(), e);
				throw new RuntimeException(e);
			}
			return jSch;
		}
		
	}
	
	static class LogProgressMonitor implements ProgressMonitor {

		private final String task;
		
		public LogProgressMonitor(final String task) {
			this.task = task;
		}
		
		@Override
		public void start(int totalTasks) {
			log.info("Starting {} with {} tasks", task, totalTasks);
		}

		@Override
		public void beginTask(String title, int totalWork) {}

		@Override
		public void update(int completed) {}

		@Override
		public void endTask() {
			log.info("Finished {}", task);
		}

		@Override
		public boolean isCancelled() {
			return false;
		}
		
	}
	
}
