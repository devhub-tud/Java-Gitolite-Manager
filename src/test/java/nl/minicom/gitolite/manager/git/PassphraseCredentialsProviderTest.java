package nl.minicom.gitolite.manager.git;

import org.eclipse.jgit.transport.CredentialItem.Password;
import org.eclipse.jgit.transport.CredentialItem.StringType;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.junit.Assert;
import org.junit.Test;

public class PassphraseCredentialsProviderTest {

	@Test
	public void testConstructorWitValidInputs() {
		new PassphraseCredentialsProvider("");
	}

	@Test
	public void testConstructorWithNullAsPassphrase() {
		new PassphraseCredentialsProvider(null);
	}

	@Test
	public void testThatGetMethodSetsPasswordWhenAsked() {
		CredentialsProvider provider = new PassphraseCredentialsProvider("passphrase");

		Password password = new Password();

		Assert.assertTrue(provider.get(null, new StringType("", true), password));
		Assert.assertEquals("passphrase", String.valueOf(password.getValue()));
	}

	@Test
	public void testIsInteractiveMethodReturnsFalse() {
		Assert.assertFalse(new PassphraseCredentialsProvider("").isInteractive());
	}

	@Test
	public void testSupportsMethodWhenAskingForPassphrase() {
		CredentialsProvider provider = new PassphraseCredentialsProvider("passphrase");
		Assert.assertTrue(provider.supports(new Password()));
	}

}
