package nl.tudelft.ewi.gitolite.keystore;

import com.google.common.collect.ComparisonChain;

import java.io.IOException;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface Key extends Comparable<Key> {

	/**
	 * The empty key name
	 */
	String EMPTY_KEY_NAME = "";

	/**
	 * @return the Identifiable for this Key
	 */
	String getUser();

	/**
	 * @return the name for this key.
	 */
	String getName();

	/**
	 * @return the contents for this key.
	 * @throws IOException if the contents could not be read
	 */
	String getContents() throws IOException;

	@Override
	default int compareTo(Key o) {
		return ComparisonChain.start()
			.compare(getUser(), o.getUser())
			.compare(getName(), o.getName())
			.result();
	};
}
