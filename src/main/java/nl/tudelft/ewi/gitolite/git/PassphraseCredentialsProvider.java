package nl.tudelft.ewi.gitolite.git;

import lombok.Data;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialItem.CharArrayType;
import org.eclipse.jgit.transport.CredentialItem.StringType;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * This class is a very basic {@link CredentialsProvider} which attempts to
 * login with a passphrase.
 *
 * @author Michael de Jong &lt;<a href="mailto:michaelj@minicom.nl">michaelj@minicom.nl</a>&gt;
 */
@Data
public class PassphraseCredentialsProvider extends CredentialsProvider {

	private final String passphrase;

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
