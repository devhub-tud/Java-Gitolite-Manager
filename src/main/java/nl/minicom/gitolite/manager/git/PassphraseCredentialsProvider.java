package nl.minicom.gitolite.manager.git;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialItem.Password;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import com.google.common.base.Preconditions;

public class PassphraseCredentialsProvider extends CredentialsProvider {

	private final String passphrase;

	public PassphraseCredentialsProvider(String passphrase) {
		Preconditions.checkNotNull(passphrase);
		this.passphrase = passphrase;
	}
	
	@Override
	public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
		for (CredentialItem item : items) {
			if (item instanceof Password) {
				((Password) item).setValue(passphrase.toCharArray());
			}
		}
		return true;
	}

	@Override
	public boolean isInteractive() {
		return false;
	}

	@Override
	public boolean supports(CredentialItem... items) {
		for (CredentialItem item : items) {
			if (!(item instanceof Password)) {
				return false;
			}
		}
		return true;
	}
	
}
