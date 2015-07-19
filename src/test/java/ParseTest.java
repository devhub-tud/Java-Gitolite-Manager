import com.google.common.collect.Lists;
import nl.tudelft.ewi.gitolite.config.parser.Parser;
import nl.tudelft.ewi.gitolite.config.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRuleBlock;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.objects.IdentifiableImpl;
import nl.tudelft.ewi.gitolite.config.permission.Permission;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class ParseTest {

	private final static Identifiable foo = new IdentifiableImpl("foo");
	private final static Identifiable bar = new IdentifiableImpl("bar");

	private Parser parser;

	@Before
	public void before() {
		parser = new Parser();
	}

	@Test
	public void parseGroupRuleTest() throws IOException {
		GroupRule test = GroupRule.builder()
			.pattern("@test")
			.member(foo)
			.build();

		GroupRule other = GroupRule.builder()
			.pattern("@other")
			.group(test)
			.member(bar)
			.build();

		GroupRule actualA = parser.parseGroupRule(new Scanner("@test               =   foo"));
		GroupRule actualB = parser.parseGroupRule(new Scanner("@other              =   @test bar"));
		actualA.write(System.out);
		actualB.write(System.out);

		assertEquals(test, actualA);
		assertEquals(other, actualB);
	}

	@Test
	public void parseRepositoryRuleTest() throws IOException {
		RepositoryRule repositoryRule = new RepositoryRule(Permission.RW_PLUS, foo, bar);
		String rule = "RW+ = foo bar";

		RepositoryRule actual = parser.parseRepositoryRule(new Scanner(rule));
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseRepositoryRuleWithRefex() throws IOException {
		RepositoryRule repositoryRule = new RepositoryRule(Permission.RW_PLUS, "master", foo, bar);
		String rule = "RW+ master = foo bar";

		RepositoryRule actual = parser.parseRepositoryRule(new Scanner(rule));
		actual.write(System.out);
		assertEquals(repositoryRule, actual);
	}

	@Test
	public void parseBlockWithSingleRule() throws IOException {
		RepositoryRule repositoryRule = new RepositoryRule(Permission.RW_PLUS, foo, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock("yolo", repositoryRule);

		String rule = "repo yolo\n    RW+ = foo bar";

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock(new Scanner(rule));
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithTwoRules() throws IOException {
		RepositoryRule repositoryRule = new RepositoryRule(Permission.RW_PLUS, foo);
		RepositoryRule repositoryRule2 = new RepositoryRule(Permission.RW, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock("yolo", repositoryRule, repositoryRule2);

		String rule = "repo yolo\n" +
			"    RW+ = foo\n" +
			"    RW = bar";

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock(new Scanner(rule));
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithTwoRulesAndSpacing() throws IOException {
		RepositoryRule repositoryRule = new RepositoryRule(Permission.RW_PLUS, foo);
		RepositoryRule repositoryRule2 = new RepositoryRule(Permission.RW, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock("yolo", repositoryRule, repositoryRule2);

		String rule = "\nrepo yolo\n" +
			"    RW+ = foo\n" +
			"    RW = bar\n\n";

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock(new Scanner(rule));
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithTwoRepositories() throws IOException {
		RepositoryRule repositoryRule = new RepositoryRule(Permission.RW_PLUS, foo);
		RepositoryRule repositoryRule2 = new RepositoryRule(Permission.RW, bar);
		List<Identifiable> identifiables = Lists.newArrayList(new IdentifiableImpl("yolo"), new IdentifiableImpl("swag"));
		List<RepositoryRule> rules = Lists.newArrayList(repositoryRule, repositoryRule2);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock(identifiables, rules);

		String rule = "repo yolo swag\n" +
			"    RW+ = foo\n" +
			"    RW = bar\n\n";

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock(new Scanner(rule));
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}

	@Test
	public void parseBlockWithRepoGroup() throws IOException {
		GroupRule inhouse = parser.parseGroupRule(new Scanner("@inhouse = yolo swag"));;
		RepositoryRule repositoryRule = new RepositoryRule(Permission.RW_PLUS, foo, bar);
		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock(
			Lists.newArrayList(inhouse),
			Lists.newArrayList(repositoryRule)
		);

		String rule = "repo @inhouse\n    RW+ = foo bar";

		RepositoryRuleBlock actual = parser.parseRepositoryRuleBlock(new Scanner(rule));
		actual.write(System.out);
		assertEquals(repositoryRuleBlock, actual);
	}




}
