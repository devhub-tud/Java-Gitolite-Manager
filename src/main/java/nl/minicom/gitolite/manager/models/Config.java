package nl.minicom.gitolite.manager.models;

import java.util.Map.Entry;
import java.util.SortedSet;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.models.Recorder.Modification;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;

/**
 * The {@link ConfigModelModel} class is a representation of a configuration of gitolite.
 * If you wish to change your gitolite configuration, You will have to manipulate this object.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class Config {

	private final SortedSet<Repository> repositories;
	private final SortedSet<Group> groups;
	private final SortedSet<User> users;
	private final Recorder recorder;

	/**
	 * This constructs a new {@link ConfigModelModel} object.
	 */
	Config() {
		this(new Recorder());
	}
	
	/**
	 * This constructs a new {@link ConfigModelModel} object.
	 * 
	 * @param recorder
	 * 	The {@link Recorder} to use when modifying this {@link Config} object.
	 */
	Config(Recorder recorder) {
		Preconditions.checkNotNull(recorder);
		
		this.recorder = recorder;
		this.repositories = Sets.newTreeSet(Repository.SORT_BY_NAME);
		this.groups = Sets.newTreeSet(Group.SORT_BY_NAME);
		this.users = Sets.newTreeSet(User.SORT_BY_TYPE_AND_NAME);
	}
	
	/**
	 * @return The {@link Recorder} used by this {@link Config} to record changes.
	 */
	Recorder getRecorder() {
		return recorder;
	}

	/**
	 * This method ensures that the {@link ConfigModel} object will contain a {@link Repository}
	 * with the specified name. This means that if no such {@link Repository} exists, it will
	 * be created.
	 * 
	 * @param repoName
	 * 	The name the {@link Repository} should have. 
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	The existing or newly created {@link Repository} object.
	 */
	public Repository ensureRepositoryExists(String repoName) {
		validateRepositoryName(repoName);
		
		synchronized (repositories) {
			Repository repository = getRepository(repoName);
			if (repository == null) {
				repository = createRepository(repoName);
			}
			return repository;
		}
	}

	/**
	 * This method creates a new {@link Repository} with the specified name.
	 * 
	 * @param repoName
	 * 	The name of the {@link Repository}. This may not be NULL or an empty {@link String}.
	 * 	If the {@link Repository} already exists a {@link IllegalArgumentException} is thrown. 
	 * 	Use the {@link ConfigModel#ensureRepositoryExists(String)} method in stead.
	 * 
	 * @return
	 * 	The created {@link Repository} object.
	 */
	public Repository createRepository(final String repoName) {
		validateRepositoryName(repoName);
		
		Repository repository = null;
		synchronized (repositories) {
			if (getRepository(repoName) != null) {
				throw new IllegalArgumentException("The repository " + repoName + " has already been created!");
			}
			
			repository = new Repository(repoName, recorder);
			repositories.add(repository);
		}
		
		recorder.append(new Modification("Create repository: " + repoName) {
			@Override
			public void apply(Config config) throws ModificationException {
				try {
					config.createRepository(repoName);
				}
				catch (IllegalArgumentException e) {
					throw new ModificationException();
				}
			}
		});
		
		return repository;
	}

	/**
	 * This method removes the specified {@link Repository} from the {@link ConfigModel} object.
	 * 
	 * @param repository
	 * 	The {@link Repository} to remove. This may not be NULL.
	 * 
	 * @return
	 * 	True if it was removed, or false if it was not. 
	 * 	In the latter case it most likely did not exist.
	 */
	public boolean removeRepository(Repository repository) {
		Preconditions.checkNotNull(repository);

		boolean removed = false;
		synchronized (repositories) {
			removed = repositories.remove(repository);
		}
		
		final String repoName = repository.getName();
		recorder.append(new Modification("Remove repository: " + repository.getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				Repository repo = config.getRepository(repoName);
				if (repo == null) {
					throw new ModificationException();
				}
				
				config.removeRepository(repo);
			}
		});
		
		return removed;
	}

	/**
	 * This method checks if the {@link ConfigModel} object contains a {@link Repository}
	 * object with the specified name.
	 * 
	 * @param repoName
	 * 	The name of the {@link Repository} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	True if a {@link Repository} with the specified name exists, false otherwise.
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
	 * 	The name of the {@link Repository} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	The {@link Repository} object with the specified name, 
	 * 	or NULL if no such {@link Repository} exists.
	 */
	public Repository getRepository(String repoName) {
		validateRepositoryName(repoName);

		synchronized (repositories) {
			for (Repository repository : repositories) {
				if (repository.getName().equals(repoName)) {
					return repository;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @return
	 * 	Am {@link ImmutableSet} of {@link Repository} objects currently 
	 * 	registered in the {@link Config} object.
	 */
	public ImmutableSet<Repository> getRepositories() {
		synchronized (repositories) {
			return ImmutableSortedSet.copyOf(Repository.SORT_BY_NAME, repositories);
		}
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
	 * 	The name the {@link Group} should have.
	 * 	This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 	The existing or newly created {@link Group} object.
	 */
	public Group ensureGroupExists(String groupName) {
		validateGroupName(groupName);

		synchronized (groups) {
			Group group = getGroup(groupName);
			if (group == null) {
				group = createGroup(groupName);
			}
			return group;
		}
	}

	/**
	 * This method creates a new {@link Group} with the specified name.
	 * 
	 * @param groupName
	 * 	The name of the {@link Group}. This may not be NULL, and it must start with the character '@'.
	 * 	If the {@link Group} already exists an {@link IllegalArgumentException} is thrown. Use the 
	 * 	{@link Config#ensureGroupExists(String)} method in stead.
	 * 
	 * @return
	 * 	The created {@link Group} object.
	 */
	public Group createGroup(final String groupName) {
		validateGroupName(groupName);

		Group group = null;
		synchronized (groups) {
			if (getGroup(groupName) != null) {
				throw new IllegalArgumentException("The group " + groupName + " has already been created!");
			}
			
			group = new Group(groupName, recorder);
			groups.add(group);
		}
		
		recorder.append(new Modification("Creating group: " + groupName) {
			@Override
			public void apply(Config config) throws ModificationException {
				try {
					config.createGroup(groupName);
				}
				catch (IllegalArgumentException e) {
					throw new ModificationException();
				}
			}
		});
		
		return group;
	}

	/**
	 * This method removes the specified {@link Repository} from the {@link Group} object.
	 * 
	 * @param group
	 * 	The {@link Group} to remove. This may not be NULL.
	 * 
	 * @return
	 * 	True if it was removed, or false if it was not. 
	 * 	In the latter case it most likely did not exist.
	 */
	public boolean removeGroup(Group group) {
		Preconditions.checkNotNull(group);

		boolean remove = false;
		synchronized (groups) {
			remove = groups.remove(group);
		}
		
		if (remove) {
			synchronized (repositories) {
				for (Repository repo : repositories) {
					repo.revokePermissions(group);
				}
			}
			
			final String groupName = group.getName();
			recorder.append(new Modification("Removing group: " + groupName) {
				@Override
				public void apply(Config config) throws ModificationException {
					Group group = config.getGroup(groupName);
					if (group == null || !config.removeGroup(group)) {
						throw new ModificationException();
					}
				}
			});
		}
		
		return remove;
	}

	/**
	 * This method checks if the {@link Config} object contains a {@link Group}
	 * object with the specified name.
	 * 
	 * @param groupName
	 * 	The name of the {@link Group} to look for.
	 * 	This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 	True if a {@link Group} with the specified name exists, false otherwise.
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
	 * 	The name of the {@link Group} to look for.
	 * 	This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 	The {@link Group} object with the specified name, 
	 * 	or NULL if no such {@link Group} exists.
	 */
	public Group getGroup(String groupName) {
		validateGroupName(groupName);
		
		synchronized (groups) {
			for (Group group : groups) {
				if (group.getName().equals(groupName)) {
					return group;
				}
			}
		}
		return null;
	}

	/**
	 * @return
	 * 	Am {@link ImmutableSet} of {@link Group} objects currently 
	 * 	registered in the {@link Config} object. This includes the "@all" {@link Group}.
	 */
	public ImmutableSet<Group> getGroups() {
		synchronized (groups) {
			return ImmutableSortedSet.copyOf(Group.SORT_BY_NAME, groups);
		}
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
	 * 	The name the {@link User} should have.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	The existing or newly created {@link User} object.
	 */
	public User ensureUserExists(String userName) {
		validateUserName(userName);

		synchronized (users) {
			User user = getUser(userName);
			if (user == null) {
				user = createUser(userName);
			}
			return user;
		}
	}

	/**
	 * This method creates a new {@link User} with the specified name.
	 * 
	 * @param userName
	 * 	The name of the {@link User}. This may not be NULL or an empty {@link String}.
	 * 	If the {@link User} already exists, a {@link IllegalArgumentException} is thrown. 
	 * 	Use the {@link Config#ensureUserExists(String)} method in stead.
	 * 
	 * @return
	 * 	The created {@link User} object.
	 * 
	 * @throws IllegalArgumentException
	 */
	public User createUser(final String userName) {
		validateUserName(userName);

		User user = null;
		synchronized (users) {
			if (getUser(userName) != null) {
				throw new IllegalArgumentException("The user " + userName + " has already been created!");
			}
			
			user = new User(userName, recorder);
			users.add(user);
		}

		recorder.append(new Modification("Creating user: " + userName) {
			@Override
			public void apply(Config config) throws ModificationException {
				try {
					config.createUser(userName);
				}
				catch (IllegalArgumentException e) {
					throw new ModificationException();
				}
			}
		});
		
		return user;
	}

	/**
	 * This method removes the specified {@link Repository} from the {@link User} object.
	 * 
	 * @param user
	 * 	The {@link User} to remove. This may not be NULL.
	 * 
	 * @return
	 * 	True if it was removed, or false if it was not. 
	 * 	In the latter case it most likely did not exist.
	 */
	public boolean removeUser(User user) {
		Preconditions.checkNotNull(user);

		boolean success = false;
		synchronized (users) {
			success = users.remove(user);
		}
		
		if (success) {
			synchronized (repositories) {
				for (Repository repo : repositories) {
					repo.revokePermissions(user);
				}
			}
	
			final String userName = user.getName();
			recorder.append(new Modification("Removing user: " + userName) {
				@Override
				public void apply(Config config) throws ModificationException {
					User user = config.getUser(userName);
					if (user == null || !config.removeUser(user)) {
						throw new ModificationException();
					}
				}
			});
		}
		
		return success;
	}

	/**
	 * This method checks if the {@link Config} object contains a {@link User}
	 * object with the specified name.
	 * 
	 * @param userName
	 * 	The name of the {@link User} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	True if a {@link User} with the specified name exists, false otherwise.
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
	 * 	The name of the {@link User} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	The {@link User} object with the specified name,
	 * 	or NULL if no such {@link User} exists.
	 */
	public User getUser(String userName) {
		validateUserName(userName);
		synchronized (users) {
			for (User user : users) {
				if (user.getName().equals(userName)) {
					return user;
				}
			}
		}
		return null;
	}

	/**
	 * @return
	 * 	Am {@link ImmutableSet} of {@link User} objects currently
	 * 	registered in the {@link Config} object.
	 */
	public ImmutableSet<User> getUsers() {
		synchronized (users) {
			return ImmutableSortedSet.copyOf(User.SORT_BY_TYPE_AND_NAME, users);
		}
	}
	
	private void validateUserName(String userName) {
		Preconditions.checkNotNull(userName);
		Preconditions.checkArgument(!userName.isEmpty());
	}
	
	/**
	 * @return The created deep copy of this {@link Config} object.
	 */
	Config copy() {
		Config config = new Config();
		
		// Add users.
		for (User user : getUsers()) {
			User created = config.createUser(user.getName());
			for (Entry<String, String> entry : user.getKeys().entrySet()) {
				created.setKey(entry.getKey(), entry.getValue());
			}
		}
		
		// Add groups and their users.
		for (Group group : getGroups()) {
			Group created = config.createGroup(group.getName());
			for (User member : group.getUsers()) {
				created.add(member);
			}
		}
		
		// Add sub groups to groups.
		for (Group group : getGroups()) {
			Group existing = config.getGroup(group.getName());
			for (Group member : group.getGroups()) {
				existing.add(config.getGroup(member.getName()));
			}
		}
		
		// Add repositories.
		for (Repository repo : getRepositories()) {
			Repository created = config.createRepository(repo.getName());
			for (Entry<Permission, Identifiable> entry : repo.getPermissions().entries()) {
				if (entry.getValue() instanceof User) {
					User user = config.getUser(entry.getValue().getName());
					created.setPermission(user, entry.getKey());
				}
				else {
					Group group = config.getGroup(entry.getValue().getName());
					created.setPermission(group, entry.getKey());
				}
			}
		}
		
		return config;
	}
	
}
