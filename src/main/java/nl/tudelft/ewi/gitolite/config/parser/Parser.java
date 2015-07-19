package nl.tudelft.ewi.gitolite.config.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.tudelft.ewi.gitolite.config.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.InlineUserGroup;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRuleBlock;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.objects.IdentifiableImpl;
import nl.tudelft.ewi.gitolite.config.permission.Permission;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class Parser {

	private Map<String, GroupRule> groupRuleMap = Maps.newHashMap();
	private Map<String, Identifiable> identifiableMap = Maps.newHashMap();

	public Identifiable getIdentifiable(String name) {
		Identifiable identifiable = identifiableMap.get(name);
		if(identifiable == null) {
			identifiable = new IdentifiableImpl(name);
			identifiableMap.put(name, identifiable);
		}
		return identifiable;
	}

	public InlineUserGroup parseInlineUserGroup(Scanner scanner) {
		List<GroupRule> groups = Lists.newArrayList();
		List<Identifiable> members = Lists.newArrayList();
		parseInlineGroup(scanner, groups, members);
		return new InlineUserGroup(groups, members);
	}

	/**
	 * Consume a list of tokens {@code [@foo bar]} from a {@code Scanner} and put them in their
	 * corresponding {@code List}.
	 * @param scanner {@code Scanner} to consume.
	 * @param groups {@code List} for {@link GroupRule} references.
	 * @param members {@code List} for {@link Identifiable} references.
	 */
	public void parseInlineGroup(Scanner scanner, List<? super GroupRule> groups, List<? super Identifiable> members) {
		while(scanner.hasNext()) {
			String idName = scanner.next();
			if(idName.startsWith("@")) {
				GroupRule groupRule = groupRuleMap.get(idName);
				Preconditions.checkNotNull(groupRule);
				groups.add(groupRule);
			}
			else {
				members.add(getIdentifiable(idName));
			}
		}
	}

	public GroupRule parseGroupRule(Scanner scanner) {
		String name = scanner.next();
		scanner.next("=");

		GroupRule parent = groupRuleMap.get(name);
		List<GroupRule> groups = Lists.newArrayList();
		List<Identifiable> members = Lists.newArrayList();
		parseInlineGroup(scanner, groups, members);

		GroupRule rule = new GroupRule(name, parent, members, groups);
		groupRuleMap.put(name, rule);
		return rule;
	}

	public RepositoryRule parseRepositoryRule(Scanner scanner) {
		Permission permission = Permission.valueOf(scanner.next());
		String next = scanner.next();
		String refex = null;
		if(!next.equals("=")) {
			refex = next;
			scanner.next("=");
		}
		InlineUserGroup inlineUserGroup = parseInlineUserGroup(scanner);
		return new RepositoryRule(permission, refex, inlineUserGroup);
	}

	public RepositoryRuleBlock parseRepositoryRuleBlock(Scanner scanner) {
		List<Identifiable> identifiables = Lists.newArrayList();
		Scanner sc = new Scanner(readLine(scanner));
		sc.next("repo");
		parseInlineGroup(sc, identifiables, identifiables);

		List<RepositoryRule> rules = Lists.newArrayList();
		while(scanner.hasNext()) {
			sc = new Scanner(readLine(scanner));
			RepositoryRule rule = parseRepositoryRule(sc);
			rules.add(rule);
		}

		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock(identifiables, rules);
		return repositoryRuleBlock;
	}

	public static String readLine(Scanner sc) {
		String line = sc.nextLine();
		while(line.isEmpty() && sc.hasNextLine()) {
			line = sc.nextLine();
		}
		return line;
	}

}
