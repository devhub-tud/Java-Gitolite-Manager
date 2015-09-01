package nl.tudelft.ewi.gitolite.config.objects;

import java.io.Serializable;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface Identifiable extends Comparable<Identifiable>, Serializable {

	String getPattern();

	@Override
	default int compareTo(Identifiable o) {
		return getPattern().compareTo(o.getPattern());
	}

}
