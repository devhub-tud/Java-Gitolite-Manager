package nl.minicom.gitolite.manager.models;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * This class represents a {@link InternalGroup} in the gitolite configuration.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class InternalGroup implements Identifiable, Group {
	
	private final String name;
	private final SortedSet<Group> groups;
	private final SortedSet<User> users;
	
	/**
	 * Constructs a new {@link InternalGroup} object with the specified name.
	 * 
	 * @param name
	 * 	The name of the group. The name must be a non-null, not-empty value.
	 */
	InternalGroup(String name) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkArgument(name.startsWith("@"));
		
		this.name = name;
		this.groups = Sets.newTreeSet(Group.SORT_BY_NAME);
		this.users = Sets.newTreeSet(User.SORT_BY_NAME);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void add(Group group) {
		Preconditions.checkArgument(!isAllGroup());
		Preconditions.checkNotNull(group);
		if (groups.contains(group)) {
			throw new IllegalArgumentException("Cannot add group: '" + group.getName() + "'. It's already added!");
		}
		groups.add(group);
	}
	
	@Override
	public Set<Group> getGroups() {
		return Collections.unmodifiableSortedSet(groups);
	}
	
	@Override
	public boolean containsGroup(Group group) {
		Preconditions.checkNotNull(group);
		if (getGroups().contains(group)) {
			return true;
		}
		for (Group child : getGroups()) {
			if (child.containsGroup(group)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void add(User user) {
		Preconditions.checkArgument(!isAllGroup());
		Preconditions.checkNotNull(user);
		if (users.contains(user)) {
			throw new IllegalArgumentException("Cannot add user: '" + user.getName() + "'. It's already added!");
		}
		users.add(user);
	}
	
	@Override
	public Set<User> getUsers() {
		return Collections.unmodifiableSortedSet(users);
	}
	
	@Override
	public boolean containsUser(User user) {
		Preconditions.checkNotNull(user);
		return getUsers().contains(user);
	}
	
	private boolean isAllGroup() {
		return name.equals("@all");
	}

	@Override
	public Set<Identifiable> getMembers() {
		Set<Identifiable> members = Sets.newTreeSet(Identifiable.SORT_BY_TYPE_AND_NAME);
		members.addAll(groups);
		members.addAll(users);
		return members;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(name)
			.toHashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof InternalGroup)) {
			return false;
		}
		
		return new EqualsBuilder()
			.append(name, ((InternalGroup) other).name)
			.isEquals();
	}
	
	@Override
	public String toString() {
		return name;
	}

}
