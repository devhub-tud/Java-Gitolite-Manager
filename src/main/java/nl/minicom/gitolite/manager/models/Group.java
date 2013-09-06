package nl.minicom.gitolite.manager.models;

import java.util.Comparator;
import java.util.SortedSet;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.models.Recorder.Modification;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableSortedSet.Builder;
import com.google.common.collect.Sets;

/**
 * This class represents a {@link Group} in the gitolite configuration.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class Group implements Identifiable {

	static final Comparator<Group> SORT_BY_NAME = new Comparator<Group>() {
		@Override
		public int compare(Group arg0, Group arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	};
	
	private final String name;
	private final Recorder recorder;
	private final SortedSet<Group> groups;
	private final SortedSet<User> users;
	
	/**
	 * Constructs a new {@link Group} object with the specified name.
	 * 
	 * @param name
	 * 	The name of the group. The name must be a non-null, not-empty value.
	 */
	Group(String name) {
		this(name, new Recorder());
	}
	
	/**
	 * Constructs a new {@link Group} object with the specified name.
	 * 
	 * @param name
	 * 	The name of the group. The name must be a non-null, not-empty value.
	 * 
	 * @param recorder
	 * 	The {@link Recorder} to use when recording changes of this {@link Group}. 
	 */
	Group(String name, Recorder recorder) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkArgument(name.startsWith("@"));
		Preconditions.checkNotNull(recorder);
		
		this.name = name;
		this.recorder = recorder;
		this.groups = Sets.newTreeSet(Group.SORT_BY_NAME);
		this.users = Sets.newTreeSet(User.SORT_BY_NAME);
	}

	/**
	 * @return
	 * 	The name of this {@link Group}.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * This method adds a {@link User} object to the {@link Group}.
	 * 
	 * @param user
	 * 	The {@link User} to add to this {@link Group}. This may not be NULL.
	 * 	In case the {@link User} is already a member of this group an 
	 * 	{@link IllegalArgumentException} is thrown.
	 */
	public void add(User user) {
		Preconditions.checkArgument(!isAllGroup());
		Preconditions.checkNotNull(user);
		
		synchronized (users) {
			if (users.contains(user)) {
				throw new IllegalArgumentException("Cannot add user: '" + user.getName() + "'. It's already added!");
			}
			users.add(user);
		}

		final String childName = user.getName();
		recorder.append(new Modification("Adding user: '%s' to group: '%s'", childName, getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				Group parent = config.getGroup(getName());
				User child = config.getUser(childName);
				parent.add(child);
			}
		});
	}

	/**
	 * This method adds a child {@link Group} object to the {@link Group}.
	 * 
	 * @param group
	 * 	The {@link Group} to add to this {@link Group}. This may not be NULL.
	 * 	In case the {@link Group} is already a member of this group an 
	 * 	{@link IllegalArgumentException} is thrown.
	 */
	public void add(Group group) {
		Preconditions.checkArgument(!isAllGroup());
		Preconditions.checkNotNull(group);
		
		synchronized (groups) {
			if (groups.contains(group)) {
				throw new IllegalArgumentException("Cannot add group: '" + group.getName() + "'. It's already added!");
			}
			groups.add(group);
		}

		final String groupName = group.getName();
		recorder.append(new Modification("Adding group: '%s' to group: '%s'", groupName, getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				Group parent = config.getGroup(getName());
				Group child = config.getGroup(groupName);
				parent.add(child);
			}
		});
	}

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
	public boolean containsUser(User user) {
		Preconditions.checkNotNull(user);
		return getUsers().contains(user);
	}

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
	
	/**
	 * @return
	 * 	An {@link ImmutableSet} of child {@link Group}s of this {@link Group}.
	 */
	public ImmutableSet<Group> getGroups() {
		synchronized (groups) {
			return ImmutableSortedSet.copyOf(SORT_BY_NAME, groups);
		}
	}

	/**
	 * @return
	 * 	An {@link ImmutableSet} of child {@link User}s of this {@link Group}.
	 */
	public ImmutableSet<User> getUsers() {
		synchronized (users) {
			return ImmutableSortedSet.copyOf(User.SORT_BY_NAME, users);
		}
	}

	/**
	 * @return
	 * 	A {@link ImmutableSet} containing all {@link User}s and {@link Group}s.
	 */
	public ImmutableSet<Identifiable> getAllMembers() {
		Builder<Identifiable> builder = ImmutableSortedSet.orderedBy(Identifiable.SORT_BY_TYPE_AND_NAME);
		synchronized (groups) {
			builder.addAll(groups);
		}
		synchronized (users) {
			builder.addAll(users);
		}
		return builder.build();
	}
	
	private boolean isAllGroup() {
		return name.equals("@all");
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(name)
			.toHashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Group)) {
			return false;
		}
		
		return new EqualsBuilder()
			.append(name, ((Group) other).name)
			.isEquals();
	}
	
}
