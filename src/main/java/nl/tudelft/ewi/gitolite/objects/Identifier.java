package nl.tudelft.ewi.gitolite.objects;

import lombok.Value;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * An identifier is used both for defining repositories (where the identifier may also be a
 * regular expression) or users.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Value
public class Identifier implements Identifiable {

	/**
	 * The name for the identifier
	 */
	private final String pattern;

	private final static WeakHashMap<String, WeakReference<Identifier>> identifiableMap = new WeakHashMap<>();

	/**
	 * Get an identifier. As {@code Identifiers} should be immutable, they are stored in a
	 * {@code WeakHashMap} for caching purposes. This method tries to find {@code Identifiers}
	 * in the cache first, and only creates a new instance if not. Therefore, this method is
	 * preferred over using a constructor.
	 *
	 * @param name Name for the identifier.
	 * @return Identifier
	 */
	public static Identifier valueOf(String name) {
		WeakReference<Identifier> ref = identifiableMap.get(name);
		Identifier identifiable;
		if(ref == null || (identifiable = ref.get()) == null) {
			identifiable = new Identifier(name);
			identifiableMap.put(name, new WeakReference<>(identifiable));
		}
		return identifiable;
	}

}
