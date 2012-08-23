package nl.minicom.gitolite;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import nl.minicom.gitolite.Config;
import nl.minicom.gitolite.ConfigReader;
import nl.minicom.gitolite.Group;
import nl.minicom.gitolite.Repository;

import org.junit.Ignore;

@Ignore
public class ConfigReaderTestingUtils {

	protected Config read(String fileName) throws IOException {
		InputStream in = getClass().getResourceAsStream("/" + fileName);
		return new ConfigReader().read(new InputStreamReader(in));
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
