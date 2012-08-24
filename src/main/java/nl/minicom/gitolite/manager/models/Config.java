package nl.minicom.gitolite.manager.models;

import java.text.MessageFormat;
import java.util.SortedSet;

import nl.minicom.gitolite.manager.ConfigManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * The {@link Config} class is a representation of a configuration of gitolite.
 * If you wish to change your gitolite configuration, You will have to manipulate this object.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class Config {

	private final SortedSet<Repository> repositories;
	private final SortedSet<Group> groups;
	private final SortedSet<User> users;

	/**
	 * This constructs a new {@link Config} object. If you wish to change your gitolite 
	 * configuration, you should use the {@link ConfigManager} in stead, to load and 
	 * persist changes to your configuration.
	 */
	public Config() {
		this.repositories = Sets.newTreeSet(Repository.SORT_ALPHABETICALLY);
		this.groups = Sets.newTreeSet(Group.SORT_BY_DEPTH);
		this.users = Sets.newTreeSet(User.SORT_BY_TYPE_AND_ALPHABETICALLY);
	}
	
	/**
	 * This method ensures that the {@link Config} object will contain a {@link Repository}
	 * with the specified name. This means that if no such {@link Repository} exists, it will
	 * be created.
	 * 
	 * @param repoName
	 * 		The name the {@link Repository} should have. 
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		The existing or newly created {@link Repository} object.
	 */
	public Repository ensureRepositoryExists(String repoName) {
		validateRepositoryName(repoName);
		Repository repository = getRepository(repoName);
		if (repository == null) {
			repository = createRepository(repoName);
		}
		return repository;
	}

	/**
	 * This method creates a new {@link Repository} with the specified name.
	 * 
	 * @param repoName
	 * 		The name of the {@link Repository}.
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		The created {@link Repository} object.
	 * 
	 * @throws IllegalArgumentException
	 * 		If the {@link Repository} already exists. Use the 
	 * 		{@link Config#ensureRepositoryExists(String)} method in stead.
	 */
	public Repository createRepository(String repoName) {
		validateRepositoryName(repoName);
		if (getRepository(repoName) != null) {
			throw new IllegalArgumentException(MessageFormat.format("The repository {0} has already been created!", repoName));
		}
		
		Repository repository = new Repository(repoName);
		repositories.add(repository);
		return repository;
	}
	
	/**
	 * This method removes the specified {@link Repository} from the {@link Config} object.
	 * 
	 * @param repository
	 * 		The {@link Repository} to remove. This may not be NULL.
	 * 
	 * @return
	 * 		True if it was removed, or false if it was not. 
	 * 		In the latter case it most likely did not exist.
	 */
	public boolean removeRepository(Repository repository) {
		Preconditions.checkNotNull(repository);
		return repositories.remove(repository);
	}
	
	/**
	 * This method checks if the {@link Config} object contains a {@link Repository}
	 * object with the specified name.
	 * 
	 * @param repoName
	 * 		The name of the {@link Repository} to look for.
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		True if a {@link Repository} with the specified name exists, false otherwise.
	 */
	public boolean hasRepository(String repoName) {
		validateRepositoryName(repoName);
		return getRepository(repoName) != null;
	}
	
	/**
	 * This method fetches the {@link Repository} object with the specified name 
	 * from the {@link Config} object.
	 * 
	 * @param repoName
	 * 		The name of the {@link Repository} to look for.
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		The {@link Repository} object with the specified name, 
	 * 		or NULL if no such {@link Repository} exists.
	 */
	public Repository getRepository(String repoName) {
		validateRepositoryName(repoName);
		for (Repository repository : repositories) {
			if (repository.getName().equals(repoName)) {
				return repository;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 * 		Am {@link ImmutableSet} of {@link Repository} objects currently 
	 * 		registered in the {@link Config} object.
	 */
	public ImmutableSet<Repository> getRepositories() {
		return ImmutableSet.copyOf(repositories);
	}
	
	private void validateRepositoryName(String repoName) {
		Preconditions.checkNotNull(repoName);
		Preconditions.checkArgument(!repoName.isEmpty());
	}

	/**
	 * This method ensures that the {@link Config} object will contain a {@link Group}
	 * with the specified name. This means that if no such {@link Group} exists, it will
	 * be created.
	 * 
	 * @param groupName
	 * 		The name the {@link Group} should have.
	 * 		This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 		The existing or newly created {@link Group} object.
	 */
	public Group ensureGroupExists(String groupName) {
		validateGroupName(groupName);
		Group group = getGroup(groupName);
		if (group == null) {
			group = createGroup(groupName);
		}
		return group;
	}

	/**
	 * This method creates a new {@link Group} with the specified name.
	 * 
	 * @param groupName
	 * 		The name of the {@link Group}.
	 * 		This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 		The created {@link Group} object.
	 * 
	 * @throws IllegalArgumentException
	 * 		If the {@link Group} already exists. Use the 
	 * 		{@link Config#ensureGroupExists(String)} method in stead.
	 */
	public Group createGroup(String groupName) {
		validateGroupName(groupName);
		if (getGroup(groupName) != null) {
			throw new IllegalArgumentException(MessageFormat.format("The group {0} has already been created!", groupName));
		}
		
		Group group = new Group(groupName);
		groups.add(group);
		return group;
	}

	/**
	 * This method removes the specified {@link Repository} from the {@link Group} object.
	 * 
	 * @param group
	 * 		The {@link Group} to remove. This may not be NULL.
	 * 
	 * @return
	 * 		True if it was removed, or false if it was not. 
	 * 		In the latter case it most likely did not exist.
	 */
	public boolean removeGroup(Group group) {
		Preconditions.checkNotNull(group);
		return groups.remove(group);
	}
	
	/**
	 * This method checks if the {@link Config} object contains a {@link Group}
	 * object with the specified name.
	 * 
	 * @param groupName
	 * 		The name of the {@link Group} to look for.
	 * 		This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 		True if a {@link Group} with the specified name exists, false otherwise.
	 */
	public boolean hasGroup(String groupName) {
		validateGroupName(groupName);
		return getGroup(groupName) != null;
	}
	
	/**
	 * This method fetches the {@link Group} object with the specified name 
	 * from the {@link Config} object.
	 * 
	 * @param groupName
	 * 		The name of the {@link Group} to look for.
	 * 		This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 		The {@link Group} object with the specified name, 
	 * 		or NULL if no such {@link Group} exists.
	 */
	public Group getGroup(String groupName) {
		validateGroupName(groupName);
		for (Group group : groups) {
			if (group.getName().equals(groupName)) {
				return group;
			}
		}
		return null;
	}

	/**
	 * @return
	 * 		Am {@link ImmutableSet} of {@link Group} objects currently 
	 * 		registered in the {@link Config} object. This includes the "@all" {@link Group}.
	 */
	public ImmutableSet<Group> getGroups() {
		return ImmutableSet.copyOf(groups);
	}
	
	private void validateGroupName(String groupName) {
		Preconditions.checkNotNull(groupName);
		Preconditions.checkArgument(!groupName.isEmpty());
		Preconditions.checkArgument(groupName.startsWith("@"));
	}

	/**
	 * This method ensures that the {@link Config} object will contain a {@link User}
	 * with the specified name. This means that if no such {@link User} exists, it will
	 * be created.
	 * 
	 * @param userName
	 * 		The name the {@link User} should have.
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		The existing or newly created {@link User} object.
	 */
	public User ensureUserExists(String userName) {
		validateUserName(userName);
		User user = getUser(userName);
		if (user == null) {
			user = createUser(userName);
		}
		return user;
	}

	/**
	 * This method creates a new {@link User} with the specified name.
	 * 
	 * @param repoName
	 * 		The name of the {@link User}.
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		The created {@link User} object.
	 * 
	 * @throws IllegalArgumentException
	 * 		If the {@link User} already exists. Use the 
	 * 		{@link Config#ensureUserExists(String)} method in stead.
	 */
	public User createUser(String userName) {
		validateUserName(userName);
		if (getUser(userName) != null) {
			throw new IllegalArgumentException(MessageFormat.format("The user {0} has already been created!", userName));
		}
		
		User user = new User(userName);
		users.add(user);
		return user;
	}
	
	/**
	 * This method removes the specified {@link Repository} from the {@link User} object.
	 * 
	 * @param user
	 * 		The {@link User} to remove. This may not be NULL.
	 * 
	 * @return
	 * 		True if it was removed, or false if it was not. 
	 * 		In the latter case it most likely did not exist.
	 */
	public boolean removeUser(User user) {
		Preconditions.checkNotNull(user);
		boolean success = users.remove(user);
		
		for (Repository repo : repositories) {
			repo.revokePermissions(user);
		}
		
		return success;
	}
	
	/**
	 * This method checks if the {@link Config} object contains a {@link User}
	 * object with the specified name.
	 * 
	 * @param userName
	 * 		The name of the {@link User} to look for.
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		True if a {@link User} with the specified name exists, false otherwise.
	 */
	public boolean hasUser(String userName) {
		validateUserName(userName);
		return getUser(userName) != null;
	}
	
	/**
	 * This method fetches the {@link User} object with the specified name 
	 * from the {@link Config} object.
	 * 
	 * @param userName
	 * 		The name of the {@link User} to look for.
	 * 		This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 		The {@link User} object with the specified name, 
	 * 		or NULL if no such {@link User} exists.
	 */
	public User getUser(String userName) {
		validateUserName(userName);
		for (User user : users) {
			if (user.getName().equals(userName)) {
				return user;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 * 		Am {@link ImmutableSet} of {@link User} objects currently 
	 * 		registered in the {@link Config} object.
	 */
	public ImmutableSet<User> getUsers() {
		return ImmutableSet.copyOf(users);
	}
	
	private void validateUserName(String userName) {
		Preconditions.checkNotNull(userName);
		Preconditions.checkArgument(!userName.isEmpty());
	}

}
