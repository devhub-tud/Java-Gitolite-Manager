package nl.tudelft.ewi.gitolite.config.util;

import java.util.stream.Stream;

/**
 * A Group is defined by its members, which is a collection of values
 * and recursive groups of the same type.
 *
 * @param <T> Type of the recursive group definition.
 * @param <V> Type of the members stored in the group definition.
 * @author Jan-Willem Gmelig Meyling
 */
public interface RecursiveGroupDefinition<T extends RecursiveGroupDefinition<? extends T, ? extends V>, V> extends GroupDefinition<V>{

	/**
	 * @return the groups for this {@code GroupDefinition}.
	 */
	Stream<T> getGroups();

	/**
	 * @return the inherited members for this {@code GroupDefinition}.
	 */
	default Stream<V> getInheritedMembers() {
		Stream<V> inheritedMembers = getGroups()
			.flatMap(RecursiveGroupDefinition::getInheritedMembers);
		return Stream.concat(inheritedMembers, getMembers());
	}

	/**
	 * Check if a certain value exists in this {@code GroupDefinition}.
	 * @param value value to look for.
	 * @return true if the value exists.
	 * @see RecursiveGroupDefinition#getInheritedMembers()
	 */
	default boolean contains(V value) {
		return getInheritedMembers().anyMatch(value::equals);
	}

	/**
	 * Add a group to the group.
	 * @param group group to add.
	 */
	void add(T group);

}
