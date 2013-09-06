package nl.minicom.gitolite.manager.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

/**
 * This class contains a method to read a gitolite configuration file, and
 * parsing it. This allows you to obtain a {@link Config} object based on the
 * configuration file
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class ConfigReader {

	/**
	 * This method reads the configuration file from the specified {@link Reader}, and creates
	 * a {@link Config} object from it.
	 * 
	 * @param reader
	 * 	The {@link Reader} which allows us to read the configuration file. This cannot be NULL.
	 * 
	 * @return
	 * 	The constructed {@link Config} object.
	 * 
	 * @throws IOException
	 * 	If the configuration file could not be read, or was syntactically incorrect.
	 */
	public static Config read(Reader reader) throws IOException {
		Preconditions.checkNotNull(reader);
		
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			return parseConfig(bufferedReader);
		}
		finally {
			bufferedReader.close();
		}
	}

	private static Config parseConfig(BufferedReader reader) throws IOException {
		Config config = new Config();
		
		String line;
		Repository currentRepo = null;
		int lineNumber = 0;
		while ((line = reader.readLine()) != null) {
			lineNumber++;
			line = line.trim();
			line = line.replaceAll("#.*", "");
			
			if (!line.isEmpty()) {
				if (line.charAt(0) == '@') {
					addIdentifiablesToGroup(config, line);
				}
				else if (line.startsWith("repo")) {
					currentRepo = createRepo(config, line);
				}
				else if (currentRepo != null) {
					createPermissionRule(config, currentRepo, line);
				}
				else {
					throw new IllegalArgumentException("Incorrect syntax at line: " + lineNumber);
				}
			}
		}
		return config;
	}

	private static void addIdentifiablesToGroup(Config config, String line) {
		int indexOfEqualsSign = line.indexOf('=');
		String groupName = line.substring(0, indexOfEqualsSign).trim();
		String identifiables = line.substring(indexOfEqualsSign + 1).trim();
		Iterable<String> ids = Splitter.on(' ').omitEmptyStrings().split(identifiables);
		
		Group group = config.ensureGroupExists(groupName);
		for (String id : ids) {
			if (id.startsWith("@")) {
				group.add(config.ensureGroupExists(id));
			}
			else {
				group.add(config.ensureUserExists(id));
			}
		}
	}

	private static Repository createRepo(Config config, String line) {
		String repoName = line.substring("repo".length()).trim();
		return config.createRepository(repoName);
	}

	private static void createPermissionRule(Config config, Repository currentRepo, String line) {
		int indexOfEqualsSign = line.indexOf('=');
		String permissionLevel = line.substring(0, indexOfEqualsSign).trim();
		Permission permission = Permission.getByLevel(permissionLevel);
		String identifiables = line.substring(indexOfEqualsSign + 1).trim();
		Iterable<String> ids = Splitter.on(' ').omitEmptyStrings().split(identifiables);

		for (String id : ids) {
			if (id.charAt(0) == '@') {
				Group group = config.ensureGroupExists(id);
				currentRepo.setPermission(group, permission);
			}
			else {
				User user = config.ensureUserExists(id);
				currentRepo.setPermission(user, permission);
			}
		}
	}
	
	private ConfigReader() {
		//prevent instantiation.
	}
	
}
