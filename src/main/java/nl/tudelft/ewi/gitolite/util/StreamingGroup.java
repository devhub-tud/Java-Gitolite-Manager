package nl.tudelft.ewi.gitolite.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection type for group data structure with recursive characteristics.
 *
 * @param <T> Type that this group contains.
 * @author Jan-Willem Gmelig Meyling
 */
public interface StreamingGroup<T> extends Collection<T> {

	/**
	 * @return A {@code Stream} of items
	 */
	Stream<T> getMembersStream();

	@Override
	default boolean contains(Object element) {
		return getMembersStream().anyMatch(element::equals);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return c.stream().allMatch(this::contains);
	}

	@Override
	default boolean isEmpty() {
		return !getMembersStream().findAny().isPresent();
	}

	@Override
	default Iterator<T> iterator() {
		return getMembersStream().iterator();
	}

	@Override
	default int size() {
		return (int) getMembersStream().count();
	}

	@Override
	default <T1> T1[] toArray(T1[] a) {
		return getMembersStream().collect(Collectors.toList()).toArray(a);
	}

	@Override
	default Object[] toArray() {
		return getMembersStream().collect(Collectors.toList()).toArray();
	}

	@Override
	default Stream<T> stream() {
		return getMembersStream();
	}

	@Override
	default Stream<T> parallelStream() {
		return getMembersStream().parallel();
	}

	@Override
	default void forEach(Consumer<? super T> action) {
		getMembersStream().forEach(action);
	}

}
