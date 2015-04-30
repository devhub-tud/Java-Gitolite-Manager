package nl.minicom.gitolite.manager.git;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class KeyGenerator {

	public static void main(String... args) throws NoSuchAlgorithmException, IOException {
        KeyPair keyPair = generateKeyPair();
		System.out.println(encodePublicKey(keyPair.getPublic()));
	}
	
	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
	}

	/**
	 * Encode PublicKey (DSA or RSA encoded) to authorized_keys like string
	 *
	 * @param publicKey
	 *            DSA or RSA encoded
	 * @return authorized_keys like string
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public static String encodePublicKey(final PublicKey publicKey)
			throws IOException {
		if (publicKey.getAlgorithm().equals("RSA")) {
			RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
			ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(byteOs);
			dos.writeInt("ssh-rsa".getBytes().length);
			dos.write("ssh-rsa".getBytes());
			dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
			dos.write(rsaPublicKey.getPublicExponent().toByteArray());
			dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
			dos.write(rsaPublicKey.getModulus().toByteArray());
			return "ssh-rsa " + new String(Base64.getEncoder().encode(byteOs
					.toByteArray()));
		} else {
			throw new IllegalArgumentException("Unknown public key encoding: "
					+ publicKey.getAlgorithm());
		}
	}
	
	public static String generateRandomPublicKey() throws NoSuchAlgorithmException, IOException {
		return encodePublicKey(generateKeyPair().getPublic());
	}
}
