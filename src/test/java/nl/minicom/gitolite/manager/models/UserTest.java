package nl.minicom.gitolite.manager.models;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import nl.minicom.gitolite.manager.models.User;

import org.junit.Test;

public class UserTest {

	private static final String NET_ID = "mdejong2";
	
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
	public void testEqualsMethod() {
		EqualsVerifier.forClass(User.class).suppress(Warning.STRICT_INHERITANCE).verify();
	}

}
