package nl.tudelft.ewi.gitolite.git;

import lombok.NoArgsConstructor;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@NoArgsConstructor
public class ServiceUnavailable extends GitException {

	public ServiceUnavailable(Throwable cause) {
		super(cause);
	}

}
