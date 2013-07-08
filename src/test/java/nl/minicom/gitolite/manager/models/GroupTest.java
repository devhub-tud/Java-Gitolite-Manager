package nl.minicom.gitolite.manager.models;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

public class GroupTest {

	@Test
	public void testConstructorWithValidInputs() {
		Group group = new InternalGroup("@test-group");
		Assert.assertEquals("@test-group", group.getName());
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullAsName() {
		new InternalGroup(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyName() {
		new InternalGroup("");
	}

	@Test(expected = NullPointerException.class)
	public void testAddingNullGroupToGroup() {
		Group nullGroup = null;
		new InternalGroup("@parent").add(nullGroup);
	}
	
	@Test(expected = NullPointerException.class)
	public void testAddingNullUserToGroup() {
		User nullUser = null;
		new InternalGroup("@parent").add(nullUser);
	}

	@Test
	public void testAddingUserToGroup() {
		Group parent = new InternalGroup("@parent");
		User user = new User("test-user");
		parent.add(user);

		Assert.assertEquals(Sets.newHashSet(user), parent.getUsers());
	}

	@Test
	public void testAddingGroupToGroup() {
		Group parent = new InternalGroup("@parent");
		Group child = new InternalGroup("@child");
		parent.add(child);

		Assert.assertEquals(Sets.newHashSet(child), parent.getGroups());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingSameGroupTwiceToGroup() {
		Group parent = new InternalGroup("@parent");
		Group child = new InternalGroup("@child");
		parent.add(child);
		parent.add(child);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingUserToAllGroupThrowsException() {
		new InternalGroup("@all").add(new User("test-user"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingGroupToAllGroupThrowsException() {
		new InternalGroup("@all").add(new InternalGroup("@test-group"));
	}

	@Test(expected = NullPointerException.class)
	public void testThatContainsGroupMethodThrowsExceptionOnNullAsInput() {
		new InternalGroup("@parent").containsGroup(null);
	}

	@Test
	public void testContainsGroupMethodWithFirstDegreeChild() {
		Group parent = new InternalGroup("@parent");
		Group child = new InternalGroup("@child");
		parent.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupMethodWithSecondDegreeChild() {
		Group parent = new InternalGroup("@parent");
		Group intermediate = new InternalGroup("@intermediate");
		Group child = new InternalGroup("@child");

		parent.add(intermediate);
		intermediate.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupMethodWhenOtherGroupIsNoChild() {
		Group parent = new InternalGroup("@parent");
		Group other = new InternalGroup("@other");

		Assert.assertFalse(parent.containsGroup(other));
	}

	@Test
	public void testContainsGroupMethodWhenOnlyUsersArePresent() {
		Group parent = new InternalGroup("@parent");
		parent.add(new User("test-user"));

		Assert.assertFalse(parent.containsGroup(new InternalGroup("@other")));
	}

	@Test
	public void testContainsGroupsMethodWhenGroupHasChildren() {
		Group parent = new InternalGroup("@parent");
		Group child = new InternalGroup("@child");
		parent.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupsMethodWhenGroupOnlyContainsUsers() {
		Group parent = new InternalGroup("@parent");
		User user = new User("test-user");
		parent.add(user);
		
		Assert.assertEquals(1, parent.getUsers().size());
		Assert.assertTrue(parent.containsUser(user));
	}

	@Test
	public void testGetMembersMethod() {
		Group parent = new InternalGroup("@parent");
		Group child = new InternalGroup("@child");
		User user = new User("test-user");
		parent.add(user);
		parent.add(child);
		
		Set<Identifiable> members = Sets.newTreeSet(Identifiable.SORT_BY_TYPE_AND_NAME);
		members.add(child);
		members.add(user);

		Assert.assertEquals(members, parent.getMembers());
	}

	/**
	 * Test case tree:
	 * 
	 * <pre>
	 *          B
	 *        /
	 *      -->     A
	 *        \
	 *          C - D
	 * </pre>
	 * 
	 * Expected ordering (bottom-up, alphabetical):<br>
	 * D, B, C, A
	 */
	@Test
	public void testGroupOrdering() {
		Group a = new InternalGroup("@a");
		Group b = new InternalGroup("@b");
		Group c = new InternalGroup("@c");
		Group d = new InternalGroup("@d");

		a.add(b);
		a.add(c);
		c.add(d);

		SortedSet<Group> groups = Sets.newTreeSet(InternalGroup.SORT_BY_NAME);
		groups.add(a);
		groups.add(b);
		groups.add(c);
		groups.add(d);

		Iterator<Group> iter = groups.iterator();
		Assert.assertEquals(a, iter.next());
		Assert.assertEquals(b, iter.next());
		Assert.assertEquals(c, iter.next());
		Assert.assertEquals(d, iter.next());
	}

	@Test
	public void testEqualsAndHashCode() {
		EqualsVerifier.forClass(InternalGroup.class).verify();
	}

}
