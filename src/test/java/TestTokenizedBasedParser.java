import com.google.common.collect.Lists;
import nl.tudelft.ewi.gitolite.objects.Identifiable;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.Rule;
import nl.tudelft.ewi.gitolite.parser.TokenizerBasedParser;
import nl.tudelft.ewi.gitolite.parser.rules.ConfigKey;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.Option;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.permission.BasePermission;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class TestTokenizedBasedParser {

	private final static Identifier foo = new Identifier("foo");
	private final static Identifier bar = new Identifier("bar");
	private final static Identifier baz = new Identifier("baz");
	private final static Identifier ashok = new Identifier("ashok");
	private final static Identifier wally = new Identifier("wally");
	private final static Identifier dilbert = new Identifier("dilbert");
	private final static Identifier alice = new Identifier("alice");

	@Test
	public void basicParseTest() throws IOException {
		String in = "@test               =   foo\n" +
						"@other              =   @test bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		GroupRule test = GroupRule.builder()
			.pattern("@test")
			.member(foo)
			.build();

		GroupRule other = GroupRule.builder()
			.pattern("@other")
			.group(test)
			.member(bar)
			.build();

		GroupRule actualA = parser.parseGroupRule();
		GroupRule actualB = parser.parseGroupRule();
		actualA.write(System.out);
		actualB.write(System.out);

		assertEquals(test, actualA);
		assertEquals(other, actualB);
	}

	@Test
	public void parseRepositoryRuleTest() throws IOException {
		String in = "RW+ = foo bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, foo, bar);

		AccessRule actual = parser.parseAccessRule();
		actual.write(System.out);
		assertEquals(accessRule, actual);
	}

	@Test
	public void parseRepositoryRuleWithRefex() throws IOException {
		String in = "RW+ master = foo bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, "master", foo, bar);

		AccessRule actual = parser.parseAccessRule();
		actual.write(System.out);
		assertEquals(accessRule, actual);
	}

	@Test
	public void parseBlockWithSingleRule() throws IOException {
		String in = "repo yolo\n" +
						"    RW+ = foo bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, foo, bar);
		RepositoryRule repositoryRule = new RepositoryRule("yolo", accessRule);

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseBlockWithTwoRules() throws IOException {
		String in = "repo yolo\n" +
						"    RW+ = foo\n" +
						"    RW = bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, foo);
		AccessRule accessRule2 = new AccessRule(BasePermission.RW, bar);
		RepositoryRule repositoryRule = new RepositoryRule("yolo", accessRule, accessRule2);

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseBlockWithTwoRulesAndSpacing() throws IOException {
		String in = "\nrepo yolo\n" +
						"    RW+ = foo\n" +
						"    RW = bar\n\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, foo);
		AccessRule accessRule2 = new AccessRule(BasePermission.RW, bar);
		RepositoryRule repositoryRule = new RepositoryRule("yolo", accessRule, accessRule2);

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseBlockWithTwoRepositories() throws IOException {
		String in = "repo yolo swag\n" +
						"    RW+ = foo\n" +
						"    RW = bar\n\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, foo);
		AccessRule accessRule2 = new AccessRule(BasePermission.RW, bar);
		List<Identifiable> identifiables = Lists.newArrayList(new Identifier("yolo"), new Identifier("swag"));
		List<AccessRule> rules = Lists.newArrayList(accessRule, accessRule2);
		RepositoryRule repositoryRule = new RepositoryRule(identifiables, rules, Collections.emptyList());

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseBlockWithRepoGroup() throws IOException {
		String in = "@inhouse = yolo swag\n" +

						"repo @inhouse\n" +
						"    RW+ = foo bar";

		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		GroupRule inhouse = parser.parseGroupRule();
		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, foo, bar);
		RepositoryRule repositoryRule = new RepositoryRule(
			Collections.singletonList(inhouse),
			Collections.singletonList(accessRule),
			Collections.emptyList()
		);

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parsePlainOption() throws IOException {
		String in = "    config hooks.emailprefix = [%GL_REPO]\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		ConfigKey emailPrefix = new ConfigKey("hooks.emailprefix", "[%GL_REPO]");

		ConfigKey actual = parser.parseConfigRule();
		assertEquals(emailPrefix, actual);
	}

	@Test
	public void parseOptionWithDoubleQuotes() throws IOException {
		String in = "    config hooks.emailprefix = \"[%GL_REPO] \"\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		ConfigKey emailPrefix = new ConfigKey("hooks.emailprefix", "[%GL_REPO] ");

		ConfigKey actual = parser.parseConfigRule();
		assertEquals(emailPrefix, actual);
	}

	@Test
	public void parseOptionWithSingleQuotes() throws IOException {
		String in = "    config hooks.emailprefix = \'[%GL_REPO] \'\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		ConfigKey emailPrefix = new ConfigKey("hooks.emailprefix", "[%GL_REPO] ");

		ConfigKey actual = parser.parseConfigRule();
		assertEquals(emailPrefix, actual);
	}

	@Test
	public void parseBlockWithOptions() throws IOException {
		String in = "@inhouse = yolo swag\n\n" +

						"repo @inhouse\n" +
						"    RW+ = foo bar\n\n\n" +
						"    option deny-rules     = 1\n" +
						"    config hooks.emailprefix = \"[%GL_REPO] \"\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		GroupRule inhouse = parser.parseGroupRule();

		AccessRule accessRule = new AccessRule(BasePermission.RW_PLUS, foo, bar);
		Option denyRules = new Option("deny-rules", 1);
		ConfigKey emailPrefix = new ConfigKey("hooks.emailprefix", "[%GL_REPO] ");
		RepositoryRule repositoryRule = new RepositoryRule(
			Collections.singletonList(inhouse),
			Collections.singletonList(accessRule),
			Arrays.asList(denyRules, emailPrefix)
		);

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseSmallConfig() throws IOException {
		String in = "\n" +
			"@staff              =   dilbert alice           # groups\n" +
			"@projects           =   foo bar\n" +
			"\n" +
			"repo @projects baz                              # repos\n" +
			"    RW+             =   @staff                  # rules\n" +
			"    -       master  =   ashok\n" +
			"    RW              =   ashok\n" +
			"    R               =   wally\n" +
			"\n" +
			"    option deny-rules           =   1           # options\n" +
			"    config hooks.emailprefix    = '[%GL_REPO] ' # git-config";

		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		GroupRule staff = GroupRule.builder()
			.pattern("@staff").member(dilbert).member(alice).build();
		GroupRule projects = GroupRule.builder()
			.pattern("@projects").member(foo).member(bar).build();

		RepositoryRule repo = RepositoryRule.builder()
			.identifiable(projects)
			.identifiable(baz)
			.rule(new AccessRule(BasePermission.RW_PLUS, staff))
			.rule(new AccessRule(BasePermission.DENY, "master", ashok))
			.rule(new AccessRule(BasePermission.RW, ashok))
			.rule(new AccessRule(BasePermission.R, wally))
			.configKey(new Option("deny-rules", 1))
			.configKey(new ConfigKey("hooks.emailprefix", "[%GL_REPO] "))
			.build();

		List<Rule> expected = Lists.newArrayList(staff, projects, repo);
		List<Rule> actual = Lists.newArrayList();
		parser.parse(actual);

		staff.write(System.out);
		projects.write(System.out);
		repo.write(System.out);
		assertThat(actual, Matchers.equalTo(expected));

	}

	@Test
	public void devhubTest()  {
		String in = "@ti1705ta = liam carsten\n" +

			"repo courses/ti1705/*\n" +
				"RW+ = @ti1705ta\n" +
				"-   VREF/MAX_FILE_SIZE/50 = @all\n" +

			"repo courses/ti1705/group-1\n" +
				"RW = student1 student2\n";

		GroupRule groupRule = GroupRule.builder()
			.pattern("@ti1705ta")
			.member(new Identifier("liam"))
			.member(new Identifier("carsten"))
			.build();

		RepositoryRule repositoryRule = RepositoryRule.builder()
			.identifiable(new Identifier("courses/ti1705/*"))
			.rule(new AccessRule(BasePermission.RW_PLUS, groupRule))
			.rule(new AccessRule(BasePermission.DENY, "VREF/MAX_FILE_SIZE/50", new Identifier("@all")))
			.build();

		RepositoryRule second = RepositoryRule.builder()
			.identifiable(new Identifier("courses/ti1705/group-1"))
			.rule(new AccessRule(BasePermission.RW, new Identifier("student1"), new Identifier("student2")))
			.build();



	}

	@Test
	public void testParseAllRule() throws IOException {
		String in = "repo FOSS/..*\n" +
						"    R   =   @all";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		RepositoryRule repositoryRule = parser.parseRepositoryRule();
		AccessRule accessRule = repositoryRule.getRules().stream().findFirst().get();

		assertThatStream(accessRule.getMembers().getOwnGroupsStream(), contains(GroupRule.ALL));
	}

	@Test(expected = NoSuchElementException.class)
	public void testParseNonExistingAccessGroup() throws IOException {
		String in = "repo FOSS/..*\n" +
			"    R   =   @foo";
		new TokenizerBasedParser(new StringReader(in)).parseRepositoryRule();
	}

	@Test
	public void testParseAllRepositoriesRule() throws IOException {
		String in = "repo @all\n" +
			"    R   =   foo";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		RepositoryRule repositoryRule = parser.parseRepositoryRule();

		RepositoryRule expected = RepositoryRule.builder()
			.identifiable(GroupRule.ALL)
			.rule(new AccessRule(BasePermission.R, foo))
			.build();

		assertThat(repositoryRule, equalTo(expected));
	}

	@Test(expected = NoSuchElementException.class)
	public void testParseNonExistingRepositoryGroup() throws IOException {
		String in = "repo @foo\n" +
			"    R   =   bar";
		new TokenizerBasedParser(new StringReader(in)).parseRepositoryRule();
	}

	public static <T> void assertThatStream(Stream<T> stream, Matcher<? super List<T>> matcher) {
		assertThat(stream.collect(toList()), matcher);
	}

}
