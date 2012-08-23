package nl.minicom.gitolite.manager.models;

import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

public class UserTest {

	private static final String NET_ID = "mdejong2";
	private static final String KEY_CONTENT = "Content of SSH key...";
	
	@Test
	public void testConstructorWithValidInput() {
		new User(NET_ID);
	}
	
	@Test(expected = NullPointerException.class)
	public void testThatConstructorThrowsNullPointerExceptionWhenNameIsNull() {
		new User(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testThatConstructorWithEmptyNameThrowsIllegalArgumentException() {
		new User("");
	}
	
	@Test
	public void testAddingKey() {
		User user = new User("test-user");
		user.defineKey("test", KEY_CONTENT);
		
		Map<String, String> expected = Maps.newTreeMap();
		expected.put("test", KEY_CONTENT);
		
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test
	public void testOverridingKey() {
		User user = new User("test-user");
		user.defineKey("test", KEY_CONTENT);
		user.defineKey("test", KEY_CONTENT + "...");
		
		Map<String, String> expected = Maps.newTreeMap();
		expected.put("test", KEY_CONTENT + "...");
		
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test
	public void testRemovingKey() {
		User user = new User("test-user");
		user.defineKey("test", KEY_CONTENT);
		user.removeKey("test");
		
		Map<String, String> expected = Maps.newTreeMap();
		Assert.assertEquals(expected, user.getKeys());
	}
	
	@Test
	public void testEqualsMethod() {
		EqualsVerifier.forClass(User.class).suppress(Warning.STRICT_INHERITANCE).verify();
	}

}
