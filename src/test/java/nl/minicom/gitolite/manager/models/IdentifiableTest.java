package nl.minicom.gitolite.manager.models;

import java.util.Iterator;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class IdentifiableTest {

	private Group group1;
	private Group group2;
	private User user1;
	private User user2;

	@Before
	public void setUp() {
		group1 = new Group("@group1");
		group2 = new Group("@group2");
		user1 = new User("user1");
		user2 = new User("user2");
	}

	@Test
	public void testComparatorWithGroupsOnly() {
		SortedSet<Identifiable> identifiables = Sets.newTreeSet(Identifiable.SORT_BY_TYPE_AND_ALPHABETICALLY);
		identifiables.add(group2);
		identifiables.add(group1);

		assertSequence(identifiables, group1, group2);
	}

	@Test
	public void testComparatorWithUsersOnly() {
		SortedSet<Identifiable> identifiables = Sets.newTreeSet(Identifiable.SORT_BY_TYPE_AND_ALPHABETICALLY);
		identifiables.add(user2);
		identifiables.add(user1);

		assertSequence(identifiables, user1, user2);
	}

	@Test
	public void testComparatorWithUsersAndGroups() {
		SortedSet<Identifiable> identifiables = Sets.newTreeSet(Identifiable.SORT_BY_TYPE_AND_ALPHABETICALLY);
		identifiables.add(group2);
		identifiables.add(user2);
		identifiables.add(group1);
		identifiables.add(user1);

		assertSequence(identifiables, group1, group2, user1, user2);
	}

	private void assertSequence(SortedSet<Identifiable> elements, Identifiable... order) {
		Assert.assertEquals(order.length, elements.size());

		Iterator<Identifiable> iter = elements.iterator();
		for (Identifiable item : order) {
			Assert.assertEquals(item, iter.next());
		}
	}

}
