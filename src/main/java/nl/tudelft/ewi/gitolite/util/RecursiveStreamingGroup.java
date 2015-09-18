package nl.tudelft.ewi.gitolite.util;

import java.util.stream.Stream;

/**
 * A {@code RecursiveStreamingGroup} is a {@code StreamingGroup} that may contain other
 * {@link RecursiveStreamingGroup#getOwnGroupsStream()} of the same type.
 * The {@link StreamingGroup#getMembersStream()} should return both the
 * {@link RecursiveStreamingGroup#getOwnMembersStream() own} members as the members
 * inherited from the groups.
 *
 * @param <R> Recursive definition of group types.
 * @param <T> Type that this group contains.
 */
public interface RecursiveStreamingGroup<R extends RecursiveStreamingGroup<? extends StreamingGroup, ? extends T>, T> extends StreamingGroup<T> {

	/**
	 * @return the own members for this group, excluding the recursive members inherited from the groups.
	 */
	Stream<T> getOwnMembersStream();

	/**
	 * @return the groups in this group.
	 */
	Stream<R> getOwnGroupsStream();

	/**
	 * Add a group to this group.
	 * @param group Group to add
	 */
	void add(R group);

	/**
	 * @return the members inherited from the groups.
	 */
	default Stream<T> getRecursiveInheritedStream() {
		return getOwnGroupsStream().flatMap(StreamingGroup::getMembersStream);
	}

	@Override
	default Stream<T> getMembersStream() {
		return Stream.concat(getRecursiveInheritedStream(), getOwnMembersStream());
	}

}
