package nl.tudelft.ewi.gitolite.objects;

import lombok.Value;

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

}
