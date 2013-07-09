package nl.minicom.gitolite.manager.models;

import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

public class UserTest {

	private static final String NAME = "test-user";
	private static final String KEY_NAME = "test-key";
	private static final String KEY_CONTENT = "Content of SSH key...";
	
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
		user.setKey(KEY_NAME, KEY_CONTENT);
		
		Map<String, String> expected = Maps.newTreeMap();
		expected.put(KEY_NAME, KEY_CONTENT);
		
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddingKeyWithNameNull() {
		User user = new User(NAME);
		user.setKey(null, KEY_CONTENT);
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddingKeyWithContentNull() {
		User user = new User(NAME);
		user.setKey("", null);
	}
	
	@Test
	public void testOverridingKey() {
		User user = new User(NAME);
		user.setKey(KEY_NAME, KEY_CONTENT);
		user.setKey(KEY_NAME, KEY_CONTENT + "...");
		
		Map<String, String> expected = Maps.newTreeMap();
		expected.put(KEY_NAME, KEY_CONTENT + "...");
		
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test
	public void testRemovingKey() {
		User user = new User(NAME);
		user.setKey(KEY_NAME, KEY_CONTENT);
		user.removeKey(KEY_NAME);
		
		Map<String, String> expected = Maps.newTreeMap();
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test
	public void testEqualsMethod() {
		EqualsVerifier.forClass(User.class).suppress(Warning.STRICT_INHERITANCE).verify();
	}

}
