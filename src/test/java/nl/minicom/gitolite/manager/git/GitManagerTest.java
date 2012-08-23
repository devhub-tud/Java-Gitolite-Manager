package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;


public class GitManagerTest {

	@Test
	public void testConstructorWithValidInputs() {
		new GitManager(Files.createTempDir(), null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullAsWorkingDirectory() {
		new GitManager(null, null);
	}
	
	@Test
	public void testOpenExistingWorkingDirectory() throws IOException {
		File dir = Files.createTempDir();
		new GitManager(dir, null).init();
		
		new GitManager(dir, null).open();
	}
	
	@Test
	public void testCloningExistingRepo() {
		File location = Files.createTempDir();
		new GitManager(location, null).init();
		
		GitManager git = new GitManager(Files.createTempDir(), null);
		git.clone(location.getAbsolutePath());
	}
	
	@Test
	public void testInitializingNewRepo() {
		File dir = Files.createTempDir();
		new GitManager(dir, null).init();
		Assert.assertTrue(new File(dir, ".git").exists());
	}
	
	@Test
	public void testPullingFromExistingRepo() throws IOException {
		File location = Files.createTempDir();
		GitManager orig = new GitManager(location, null);
		orig.init();
		orig.commitChanges();
		
		GitManager git = new GitManager(Files.createTempDir(), null);
		git.clone(location.getAbsolutePath());
		
		FileWriter writer = new FileWriter(new File(location, "test.txt"));
		writer.write("Hello world");
		writer.close();
		
		orig.commitChanges();
		
		Assert.assertTrue(git.pull());
	}
	
	@Test
	public void testCommittingChangesToRepo() {
		File location = Files.createTempDir();
		GitManager orig = new GitManager(location, null);
		orig.init();
		orig.commitChanges();
	}
	
	@Test
	public void testPushingToRemoteRepo() throws IOException {
		File cloneDirectory = Files.createTempDir();
		File workingDirectory = Files.createTempDir();
		File location = Files.createTempDir();
		
		GitManager orig = new GitManager(location, null);
		orig.init();
		orig.commitChanges();
		
		GitManager git = new GitManager(workingDirectory, null);
		git.clone(location.getAbsolutePath());
		
		FileWriter writer = new FileWriter(new File(workingDirectory, "test.txt"));
		writer.write("Hello world");
		writer.close();
		
		git.commitChanges();
		git.push();
		
		GitManager clone = new GitManager(cloneDirectory, null);
		clone.clone(location.getAbsolutePath());
		
		Assert.assertTrue(new File(cloneDirectory, "test.txt").exists());
	}

}
