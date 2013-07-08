package nl.minicom.gitolite.manager.models;

import java.util.Comparator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface Group extends Identifiable {

	static final Comparator<Group> SORT_BY_NAME = new Comparator<Group>() {
		@Override
		public int compare(Group arg0, Group arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	};

	/**
	 * @return
	 * 	The name of this {@link Group}.
	 */
	String getName();

	/**
	 * This method adds a child {@link Group} object to the {@link Group}.
	 * 
	 * @param group
	 * 	The {@link Group} to add to this {@link Group}. This may not be NULL.
	 * 	In case the {@link Group} is already a member of this group an 
	 * 	{@link IllegalArgumentException} is thrown.
	 */
	void add(Group group);

	/**
	 * This method adds a {@link User} object to the {@link Group}.
	 * 
	 * @param user
	 * 	The {@link User} to add to this {@link Group}. This may not be NULL.
	 * 	In case the {@link User} is already a member of this group an 
	 * 	{@link IllegalArgumentException} is thrown.
	 */
	void add(User user);
	
	/**
	 * @return
	 * 	An {@link ImmutableSet} of {@link User}s of this {@link Group}.
	 */
	Set<User> getUsers();

	/**
	 * @return
	 * 	An {@link ImmutableSet} of child {@link Group}s of this {@link Group}.
	 */
	Set<Group> getGroups();

	/**
	 * This method returns true if the specified {@link User} is a member 
	 * of this {@link Group}.
	 * 
	 * @param user
	 * 	The {@link User} to look for.
	 * 
	 * @return
	 * 	True if the specified {@link User} is a member of this {@link Group}.
	 */
	boolean containsUser(User user);

	/**
	 * This method returns true if the specified {@link Group} is a child 
	 * of this {@link Group}.
	 * 
	 * @param group
	 * 	The {@link Group} to look for.
	 * 
	 * @return
	 * 	True if the specified {@link Group} is a child of this {@link Group}.
	 */
	boolean containsGroup(Group group);

	/**
	 * @return
	 * 	A {@link Set} containing all {@link User}s and {@link Group}s.
	 */
	Set<Identifiable> getMembers();
	
}