package nl.minicom.gitolite.manager.exceptions;


/**
 * This {@link Exception} can be used to notify that a certain operation failed
 * because no connection to the gitolite server could be made. In other words,
 * it appears to be down, and thus failed to complete the operation. 
 */
public class ServiceUnavailable extends Exception {

	private static final long serialVersionUID = 5869323101282983937L;

	/**
	 * Constructs a new {@link ServiceUnavailable} object.
	 * 
	 * @param cause
	 * 	The cause of the {@link ServiceUnavailable}.
	 */
	public ServiceUnavailable(Throwable cause) {
		super(cause);
	}

}
