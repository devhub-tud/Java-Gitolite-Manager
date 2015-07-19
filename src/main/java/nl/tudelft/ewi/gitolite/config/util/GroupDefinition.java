package nl.tudelft.ewi.gitolite.config.util;

import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface GroupDefinition<V> {

	/***
	 * @return the members for this {@code GroupDefinition}.
	 */
	Stream<V> getMembers();

	/**
	 * Check if a group contains a member
	 * @param value value to check for
	 * @return true if the group contains the member
	 */
	default boolean contains(V value) {
		return getMembers().anyMatch(value::equals);
	}

	/**
	 * Remove value implementation.
	 * @param value value to remove
	 * @return true if the value was removed
	 */
	boolean remove(V value);

	/**
	 * Add a value to the group.
	 * @param value value to add.
	 */
	void add(V value);

}
