package nl.tudelft.ewi.gitolite.config.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import nl.tudelft.ewi.gitolite.config.parser.rules.ConfigKey;
import nl.tudelft.ewi.gitolite.config.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.InlineUserGroup;
import nl.tudelft.ewi.gitolite.config.parser.rules.Option;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRuleBlock;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.objects.IdentifiableImpl;
import nl.tudelft.ewi.gitolite.config.permission.Permission;

import java.text.CharacterIterator;
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

	public ConfigKey parseConfigRule(Scanner scanner) {
		scanner.next("config");
		String key = scanner.next();
		scanner.next("=");
		String value = removeQuotes(scanner.nextLine());
		return new ConfigKey(key, value);
	}

	public Option parseOptionRule(Scanner scanner) {
		scanner.next("option");
		String option = scanner.next();
		scanner.next("=");
		String value = removeQuotes(scanner.nextLine());
		return new Option(option, value);
	}

	public RepositoryRuleBlock parseRepositoryRuleBlock(Scanner scanner) {
		List<Identifiable> identifiables = Lists.newArrayList();
		Scanner sc = new Scanner(readLine(scanner));
		sc.next("repo");
		parseInlineGroup(sc, identifiables, identifiables);

		List<RepositoryRule> rules = Lists.newArrayList();
		List<ConfigKey> configKeys = Lists.newArrayList();

		while(scanner.hasNext()) {
			sc = new Scanner(readLine(scanner));
			boolean hasOption;
			if((hasOption = sc.hasNext("option")) || sc.hasNext("config")) {
				ConfigKey configKey;
				if(hasOption) {
					configKey = parseOptionRule(sc);
				} else {
					configKey = parseConfigRule(sc);
				}
				configKeys.add(configKey);
			}
			else {
				RepositoryRule rule = parseRepositoryRule(sc);
				rules.add(rule);
			}
		}

		RepositoryRuleBlock repositoryRuleBlock = new RepositoryRuleBlock(identifiables, rules, configKeys);
		return repositoryRuleBlock;
	}

	public static String readLine(Scanner sc) {
		String line = sc.nextLine();
		while(line.isEmpty() && sc.hasNextLine()) {
			line = sc.nextLine();
		}
		return line;
	}

	private static String removeQuotes(String input) {
		for(int i = 0, l = input.length(); i < l; i++) {
			switch(input.charAt(i)) {
				case ' ':
					continue;
				case '\'':
					return input.substring(i + 1, readUntil(input, i, '\''));
				case '"':
					return input.substring(i + 1, readUntil(input, i, '"'));
				default:
					return input.substring(i, readUntil(input, i, ' '));
			}
		}
		return input;
	}

	private static int readUntil(final String input, final int from, final char toRead) {
		int l = input.length();
		for(int i = from + 1; i < l; i++) {
			if(input.charAt(i) == toRead) {
				return i;
			}
		}
		return l;
	}

}
