package nl.tudelft.ewi.gitolite.config.objects;

import lombok.Value;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Value
public class IdentifiableImpl implements Identifiable {

	private final String pattern;

}
