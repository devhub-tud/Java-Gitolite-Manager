package nl.tudelft.ewi.gitolite.git;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.*;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class JGitManagerTest {

    @Rule public TemporaryFolder TEMPORARY_FOLDER = new TemporaryFolder();

    private File remote, clone;

    @Before
    public void prepareTest() throws GitAPIException, IOException, URISyntaxException {
        remote = setUpBareRepository("gitolite-admin");
        fillBareRepository(remote);
        clone = cloneRepository(remote);
    }

    @Test
    public void testCommitChangesAddsModifiedFiles() throws Exception {
        File README = new File(clone, "README.md");
        Files.write("Hello world2".getBytes(), README);

        JGitManager jGitManager = getGitManager();
        jGitManager.open();
        jGitManager.commitChanges();
        jGitManager.push();

        clone = cloneRepository(remote);
        README = new File(clone, "README.md");

        assertThat(
            FileUtils.readFileToString(README),
            equalTo("Hello world2")
        );
    }

    @Test
    public void testCommitChangesAddsNewFiles() throws Exception {
        File newFile = new File(clone, "newFile.md");
        Files.write("Hello world".getBytes(), newFile);

        JGitManager jGitManager = getGitManager();
        jGitManager.open();
        jGitManager.commitChanges();
        jGitManager.push();

        clone = cloneRepository(remote);
        newFile = new File(clone, "newFile.md");

        assertTrue(
            newFile.getAbsolutePath() + " was not present",
            newFile.exists()
        );
    }

    @Test
    public void testCommitChangesRemovesRemovedFiles() throws Exception {
        File anotherFile = new File(clone, "anotherFile.md");
        assertTrue(anotherFile.getAbsolutePath() + " was not present", anotherFile.exists());

        FileUtils.forceDelete(new File(clone, "anotherFile.md"));
        JGitManager jGitManager = getGitManager();
        jGitManager.open();
        jGitManager.commitChanges();
        jGitManager.push();

        clone = cloneRepository(remote);
        anotherFile = new File(clone, "anotherFile.md");

        assertFalse(
            anotherFile.getAbsolutePath() + " was present",
            anotherFile.exists()
        );
    }

    private JGitManager getGitManager() {
        return new JGitManager(clone, null);
    }

    File setUpBareRepository(String name) throws GitAPIException {
        File repoFolder = new File(TEMPORARY_FOLDER.getRoot(), name + ".git");
        Git.init()
            .setDirectory(repoFolder)
            .setBare(true)
            .call();
        return repoFolder;
    }

    void fillBareRepository(File remote) throws IOException, GitAPIException, URISyntaxException {
        File repoFolder = Files.createTempDir();
        Git git = Git.init().setDirectory(repoFolder).setBare(false).call();

        File README = new File(repoFolder, "README.md");
        Files.write("Hello world".getBytes(), README);
        git.add().addFilepattern("README.md").call();

        File anotherFile = new File(repoFolder, "anotherFile.md");
        Files.write("Hello world".getBytes(), anotherFile);
        git.add().addFilepattern("anotherFile.md").call();

        git.commit().setMessage("Initial commit").call();

        git.push().setRemote(remote.getAbsolutePath()).call();
        FileUtils.deleteDirectory(repoFolder);
    }

    File cloneRepository(File remote) throws IOException, GitAPIException, URISyntaxException {
        File repoFolder = new File(TEMPORARY_FOLDER.getRoot(), "clone");
        FileUtils.deleteDirectory(repoFolder);
        FileUtils.forceMkdir(repoFolder);
        Git git = Git.init().setBare(false).setDirectory(repoFolder).call();

        RemoteAddCommand remoteAd = git.remoteAdd();
        remoteAd.setName("origin");
        remoteAd.setUri(new URIish(remote.getAbsolutePath()));
        remoteAd.call();

        git.pull().setRemote("origin").call();
        return repoFolder;
    }

}
