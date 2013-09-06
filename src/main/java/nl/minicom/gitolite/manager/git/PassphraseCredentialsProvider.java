package nl.minicom.gitolite.manager.git;

import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialItem.CharArrayType;
import org.eclipse.jgit.transport.CredentialItem.StringType;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * This class is a very basic {@link CredentialsProvider} which attempts to
 * login with a passphrase.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public class PassphraseCredentialsProvider extends CredentialsProvider {

	private static String ensureNotNull(String passphrase) {
		if (passphrase == null) { 
			return "";
		}
		return passphrase;
	}

	private final String passphrase;

	/**
	 * This constructs a new {@link PassphraseCredentialsProvider} object.
	 * 
	 * @param passphrase The passphrase to use, to log in.
	 */
	public PassphraseCredentialsProvider(String passphrase) {
		this.passphrase = ensureNotNull(passphrase);
	}

	/**
	 * This method attempts to augment the provided {@link CredentialItem}
	 * objects, so that we can log in.
	 * 
	 * @param uri The URI of the git repository.
	 * 
	 * @param items The {@link CredentialItem} objects to augment.
	 * 
	 * @return True
	 */
	@Override
	public boolean get(URIish uri, CredentialItem... items) {
		for (CredentialItem item : items) {
			if (item instanceof StringType) {
				((StringType) item).setValue(passphrase);
			}
			else if (item instanceof CharArrayType) {
				((CharArrayType) item).setValue(passphrase.toCharArray());
			}
		}
		return true;
	}

	/**
	 * @return False
	 */
	@Override
	public boolean isInteractive() {
		return false;
	}

	/**
	 * @param items The {@link CredentialItem} objects to process.
	 * 
	 * @return True
	 */
	@Override
	public boolean supports(CredentialItem... items) {
		return true;
	}

}
