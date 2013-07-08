package nl.minicom.gitolite.manager.models;

import java.util.SortedSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * The {@link InternalConfig} class is a representation of a configuration of gitolite.
 * If you wish to change your gitolite configuration, You will have to manipulate this object.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class InternalConfig implements Config {

	private final SortedSet<Repository> repositories;
	private final SortedSet<InternalGroup> groups;
	private final SortedSet<User> users;

	/**
	 * This constructs a new {@link InternalConfig} object.
	 */
	public InternalConfig() {
		this.repositories = Sets.newTreeSet(Repository.SORT_ALPHABETICALLY);
		this.groups = Sets.newTreeSet(InternalGroup.SORT_BY_NAME);
		this.users = Sets.newTreeSet(User.SORT_BY_TYPE_AND_NAME);
	}
	
	@Override
	public Repository ensureRepositoryExists(String repoName) {
		validateRepositoryName(repoName);
		Repository repository = getRepository(repoName);
		if (repository == null) {
			repository = createRepository(repoName);
		}
		return repository;
	}

	@Override
	public Repository createRepository(String repoName) {
		validateRepositoryName(repoName);
		if (getRepository(repoName) != null) {
			throw new IllegalArgumentException("The repository " + repoName + " has already been created!");
		}
		
		Repository repository = new Repository(repoName);
		repositories.add(repository);
		return repository;
	}
	
	@Override
	public boolean removeRepository(Repository repository) {
		Preconditions.checkNotNull(repository);
		return repositories.remove(repository);
	}
	
	@Override
	public boolean hasRepository(String repoName) {
		validateRepositoryName(repoName);
		return getRepository(repoName) != null;
	}
	
	@Override
	public Repository getRepository(String repoName) {
		validateRepositoryName(repoName);
		for (Repository repository : repositories) {
			if (repository.getName().equals(repoName)) {
				return repository;
			}
		}
		return null;
	}
	
	@Override
	public ImmutableSet<Repository> getRepositories() {
		return ImmutableSet.copyOf(repositories);
	}
	
	private void validateRepositoryName(String repoName) {
		Preconditions.checkNotNull(repoName);
		Preconditions.checkArgument(!repoName.isEmpty());
	}

	@Override
	public InternalGroup ensureGroupExists(String groupName) {
		validateGroupName(groupName);
		InternalGroup group = getGroup(groupName);
		if (group == null) {
			group = createGroup(groupName);
		}
		return group;
	}

	@Override
	public InternalGroup createGroup(String groupName) {
		validateGroupName(groupName);
		if (getGroup(groupName) != null) {
			throw new IllegalArgumentException("The group " + groupName + " has already been created!");
		}
		
		InternalGroup group = new InternalGroup(groupName);
		groups.add(group);
		return group;
	}

	@Override
	public boolean removeGroup(InternalGroup group) {
		Preconditions.checkNotNull(group);
		return groups.remove(group);
	}
	
	@Override
	public boolean hasGroup(String groupName) {
		validateGroupName(groupName);
		return getGroup(groupName) != null;
	}
	
	@Override
	public InternalGroup getGroup(String groupName) {
		validateGroupName(groupName);
		for (InternalGroup group : groups) {
			if (group.getName().equals(groupName)) {
				return group;
			}
		}
		return null;
	}

	@Override
	public ImmutableSet<InternalGroup> getGroups() {
		return ImmutableSet.copyOf(groups);
	}
	
	private void validateGroupName(String groupName) {
		Preconditions.checkNotNull(groupName);
		Preconditions.checkArgument(!groupName.isEmpty());
		Preconditions.checkArgument(groupName.startsWith("@"));
	}

	@Override
	public User ensureUserExists(String userName) {
		validateUserName(userName);
		User user = getUser(userName);
		if (user == null) {
			user = createUser(userName);
		}
		return user;
	}

	@Override
	public User createUser(String userName) {
		validateUserName(userName);
		if (getUser(userName) != null) {
			throw new IllegalArgumentException("The user " + userName + " has already been created!");
		}
		
		User user = new User(userName);
		users.add(user);
		return user;
	}
	
	@Override
	public boolean removeUser(User user) {
		Preconditions.checkNotNull(user);
		boolean success = users.remove(user);
		
		for (Repository repo : repositories) {
			repo.revokePermissions(user);
		}
		
		return success;
	}
	
	@Override
	public boolean hasUser(String userName) {
		validateUserName(userName);
		return getUser(userName) != null;
	}
	
	@Override
	public User getUser(String userName) {
		validateUserName(userName);
		for (User user : users) {
			if (user.getName().equals(userName)) {
				return user;
			}
		}
		return null;
	}
	
	@Override
	public ImmutableSet<User> getUsers() {
		return ImmutableSet.copyOf(users);
	}
	
	private void validateUserName(String userName) {
		Preconditions.checkNotNull(userName);
		Preconditions.checkArgument(!userName.isEmpty());
	}

}
