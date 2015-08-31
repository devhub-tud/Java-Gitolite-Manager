package nl.tudelft.ewi.gitolite.config.objects;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface Identifiable {

	String getPattern();

	default boolean matches(String input) {
		return input.matches(getPattern());
	}

	static Identifiable valueOf(String input) {
		return new IdentifiableImpl(input);
	}

}
