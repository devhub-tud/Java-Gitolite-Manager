package nl.tudelft.ewi.gitolite.keystore;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Value
@AllArgsConstructor
public class KeyHolder implements Key {

	private final String user;

	private final String name;

	private final String contents;

	public KeyHolder(String user, String contents) {
		this(user, EMPTY_KEY_NAME, contents);
	}

}
