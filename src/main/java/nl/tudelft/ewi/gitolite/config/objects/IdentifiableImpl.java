package nl.tudelft.ewi.gitolite.config.objects;

import lombok.Data;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
public class IdentifiableImpl implements Identifiable {

	private final String pattern;

}
