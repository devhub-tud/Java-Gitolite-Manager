package nl.minicom.gitolite.manager.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.Identifiable;
import nl.minicom.gitolite.manager.models.Permission;
import nl.minicom.gitolite.manager.models.Repository;

import com.google.common.base.Splitter;

public class ConfigReader {

	public Config read(Reader in) throws IOException {
		BufferedReader reader = new BufferedReader(in);
		try {
			return parseConfig(reader);
		}
		finally {
			reader.close();
		}
	}

	private Config parseConfig(BufferedReader reader) throws IOException {
		Config config = new Config();
		
		String line;
		Repository currentRepo = null;
		int lineNumber = 0;
		while ((line = reader.readLine()) != null) {
			lineNumber++;
			line = line.trim();
			line = line.replaceAll("#.*", "");
			
			if (line.isEmpty()) {
				continue;
			}
			
			if (line.startsWith("@")) {
				addIdentifiablesToGroup(config, line);
			}
			else if (line.startsWith("repo")) {
				currentRepo = createRepo(config, line);
			}
			else if (currentRepo != null) {
				createPermissionRule(config, currentRepo, line);
			}
			else {
				throw new IllegalArgumentException("The config file is syntaxtically incorrect at line: " + lineNumber);
			}
		}
		
		return config;
	}

	private void addIdentifiablesToGroup(Config config, String line) {
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

	private Repository createRepo(Config config, String line) {
		String repoName = line.substring(4).trim();
		return config.createRepository(repoName);
	}

	private void createPermissionRule(Config config, Repository currentRepo, String line) {
		int indexOfEqualsSign = line.indexOf('=');
		String permissionName = line.substring(0, indexOfEqualsSign).trim();
		Permission permission = Permission.getByName(permissionName);
		String identifiables = line.substring(indexOfEqualsSign + 1).trim();
		Iterable<String> ids = Splitter.on(' ').omitEmptyStrings().split(identifiables);

		for (String id : ids) {
			Identifiable entity;
			if (id.startsWith("@")) {
				entity = config.ensureGroupExists(id);
			}
			else {
				entity = config.ensureUserExists(id);
			}
			
			currentRepo.setPermission(entity, permission);
		}
	}
	
}
