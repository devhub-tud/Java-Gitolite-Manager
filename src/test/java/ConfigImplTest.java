import nl.tudelft.ewi.gitolite.config.ConfigImpl;
import nl.tudelft.ewi.gitolite.config.CyclicDependencyException;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.parser.rules.Rule;
import nl.tudelft.ewi.gitolite.permission.BasePermission;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class ConfigImplTest {


	private final static Identifier foo = new Identifier("foo");
	private final static Identifier bar = new Identifier("bar");
	private final static Identifier baz = new Identifier("baz");

	private ConfigImpl config;

	@Before
	public void setUp() {
		config = new ConfigImpl();
	}

	@Test
	public void testGetInsertedGroup() {
		GroupRule test = GroupRule.builder()
			.pattern("@test").member(foo).member(bar).build();
		config.addGroup(test);
		assertEquals(test, config.getGroup("@test"));
	}

	@Test
	public void testTopologicalSort() {
		GroupRule test = GroupRule.builder()
			.pattern("@test").member(foo).member(bar).build();
		config.addGroup(test);

		GroupRule bliep = GroupRule.builder()
			.pattern("@bliep").member(baz).build();
		config.addGroup(bliep);

		test.add(bliep);

		Collection<Rule> rules = config.getRules();
		assertThat(rules, contains(bliep, test));
	}

	@Test(expected = CyclicDependencyException.class)
	public void testCyclicDependencyException() {
		GroupRule test = GroupRule.builder()
			.pattern("@test").member(foo).member(bar).build();
		config.addGroup(test);

		GroupRule bliep = GroupRule.builder()
			.pattern("@bliep").member(baz).build();
		config.addGroup(bliep);

		test.add(bliep);
		bliep.add(test);
		config.getRules();
	}

	@Test
	public void testDeleteGroup() {
		GroupRule test = GroupRule.builder()
			.pattern("@test").member(foo).member(bar).build();
		config.addGroup(test);

		GroupRule bliep = GroupRule.builder()
			.pattern("@bliep")
			.group(test)
			.member(baz).build();
		config.addGroup(bliep);

		assertThatStream(bliep.getOwnGroupsStream(), contains(test));

		config.deleteGroup(test);
		Collection<Rule> rules = config.getRules();
		assertThat(rules, contains(bliep));
		assertThatStream(bliep.getOwnGroupsStream(), empty());
	}

	@Test
	public void testAddRepositoryRule() {
		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(foo)
			.rule(new AccessRule(BasePermission.RW_PLUS, bar))
			.build();

		config.addRepositoryRule(repositoryRule);

		assertThat(config.getFirstRepositoryRule(foo), equalTo(repositoryRule));
		assertThat(config.getRepositoryRule(foo), contains(repositoryRule));
	}

	@Test
	public void testAddRepositoryRuleWithGroup() {
		GroupRule bliep = GroupRule.builder()
			.pattern("@bliep").member(baz).build();

		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(foo)
			.rule(new AccessRule(BasePermission.RW_PLUS, Collections.singleton(bliep), Collections.singleton(bar)))
			.build();

		config.addRepositoryRule(repositoryRule);
		assertThat(config.getRules(), contains(bliep, repositoryRule));
	}

	@Test
	public void testGroupRemoveCascadesToRepository() {
		GroupRule bliep = GroupRule.builder()
			.pattern("@bliep").member(baz).build();

		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(foo).identifiable(bliep)
			.rule(new AccessRule(BasePermission.RW_PLUS, bar))
			.build();

		assertThat(repositoryRule.getIdentifiables(), contains(foo, bliep));

		config.addRepositoryRule(repositoryRule);
		config.deleteGroup(bliep);

		assertThat(repositoryRule.getIdentifiables(), contains(foo));
		assertThat(config.getRules(), contains(repositoryRule));
	}

	@Test
	public void testCascadedRepositoryRemoval() {
		GroupRule bliep = GroupRule.builder()
			.pattern("@bliep").member(baz).build();

		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(bliep)
			.rule(new AccessRule(BasePermission.RW_PLUS, bar))
			.build();

		config.addRepositoryRule(repositoryRule);
		config.deleteGroup(bliep);

		assertThat(repositoryRule.getIdentifiables(), empty());
	}

	@Test
	public void testRepositoryWithAllGroup() {
		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(foo)
			.rule(new AccessRule(BasePermission.RW_PLUS, GroupRule.ALL))
			.build();

		config.addRepositoryRule(repositoryRule);
		assertThat(config.getRules(), contains(repositoryRule));

	}

	public static <T> void assertThatStream(Stream<T> stream, Matcher<? super List<T>> matcher) {
		assertThat(stream.collect(toList()), matcher);
	}

}
