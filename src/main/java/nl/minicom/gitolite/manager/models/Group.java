package nl.minicom.gitolite.manager.models;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;


public final class Group implements Identifiable {

	static final Comparator<Group> SORT_BY_DEPTH = new Comparator<Group>() {
		@Override
		public int compare(Group arg0, Group arg1) {
			if (arg0.containsGroups() && !arg1.containsGroups()) {
				return 1;
			}
			else if (!arg0.containsGroups() && arg1.containsGroups()) {
				return -1;
			}
			
			if (arg0.containsGroup(arg1)) {
				return 1;
			}
			else if (arg1.containsGroup(arg0)) {
				return -1;
			}
			
			return arg0.getName().compareTo(arg1.getName());
		}
	};
	
	private final String name;
	private final SortedSet<Identifiable> entities;
	
	/**
	 * Constructs a new {@link Group} object with the specified name.
	 * 
	 * @param name
	 * 		The name of the group. The name must be a non-null, not-empty value.
	 * 
	 * @throws NullPointerException
	 * 		If the name argument is null.
	 * 
	 * @throws IllegalArgumentException
	 * 		If the name is an empty {@link String}.
	 */
	Group(String name) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkArgument(name.startsWith("@"));
		
		this.name = name;
		this.entities = Sets.newTreeSet(Identifiable.SORT_BY_TYPE_AND_ALPHABETICALLY);
	}
	
	/**
	 * @return
	 * 		The name of this {@link Group}.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return
	 * 		True if this {@link Group} object consists of other {@link Group} objects.
	 */
	protected boolean containsGroups() {
		for (Identifiable entity : entities) {
			if (entity instanceof Group) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks if this {@link Group} object has a child (or 
	 * child of one of its children) which equals the provided {@link Group}.
	 * 
	 * @param needle
	 * 		The {@link Group} to look for in the children of this {@link Group}.
	 * 
	 * @return
	 * 		True if the specified {@link Group} is a child of this {@link Group}.
	 * 
	 * @throws NullPointerException
	 * 		If the argument 'needle' is NULL.
	 */
	protected boolean containsGroup(Group needle) {
		Preconditions.checkNotNull(needle);
		
		for (Identifiable entity : entities) {
			if (entity instanceof Group) {
				Group haystack = (Group) entity;
				if (haystack.equals(needle) || haystack.containsGroup(needle)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method adds an {@link Identifiable} object to the {@link Group}.
	 * This is usually a {@link User} or {@link Group} object.
	 * 
	 * @param entity
	 * 		The {@link Identifiable} to add to this {@link Group}.
	 * 
	 * @throws NullPointerException
	 * 		If the argument 'entity' is NULL.
	 */
	public void add(Identifiable entity) {
		Preconditions.checkNotNull(entity);
		Preconditions.checkArgument(!"@all".equalsIgnoreCase(name));
		
		if (entities.contains(entity)) {
			throw new IllegalArgumentException("Cannot add entity: " + entity.getName() + ". This entity is already added!");
		}
		entities.add(entity);
	}
	
	/**
	 * @return
	 * 		An {@link ImmutableSet} of {@link Identifiable} children of this {@link Group}.
	 */
	public ImmutableSet<Identifiable> getChildren() {
		return ImmutableSet.copyOf(entities);
	}

	public Collection<String> getEntityNamesInGroup() {
		return ImmutableSet.copyOf(Collections2.transform(getChildren(), new Function<Identifiable, String>() {
			@Override
			public String apply(Identifiable arg0) {
				return arg0.getName();
			}
		}));
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
