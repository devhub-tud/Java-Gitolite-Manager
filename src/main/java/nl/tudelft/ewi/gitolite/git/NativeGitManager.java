package nl.tudelft.ewi.gitolite.git;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A {@link GitManager} backed by the native Git application. Git should be available on the path.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
public class NativeGitManager extends AbstractGitManager implements GitManager {

	private static final String GIT = "git";

	public NativeGitManager(File workingDirectory) {
		super(workingDirectory);
	}

	@Override
	public void open() {}

	@Override
	public void remove(String filePattern) throws IOException, GitException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(GIT, "rm", filePattern).directory(workingDirectory);
		Process process = processBuilder.start();

		process.waitFor();
		int exitValue = process.exitValue();

		if(exitValue != 0) {
			throw new GitException();
		}
		log.info("Removed {} from {}", filePattern, workingDirectory);
	}

	@Override
	public void clone(String uri) throws IOException, InterruptedException, GitException {
		ProcessBuilder processBuilder = new ProcessBuilder(GIT, "clone", uri, workingDirectory.getAbsolutePath());
		Process process = processBuilder.start();

		IOUtils.lineIterator(process.getInputStream(), Charset.defaultCharset())
			.forEachRemaining(log::info);

		process.waitFor();
		int exitValue = process.exitValue();

		if(exitValue != 0) {
			throw new GitException();
		}
		log.info("Cloned {} into {}", uri, workingDirectory);
	}

	@Override
	public void init() throws IOException, InterruptedException, GitException {
		ProcessBuilder processBuilder = new ProcessBuilder(GIT, "init").directory(workingDirectory);
		Process process = processBuilder.start();
		process.waitFor();
		int exitValue = process.exitValue();

		if(exitValue != 0) {
			throw new GitException();
		}
		log.info("Initialized repository in {}", workingDirectory);
	}

	@Override
	public boolean pull() throws IOException, InterruptedException, GitException {
		ProcessBuilder processBuilder = new ProcessBuilder(GIT, "pull").directory(workingDirectory);
		Process process = processBuilder.start();
		process.waitFor();
		int exitValue = process.exitValue();

		if(exitValue != 0) {
			throw new GitException();
		}
		log.info("Pulled remote changes into {}", workingDirectory);
		return false;
	}

	@Override
	public void commitChanges() throws IOException, GitException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(GIT, "commit", "-m", "Changed config...").directory(workingDirectory);
		Process process = processBuilder.start();
		process.waitFor();
		int exitValue = process.exitValue();

		if(exitValue != 0) {
			throw new GitException();
		}
		log.info("Committed changes in {}", workingDirectory);
	}

	@Override
	public void push() throws GitException, IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(GIT, "push").directory(workingDirectory);
		Process process = processBuilder.start();
		process.waitFor();

		int exitValue = process.exitValue();

		if(exitValue != 0) {
			throw new GitException();
		}
		log.info("Pushed changes in {} to remote", workingDirectory);
	}

}
