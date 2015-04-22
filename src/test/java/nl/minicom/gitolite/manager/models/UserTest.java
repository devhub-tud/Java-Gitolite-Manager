package nl.minicom.gitolite.manager.models;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import nl.minicom.gitolite.manager.git.KeyGenerator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;

public class UserTest {

	private static final String NAME = "test-user";
	private static final String KEY_NAME = "test-key";
	private static String KEY_CONTENTS;
	
	@BeforeClass
	public static void setKeyContents() throws NoSuchAlgorithmException, IOException {
		KEY_CONTENTS  = KeyGenerator.generateRandomPublicKey();
	}
	
	@Test
	public void testConstructorWithValidInput() {
		new User(NAME);
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatConstructorThrowsNullPointerExceptionWhenNameIsNull() {
		new User(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatConstructorThrowsNullPointerExceptionWhenRecorderIsNull() {
		new User(NAME, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatConstructorWithEmptyNameThrowsIllegalArgumentException() {
		new User("");
	}
	
	@Test
	public void testAddingKey() {
		User user = new User(NAME);
		user.setKey(KEY_NAME, KEY_CONTENTS);
		
		Map<String, String> expected = Maps.newTreeMap();
		expected.put(KEY_NAME, KEY_CONTENTS);
		
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddingKeyWithNameNull() {
		User user = new User(NAME);
		user.setKey(null, KEY_CONTENTS);
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddingKeyWithContentNull() {
		User user = new User(NAME);
		user.setKey("", null);
	}
	
	@Test
	public void testOverridingKey() throws IOException, NoSuchAlgorithmException {
		String newKey = KeyGenerator.generateRandomPublicKey();

		User user = new User(NAME);
		user.setKey(KEY_NAME, KEY_CONTENTS);
		user.setKey(KEY_NAME, newKey);
		
		Map<String, String> expected = Maps.newTreeMap();
		expected.put(KEY_NAME, newKey);
		
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test
	public void testRemovingKey() {
		User user = new User(NAME);
		user.setKey(KEY_NAME, KEY_CONTENTS);
		user.removeKey(KEY_NAME);
		
		Map<String, String> expected = Maps.newTreeMap();
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test
	public void testEqualsMethod() {
		EqualsVerifier.forClass(User.class).suppress(Warning.STRICT_INHERITANCE).verify();
	}

}
