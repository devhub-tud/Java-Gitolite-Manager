package nl.tudelft.ewi.gitolite.config.util;

import com.sun.istack.internal.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A collection type for group data structure with recursive characteristics.
 *
 * @param <T> Type that this group contains.
 * @author Jan-Willem Gmelig Meyling
 */
public interface StreamingGroup<T> extends Iterable<T> {

	/**
	 * @return A {@code Stream} of items
	 */
	Stream<T> getMembersStream();

	/**
	 * Check if this group definition contains an element.
	 * @param element element to check for.
	 * @return true if this group contains the element.
	 */
	default boolean contains(T element) {
		return getMembersStream().anyMatch(element::equals);
	}

	/**
	 * Add an element to the group.
	 * @param element
	 */
	void add(T element);

	/**
	 * Remove an element from the group.
	 * @param element
	 */
	boolean remove(T element);

	/**
	 * Check if the group is empty.
	 * @return true if empty.
	 */
	default boolean isEmpty() {
		return !getMembersStream().findAny().isPresent();
	}

	@Override
	default Iterator<T> iterator() {
		return getMembersStream().iterator();
	}

}
