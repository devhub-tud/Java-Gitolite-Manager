package nl.tudelft.ewi.gitolite.util;

import java.util.stream.Stream;

/**
 * A group that inherits groups from a parent.
 * @param <T> Type that this group contains.
 */
public interface PrototypeStreamingGroup<T> extends StreamingGroup<T> {

	/**
	 * @return the prototypical parent for this group.
	 */
	StreamingGroup<T> getParent();

	/**
	 * @return the own members, excluding the members from the parent.
	 */
	Stream<T> getOwnMembersStream();

	/**
	 * @return true if this group has a parent
	 */
	default boolean hasParent() {
		return getParent() != null;
	}

	/**
	 * @return the members inherited from the prototype, if any.
	 */
	default Stream<T> getPrototypeInheritedStream() {
		return hasParent() ? getParent().getMembersStream() : Stream.empty();
	}

	@Override
	default Stream<T> getMembersStream() {
		return Stream.concat(getPrototypeInheritedStream(), getOwnMembersStream());
	}

}
