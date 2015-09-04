package nl.tudelft.ewi.gitolite.util;

import java.util.stream.Stream;

/**
 * A Group definition that is both recursive and prototypical.
 *
 * @param <R> Recursive definition of group types.
 * @param <T> Type that this group contains.
 */
public interface RecursiveAndPrototypeStreamingGroup<R extends RecursiveStreamingGroup<R, T>, T>
extends RecursiveStreamingGroup<R, T>, PrototypeStreamingGroup<T> {

	@Override
	default Stream<T> getMembersStream() {
		return Stream.concat(Stream.concat(getPrototypeInheritedStream(), getRecursiveInheritedStream()), getOwnMembersStream());
	}

}
