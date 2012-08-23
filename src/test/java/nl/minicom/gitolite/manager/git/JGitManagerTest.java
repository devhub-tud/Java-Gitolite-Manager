package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;


public class JGitManagerTest {

	@Test
	public void testConstructorWithValidInputs() {
		new JGitManager(Files.createTempDir(), null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullAsWorkingDirectory() {
		new JGitManager(null, null);
	}
	
	@Test
	public void testGetWorkingDirectory() {
		File workingDirectory = Files.createTempDir();
		JGitManager manager = new JGitManager(workingDirectory, null);
		
		Assert.assertEquals(workingDirectory, manager.getWorkingDirectory());
	}
	
	@Test
	public void testOpenExistingWorkingDirectory() throws IOException {
		File dir = Files.createTempDir();
		new JGitManager(dir, null).init();
		
		new JGitManager(dir, null).open();
	}
	
	@Test
	public void testRemoveFileFromWorkingDirectory() throws IOException {
		File dir = Files.createTempDir();
		JGitManager jGitManager = new JGitManager(dir, null);
		jGitManager.init();
		
		File file = new File(dir, "test.txt");
		FileWriter writer = new FileWriter(file);
		writer.write("Hello world");
		writer.close();
		
		jGitManager.commitChanges();
		jGitManager.remove(file.getName());
		jGitManager.commitChanges();
		
		Assert.assertFalse(file.exists());
	}
	
	@Test
	public void testCloningExistingRepo() {
		File location = Files.createTempDir();
		new JGitManager(location, null).init();
		
		GitManager git = new JGitManager(Files.createTempDir(), null);
		git.clone(location.getAbsolutePath());
	}
	
	@Test
	public void testInitializingNewRepo() {
		File dir = Files.createTempDir();
		new JGitManager(dir, null).init();
		Assert.assertTrue(new File(dir, ".git").exists());
	}
	
	@Test
	public void testPullingFromExistingRepo() throws IOException {
		File location = Files.createTempDir();
		GitManager orig = new JGitManager(location, null);
		orig.init();
		orig.commitChanges();
		
		GitManager git = new JGitManager(Files.createTempDir(), null);
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
		GitManager orig = new JGitManager(location, null);
		orig.init();
		orig.commitChanges();
	}
	
	@Test
	public void testPushingToRemoteRepo() throws IOException {
		File cloneDirectory = Files.createTempDir();
		File workingDirectory = Files.createTempDir();
		File location = Files.createTempDir();
		
		GitManager orig = new JGitManager(location, null);
		orig.init();
		orig.commitChanges();
		
		GitManager git = new JGitManager(workingDirectory, null);
		git.clone(location.getAbsolutePath());
		
		FileWriter writer = new FileWriter(new File(workingDirectory, "test.txt"));
		writer.write("Hello world");
		writer.close();
		
		git.commitChanges();
		git.push();
		
		GitManager clone = new JGitManager(cloneDirectory, null);
		clone.clone(location.getAbsolutePath());
		
		Assert.assertTrue(new File(cloneDirectory, "test.txt").exists());
	}

}
