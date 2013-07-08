package nl.minicom.gitolite.manager.models;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface Config {

	/**
	 * This method ensures that the {@link InternalConfig} object will contain a {@link Repository}
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
	Repository ensureRepositoryExists(String repoName);

	/**
	 * This method creates a new {@link Repository} with the specified name.
	 * 
	 * @param repoName
	 * 	The name of the {@link Repository}. This may not be NULL or an empty {@link String}.
	 * 	If the {@link Repository} already exists a {@link IllegalArgumentException} is thrown. 
	 * 	Use the {@link InternalConfig#ensureRepositoryExists(String)} method in stead.
	 * 
	 * @return
	 * 	The created {@link Repository} object.
	 */
	Repository createRepository(String repoName);

	/**
	 * This method removes the specified {@link Repository} from the {@link InternalConfig} object.
	 * 
	 * @param repository
	 * 	The {@link Repository} to remove. This may not be NULL.
	 * 
	 * @return
	 * 	True if it was removed, or false if it was not. 
	 * 	In the latter case it most likely did not exist.
	 */
	boolean removeRepository(Repository repository);

	/**
	 * This method checks if the {@link InternalConfig} object contains a {@link Repository}
	 * object with the specified name.
	 * 
	 * @param repoName
	 * 	The name of the {@link Repository} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	True if a {@link Repository} with the specified name exists, false otherwise.
	 */
	boolean hasRepository(String repoName);

	/**
	 * This method fetches the {@link Repository} object with the specified name 
	 * from the {@link InternalConfig} object.
	 * 
	 * @param repoName
	 * 	The name of the {@link Repository} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	The {@link Repository} object with the specified name, 
	 * 	or NULL if no such {@link Repository} exists.
	 */
	Repository getRepository(String repoName);

	/**
	 * @return
	 * 	Am {@link ImmutableSet} of {@link Repository} objects currently 
	 * 	registered in the {@link InternalConfig} object.
	 */
	Set<Repository> getRepositories();

	/**
	 * This method ensures that the {@link InternalConfig} object will contain a {@link InternalGroup}
	 * with the specified name. This means that if no such {@link InternalGroup} exists, it will
	 * be created.
	 * 
	 * @param groupName
	 * 	The name the {@link InternalGroup} should have.
	 * 	This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 	The existing or newly created {@link InternalGroup} object.
	 */
	InternalGroup ensureGroupExists(String groupName);

	/**
	 * This method creates a new {@link InternalGroup} with the specified name.
	 * 
	 * @param groupName
	 * 	The name of the {@link InternalGroup}. This may not be NULL, and it must start with the character '@'.
	 * 	If the {@link InternalGroup} already exists an {@link IllegalArgumentException} is thrown. Use the 
	 * 	{@link InternalConfig#ensureGroupExists(String)} method in stead.
	 * 
	 * @return
	 * 	The created {@link InternalGroup} object.
	 */
	InternalGroup createGroup(String groupName);

	/**
	 * This method removes the specified {@link Repository} from the {@link InternalGroup} object.
	 * 
	 * @param group
	 * 	The {@link InternalGroup} to remove. This may not be NULL.
	 * 
	 * @return
	 * 	True if it was removed, or false if it was not. 
	 * 	In the latter case it most likely did not exist.
	 */
	boolean removeGroup(InternalGroup group);

	/**
	 * This method checks if the {@link InternalConfig} object contains a {@link InternalGroup}
	 * object with the specified name.
	 * 
	 * @param groupName
	 * 	The name of the {@link InternalGroup} to look for.
	 * 	This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 	True if a {@link InternalGroup} with the specified name exists, false otherwise.
	 */
	boolean hasGroup(String groupName);

	/**
	 * This method fetches the {@link InternalGroup} object with the specified name 
	 * from the {@link InternalConfig} object.
	 * 
	 * @param groupName
	 * 	The name of the {@link InternalGroup} to look for.
	 * 	This may not be NULL, and it must start with the character '@'.
	 * 
	 * @return
	 * 	The {@link InternalGroup} object with the specified name, 
	 * 	or NULL if no such {@link InternalGroup} exists.
	 */
	Group getGroup(String groupName);

	/**
	 * @return
	 * 	Am {@link ImmutableSet} of {@link InternalGroup} objects currently 
	 * 	registered in the {@link InternalConfig} object. This includes the "@all" {@link InternalGroup}.
	 */
	Set<InternalGroup> getGroups();

	/**
	 * This method ensures that the {@link InternalConfig} object will contain a {@link User}
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
	User ensureUserExists(String userName);

	/**
	 * This method creates a new {@link User} with the specified name.
	 * 
	 * @param userName
	 * 	The name of the {@link User}. This may not be NULL or an empty {@link String}.
	 * 	If the {@link User} already exists, a {@link IllegalArgumentException} is thrown. 
	 * 	Use the {@link InternalConfig#ensureUserExists(String)} method in stead.
	 * 
	 * @return
	 * 	The created {@link User} object.
	 * 
	 * @throws IllegalArgumentException
	 */
	User createUser(String userName);

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
	boolean removeUser(User user);

	/**
	 * This method checks if the {@link InternalConfig} object contains a {@link User}
	 * object with the specified name.
	 * 
	 * @param userName
	 * 	The name of the {@link User} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	True if a {@link User} with the specified name exists, false otherwise.
	 */
	boolean hasUser(String userName);

	/**
	 * This method fetches the {@link User} object with the specified name
	 * from the {@link InternalConfig} object.
	 * 
	 * @param userName
	 * 	The name of the {@link User} to look for.
	 * 	This may not be NULL or an empty {@link String}.
	 * 
	 * @return
	 * 	The {@link User} object with the specified name,
	 * 	or NULL if no such {@link User} exists.
	 */
	User getUser(String userName);

	/**
	 * @return
	 * 	Am {@link ImmutableSet} of {@link User} objects currently
	 * 	registered in the {@link InternalConfig} object.
	 */
	Set<User> getUsers();

}