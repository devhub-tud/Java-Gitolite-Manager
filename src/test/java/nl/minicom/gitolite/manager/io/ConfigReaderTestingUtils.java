package nl.minicom.gitolite.manager.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Repository;

import org.junit.Assert;
import org.junit.Ignore;

@Ignore
public class ConfigReaderTestingUtils {

	protected Config read(String fileName) throws IOException {
		InputStream in = getClass().getResourceAsStream("/" + fileName);
		return ConfigReader.read(new InputStreamReader(in));
	}

	protected void verifyConfigsAreTheSame(Config expected, Config actual) {
		Assert.assertEquals(expected.getGroups(), actual.getGroups());
		for (Group expectedGroup : expected.getGroups()) {
			Group actualGroup = actual.getGroup(expectedGroup.getName());
			Assert.assertEquals(expectedGroup.getChildren(), actualGroup.getChildren());
		}

		Assert.assertEquals(expected.getRepositories(), actual.getRepositories());
		for (Repository expectedRepo : expected.getRepositories()) {
			Repository actualRepo = actual.getRepository(expectedRepo.getName());
			Assert.assertEquals(expectedRepo.getPermissions(), actualRepo.getPermissions());
		}
	}

}
