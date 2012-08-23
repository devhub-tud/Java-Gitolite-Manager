package nl.minicom.gitolite.manager.models;

import java.text.MessageFormat;
import java.util.SortedSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class Config {

	private final SortedSet<Repository> repositories;
	private final SortedSet<Group> groups;
	private final SortedSet<User> users;
	
	public Config() {
		this.repositories = Sets.newTreeSet(Repository.SORT_ALPHABETICALLY);
		this.groups = Sets.newTreeSet(Group.SORT_BY_DEPTH);
		this.users = Sets.newTreeSet(User.SORT_BY_TYPE_AND_ALPHABETICALLY);
	}
	
	public Repository ensureRepositoryExists(String repoName) {
		validateRepositoryName(repoName);
		Repository repository = getRepository(repoName);
		if (repository == null) {
			repository = createRepository(repoName);
		}
		return repository;
	}

	public Repository createRepository(String repoName) {
		validateRepositoryName(repoName);
		if (getRepository(repoName) != null) {
			throw new IllegalArgumentException(MessageFormat.format("The repository {0} has already been created!", repoName));
		}
		
		Repository repository = new Repository(repoName);
		repositories.add(repository);
		return repository;
	}
	
	public boolean removeRepository(Repository repository) {
		Preconditions.checkNotNull(repository);
		return repositories.remove(repository);
	}
	
	public boolean hasRepository(String repoName) {
		validateRepositoryName(repoName);
		return getRepository(repoName) != null;
	}
	
	public Repository getRepository(String repoName) {
		validateRepositoryName(repoName);
		for (Repository repository : repositories) {
			if (repository.getName().equals(repoName)) {
				return repository;
			}
		}
		return null;
	}
	
	public ImmutableSet<Repository> getRepositories() {
		return ImmutableSet.copyOf(repositories);
	}
	
	private void validateRepositoryName(String repoName) {
		Preconditions.checkNotNull(repoName);
		Preconditions.checkArgument(!repoName.isEmpty());
	}
	
	public Group ensureGroupExists(String groupName) {
		validateGroupName(groupName);
		Group group = getGroup(groupName);
		if (group == null) {
			group = createGroup(groupName);
		}
		return group;
	}

	public Group createGroup(String groupName) {
		validateGroupName(groupName);
		if (getGroup(groupName) != null) {
			throw new IllegalArgumentException(MessageFormat.format("The group {0} has already been created!", groupName));
		}
		
		Group group = new Group(groupName);
		groups.add(group);
		return group;
	}
	
	public boolean removeGroup(Group group) {
		Preconditions.checkNotNull(group);
		return groups.remove(group);
	}
	
	public boolean hasGroup(String groupName) {
		validateGroupName(groupName);
		return getGroup(groupName) != null;
	}
	
	public Group getGroup(String groupName) {
		validateGroupName(groupName);
		for (Group group : groups) {
			if (group.getName().equals(groupName)) {
				return group;
			}
		}
		return null;
	}
	
	public ImmutableSet<Group> getGroups() {
		return ImmutableSet.copyOf(groups);
	}
	
	private void validateGroupName(String groupName) {
		Preconditions.checkNotNull(groupName);
		Preconditions.checkArgument(!groupName.isEmpty());
		Preconditions.checkArgument(groupName.startsWith("@"));
	}
	
	public User ensureUserExists(String userName) {
		validateUserName(userName);
		User user = getUser(userName);
		if (user == null) {
			user = createUser(userName);
		}
		return user;
	}

	public User createUser(String userName) {
		validateUserName(userName);
		if (getUser(userName) != null) {
			throw new IllegalArgumentException(MessageFormat.format("The user {0} has already been created!", userName));
		}
		
		User user = new User(userName);
		users.add(user);
		return user;
	}
	
	public boolean removeUser(User user) {
		Preconditions.checkNotNull(user);
		boolean success = users.remove(user);
		
		for (Repository repo : repositories) {
			repo.revokePermissions(user);
		}
		
		return success;
	}
	
	public boolean hasUser(String userName) {
		validateUserName(userName);
		return getUser(userName) != null;
	}
	
	public User getUser(String userName) {
		validateUserName(userName);
		for (User user : users) {
			if (user.getName().equals(userName)) {
				return user;
			}
		}
		return null;
	}
	
	public ImmutableSet<User> getUsers() {
		return ImmutableSet.copyOf(users);
	}
	
	private void validateUserName(String userName) {
		Preconditions.checkNotNull(userName);
		Preconditions.checkArgument(!userName.isEmpty());
	}

}
