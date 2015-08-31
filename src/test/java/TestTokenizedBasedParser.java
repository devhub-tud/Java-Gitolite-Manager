import com.google.common.collect.Lists;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.objects.IdentifiableImpl;
import nl.tudelft.ewi.gitolite.config.parser.rules.Rule;
import nl.tudelft.ewi.gitolite.config.parser.TokenizerBasedParser;
import nl.tudelft.ewi.gitolite.config.parser.rules.ConfigKey;
import nl.tudelft.ewi.gitolite.config.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.Option;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRuleBlock;
import nl.tudelft.ewi.gitolite.config.permission.BasePermission;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class TestTokenizedBasedParser {

	private final static Identifiable foo = new IdentifiableImpl("foo");
	private final static Identifiable bar = new IdentifiableImpl("bar");
	private final static Identifiable baz = new IdentifiableImpl("baz");
	private final static Identifiable ashok = new IdentifiableImpl("ashok");
	private final static Identifiable wally = new IdentifiableImpl("wally");
	private final static Identifiable dilbert = new IdentifiableImpl("dilbert");
	private final static Identifiable alice = new IdentifiableImpl("alice");

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

		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, foo, bar);

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseRepositoryRuleWithRefex() throws IOException {
		String in = "RW+ master = foo bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, "master", foo, bar);

		RepositoryRule actual = parser.parseRepositoryRule();
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseBlockWithSingleRule() throws IOException {
		String in = "repo yolo\n" +
						"    RW+ = foo bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, foo, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock("yolo", repositoryRule);

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock();
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithTwoRules() throws IOException {
		String in = "repo yolo\n" +
						"    RW+ = foo\n" +
						"    RW = bar";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, foo);
		RepositoryRule repositoryRule2 = new RepositoryRule(BasePermission.RW, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock("yolo", repositoryRule, repositoryRule2);

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock();
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithTwoRulesAndSpacing() throws IOException {
		String in = "\nrepo yolo\n" +
						"    RW+ = foo\n" +
						"    RW = bar\n\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, foo);
		RepositoryRule repositoryRule2 = new RepositoryRule(BasePermission.RW, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock("yolo", repositoryRule, repositoryRule2);

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock();
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithTwoRepositories() throws IOException {
		String in = "repo yolo swag\n" +
						"    RW+ = foo\n" +
						"    RW = bar\n\n";
		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, foo);
		RepositoryRule repositoryRule2 = new RepositoryRule(BasePermission.RW, bar);
		List<Identifiable> identifiables = Lists.newArrayList(new IdentifiableImpl("yolo"), new IdentifiableImpl("swag"));
		List<RepositoryRule> rules = Lists.newArrayList(repositoryRule, repositoryRule2);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock(identifiables, rules, Collections.emptyList());

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock();
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithRepoGroup() throws IOException {
		String in = "@inhouse = yolo swag\n" +

						"repo @inhouse\n" +
						"    RW+ = foo bar";

		TokenizerBasedParser parser = new TokenizerBasedParser(new StringReader(in));

		GroupRule inhouse = parser.parseGroupRule();
		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, foo, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock(
			Collections.singletonList(inhouse),
			Collections.singletonList(repositoryRule),
			Collections.emptyList()
		);

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock();
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
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

		RepositoryRule repositoryRule = new RepositoryRule(BasePermission.RW_PLUS, foo, bar);
		Option denyRules = new Option("deny-rules", 1);
		ConfigKey emailPrefix = new ConfigKey("hooks.emailprefix", "[%GL_REPO] ");
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock(
			Collections.singletonList(inhouse),
			Collections.singletonList(repositoryRule),
			Arrays.asList(denyRules, emailPrefix)
		);

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock();
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
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

		RepositoryRuleBlock repo = RepositoryRuleBlock.builder()
			.identifiable(projects)
			.identifiable(baz)
			.rule(new RepositoryRule(BasePermission.RW_PLUS, staff))
			.rule(new RepositoryRule(BasePermission.DENY, "master", ashok))
			.rule(new RepositoryRule(BasePermission.RW, ashok))
			.rule(new RepositoryRule(BasePermission.R, wally))
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
			.member(new IdentifiableImpl("liam"))
			.member(new IdentifiableImpl("carsten"))
			.build();

		RepositoryRuleBlock repositoryRuleBlock = RepositoryRuleBlock.builder()
			.identifiable(new IdentifiableImpl("courses/ti1705/*"))
			.rule(new RepositoryRule(BasePermission.RW_PLUS, groupRule))
			.rule(new RepositoryRule(BasePermission.DENY, "VREF/MAX_FILE_SIZE/50", new IdentifiableImpl("@all")))
			.build();

		RepositoryRuleBlock second = RepositoryRuleBlock.builder()
			.identifiable(new IdentifiableImpl("courses/ti1705/group-1"))
			.rule(new RepositoryRule(BasePermission.RW, new IdentifiableImpl("student1"), new IdentifiableImpl("student2")))
			.build();



	}

}
