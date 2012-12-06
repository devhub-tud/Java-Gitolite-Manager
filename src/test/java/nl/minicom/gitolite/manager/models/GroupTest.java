package nl.minicom.gitolite.manager.models;

import java.util.Iterator;
import java.util.SortedSet;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;

public class GroupTest {

	@Test
	public void testConstructorWithValidInputs() {
		Group group = new Group("@test-group");
		Assert.assertEquals("@test-group", group.getName());
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorWithNullAsName() {
		new Group(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithEmptyName() {
		new Group("");
	}

	@Test(expected = NullPointerException.class)
	public void testAddingNullToGroup() {
		new Group("@parent").add(null);
	}

	@Test
	public void testAddingUserToGroup() {
		Group parent = new Group("@parent");
		User user = new User("test-user");
		parent.add(user);

		Assert.assertEquals(Sets.newHashSet(user), parent.getChildren());
	}

	@Test
	public void testAddingGroupToGroup() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);

		Assert.assertEquals(Sets.newHashSet(child), parent.getChildren());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingSameGroupTwiceToGroup() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);
		parent.add(child);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingUserToAllGroupThrowsException() {
		new Group("@all").add(new User("test-user"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingGroupToAllGroupThrowsException() {
		new Group("@all").add(new Group("@test-group"));
	}

	@Test(expected = NullPointerException.class)
	public void testThatContainsGroupMethodThrowsExceptionOnNullAsInput() {
		new Group("@parent").containsGroup(null);
	}

	@Test
	public void testContainsGroupMethodWithFirstDegreeChild() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupMethodWithSecondDegreeChild() {
		Group parent = new Group("@parent");
		Group intermediate = new Group("@intermediate");
		Group child = new Group("@child");

		parent.add(intermediate);
		intermediate.add(child);

		Assert.assertTrue(parent.containsGroup(child));
	}

	@Test
	public void testContainsGroupMethodWhenOtherGroupIsNoChild() {
		Group parent = new Group("@parent");
		Group other = new Group("@other");

		Assert.assertFalse(parent.containsGroup(other));
	}

	@Test
	public void testContainsGroupMethodWhenOnlyUsersArePresent() {
		Group parent = new Group("@parent");
		parent.add(new User("test-user"));

		Assert.assertFalse(parent.containsGroup(new Group("@other")));
	}

	@Test
	public void testContainsGroupsMethodWhenGroupHasChildren() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		parent.add(child);

		Assert.assertTrue(parent.containsGroups());
	}

	@Test
	public void testContainsGroupsMethodWhenGroupHasNoChildren() {
		Group parent = new Group("@parent");
		Assert.assertFalse(parent.containsGroups());
	}

	@Test
	public void testContainsGroupsMethodWhenGroupOnlyContainsUsers() {
		Group parent = new Group("@parent");
		parent.add(new User("test-user"));
		Assert.assertFalse(parent.containsGroups());
	}

	@Test
	public void testGetEntityNamesInGroupMethod() {
		Group parent = new Group("@parent");
		Group child = new Group("@child");
		User user = new User("test-user");
		parent.add(user);
		parent.add(child);

		Assert.assertEquals(Sets.newHashSet("@child", "test-user"), parent.getEntityNamesInGroup());
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
		Group a = new Group("@a");
		Group b = new Group("@b");
		Group c = new Group("@c");
		Group d = new Group("@d");

		a.add(b);
		a.add(c);
		c.add(d);

		SortedSet<Group> groups = Sets.newTreeSet(Group.SORT_BY_DEPTH);
		groups.add(a);
		groups.add(b);
		groups.add(c);
		groups.add(d);

		Iterator<Group> iter = groups.iterator();
		Assert.assertEquals(b, iter.next());
		Assert.assertEquals(d, iter.next());
		Assert.assertEquals(c, iter.next());
		Assert.assertEquals(a, iter.next());
	}

	@Test
	public void testEqualsAndHashCode() {
		EqualsVerifier.forClass(Group.class).verify();
	}

}
