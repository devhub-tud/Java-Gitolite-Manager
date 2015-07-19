package nl.tudelft.ewi.gitolite.config.util;

import com.sun.istack.internal.Nullable;

import java.util.stream.Stream;

/**
 * The {@code PrototypeGroupDefinition} is a {@link RecursiveGroupDefinition} that inherits members
 * from another {@code GroupDefinition}, but adds a few new members or recursive groups to
 * the definition.
 *
 * @param <T> Type of the recursive group definition.
 * @param <V> Type of the members stored in the group definition.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface PrototypeGroupDefinition<T extends RecursiveGroupDefinition<? extends T, ? extends V>, V> extends RecursiveGroupDefinition<T, V> {

	/**
	 * @return the parent for this {@code PrototypeGroupDefinition}.
	 */
	@Nullable
	T getParent();

	/**
	 * @return the own groups for this definition.
	 */
	Stream<T> getOwnGroups();

	/***
	 * @return the own members for this definition.
	 */
	Stream<V> getOwnMembers();

	@Override
	default Stream<T> getGroups() {
		T parent = getParent();
		Stream< T> ownGroups = getOwnGroups();
		if(parent != null) {
			return Stream.concat(parent.getGroups(), ownGroups);
		}
		return ownGroups;
	}

	@Override
	default Stream<V> getMembers() {
		T parent = getParent();
		Stream<V> ownMembers = getOwnMembers();
		if(parent != null) {
			return Stream.concat(parent.getMembers(), ownMembers);
		}
		return ownMembers;
	}

}
