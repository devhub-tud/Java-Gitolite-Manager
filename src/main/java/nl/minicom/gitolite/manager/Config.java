package nl.minicom.gitolite.manager;

import java.util.List;
import java.util.Set;

import nl.minicom.gitolite.manager.Changes.Change;
import nl.minicom.gitolite.manager.models.Group;
import nl.minicom.gitolite.manager.models.InternalGroup;
import nl.minicom.gitolite.manager.models.Repository;
import nl.minicom.gitolite.manager.models.User;

public class Config implements nl.minicom.gitolite.manager.models.Config {

	private final nl.minicom.gitolite.manager.models.Config config;
	private final Changes changes;

	public Config(nl.minicom.gitolite.manager.models.Config config) {
		this.config = config;
		this.changes = new Changes();
	}
	
	@Override
	public Repository ensureRepositoryExists(String repoName) {
		return config.ensureRepositoryExists(repoName);
	}

	@Override
	public Repository createRepository(String repoName) {
		Repository repo = config.createRepository(repoName);
		changes.add(Changes.createRepository(repoName));
		return repo;
	}

	@Override
	public boolean removeRepository(Repository repository) {
		boolean removed = config.removeRepository(repository);
		changes.add(Changes.removeRepository(repository));
		return removed;
	}

	@Override
	public boolean hasRepository(String repoName) {
		return config.hasRepository(repoName);
	}

	@Override
	public Repository getRepository(String repoName) {
		return config.getRepository(repoName);
	}

	@Override
	public Set<Repository> getRepositories() {
		return config.getRepositories();
	}

	@Override
	public InternalGroup ensureGroupExists(String groupName) {
		return config.ensureGroupExists(groupName);
	}

	@Override
	public InternalGroup createGroup(String groupName) {
		return config.createGroup(groupName);
	}

	@Override
	public boolean removeGroup(InternalGroup group) {
		return config.removeGroup(group);
	}

	@Override
	public boolean hasGroup(String groupName) {
		return config.hasGroup(groupName);
	}

	@Override
	public Group getGroup(String groupName) {
		return config.getGroup(groupName);
	}

	@Override
	public Set<InternalGroup> getGroups() {
		return config.getGroups();
	}

	@Override
	public User ensureUserExists(String userName) {
		return config.ensureUserExists(userName);
	}

	@Override
	public User createUser(String userName) {
		return config.createUser(userName);
	}

	@Override
	public boolean removeUser(User user) {
		return config.removeUser(user);
	}

	@Override
	public boolean hasUser(String userName) {
		return config.hasUser(userName);
	}

	@Override
	public User getUser(String userName) {
		return config.getUser(userName);
	}

	@Override
	public Set<User> getUsers() {
		return config.getUsers();
	}

	List<Change> listChanges() {
		return changes.listChanges();
	}

}
