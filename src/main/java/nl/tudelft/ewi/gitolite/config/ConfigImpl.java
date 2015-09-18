package nl.tudelft.ewi.gitolite.config;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.gitolite.objects.Identifiable;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.AccessRule;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.InlineUserGroup;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.parser.rules.Rule;
import nl.tudelft.ewi.gitolite.util.StreamingGroup;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple config implementation.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Slf4j
@NoArgsConstructor
@EqualsAndHashCode
public class ConfigImpl implements Config {

	private final Multimap<String, GroupRule> groupRuleMultimap = LinkedListMultimap.create();

	private final List<RepositoryRule> repositoryRules = Lists.newArrayList();

	public ConfigImpl(Collection<? extends GroupRule> groupRules, Collection<? extends RepositoryRule> repositoryRules) {
		groupRules.stream().forEach(groupRule -> groupRuleMultimap.put(groupRule.getPattern(), groupRule));
		this.repositoryRules.addAll(repositoryRules);
	}

	@Override
	public GroupRule getGroup(String name) {
		return groupRuleMultimap.get(name).stream().findFirst().get();
	}

	@Override
	public void addGroup(GroupRule groupRule) {
		if(GroupRule.ALL.equals(groupRule)) return;
		// Add groups dependencies recursively
		groupRule.getOwnGroupsStream().forEach(this::addGroup);
		if(!groupRuleMultimap.containsEntry(groupRule.getPattern(), groupRule)) {
			groupRuleMultimap.put(groupRule.getPattern(), groupRule);
		}
	}

	@Override
	public Collection<GroupRule> getGroupRules() {
		return groupRuleMultimap.values();
	}

	@Override
	public void deleteIdentifierUses(Identifier identifier) {
		deleteIdentifierUsesInGroupRules(identifier);
		deleteIdentifierUsesInAccessRules(identifier);
		deleteIdentifierUsersInRepositoryRules(identifier);
		cleanUpModifiedRepositories();
	}

	protected void deleteIdentifierUsersInRepositoryRules(Identifier identifier) {
		repositoryRules.forEach(repositoryRule ->
			repositoryRule.removeIdentifiable(identifier));
	}

	protected void deleteIdentifierUsesInAccessRules(Identifier identifier) {
		repositoryRules.forEach(repositoryRule ->
			repositoryRule.getRules().removeIf(accessRule ->
				accessRule.getMembers().remove(identifier)));
	}

	protected void deleteIdentifierUsesInGroupRules(Identifier identifier) {
		getGroupRules().stream()
			.filter(group -> group.remove(identifier)) // Remove uses as well
			.filter(StreamingGroup::isEmpty)
			.forEach(this::deleteGroup); // Remove groups that have become empty
	}


	@Override
	public boolean deleteGroup(GroupRule groupRule) {
		Collection<GroupRule> groupRules = groupRuleMultimap.get(groupRule.getPattern());
		if(groupRules.remove(groupRule)){
			deleteGroupUses(groupRule);
			return true;
		}
		return false;
	}


	protected void deleteGroupUses(GroupRule groupRule) {
		deleteRecursiveGroupUsages(groupRule);
		deleteEmptyGroupRules();
		deleteGroupFromRepositoryRules(groupRule);
		deleteGroupFromAccessRules(groupRule);
		cleanUpModifiedRepositories();
	}

	protected void deleteEmptyGroupRules() {
		Queue<GroupRule> groupRulesQueue = Queues.newArrayDeque(groupRuleMultimap.values());
		while(!groupRulesQueue.isEmpty()) {
			GroupRule groupRule = groupRulesQueue.remove();
			if(groupRule.isEmpty() && deleteGroup(groupRule)) {
				groupRulesQueue.addAll(groupRuleMultimap.values());
			}
		}
	}

	protected void deleteRecursiveGroupUsages(GroupRule groupRule) {
		getGroupRules().stream()
			.forEach(group -> group.remove(groupRule)); // Remove uses as well
	}

	protected void deleteGroupFromRepositoryRules(GroupRule groupRule) {
		repositoryRules.forEach(repositoryRule ->
			repositoryRule.removeIdentifiable(groupRule));
	}

	protected void deleteGroupFromAccessRules(GroupRule groupRule) {
		repositoryRules.forEach(repositoryRule ->
			repositoryRule.getRules().removeIf(accessRule ->
				accessRule.getMembers().remove(groupRule)));
	}

	@Override
	public void cleanUpModifiedRepositories() {
		repositoryRules.forEach(repositoryRule ->
			repositoryRule.getRules().removeIf(accessRule ->
				accessRule.getMembers().isEmpty()));
		repositoryRules.removeIf(repositoryRule ->
			repositoryRule.getIdentifiables().isEmpty() ||
				repositoryRule.getRules().isEmpty() && repositoryRule.getConfigKeys().isEmpty());
	}

	@Override
	public List<RepositoryRule> getRepositoryRule(Identifiable... identifiables) {
		return repositoryRules.stream()
			.filter(forIdentifiables(identifiables))
			.collect(Collectors.toList());
	}

	private static Predicate<RepositoryRule> forIdentifiables(Identifiable... identifiables) {
		return repositoryRule -> repositoryRule.getIdentifiables().stream()
			.map(Identifiable::getPattern)
			.allMatch(identifiable -> Stream.of(identifiables)
				.map(Identifiable::getPattern)
				.anyMatch(identifiable::equals));
	}

	@Override
	public RepositoryRule getFirstRepositoryRule(Identifiable... identifiables) {
		return repositoryRules.stream()
			.filter(forIdentifiables(identifiables))
			.findFirst().get();
	}

	@Override
	public void addRepositoryRule(RepositoryRule repositoryRule) {
		ensureGroupsFromRepositoryExist(repositoryRule);
		repositoryRules.add(repositoryRule);
	}

	protected void ensureGroupsFromRepositoryExist(RepositoryRule repositoryRule) {
		Stream<Identifiable> identifiablesFromRepositoryRule = repositoryRule.getIdentifiables().stream();

		Stream<Identifiable> identifiablesFromAccessRules = repositoryRule.getRules().stream()
			.map(AccessRule::getMembers)
			.flatMap(InlineUserGroup::getOwnGroupsStream);

		Stream.concat(identifiablesFromRepositoryRule, identifiablesFromAccessRules)
			.filter(GroupRule.class::isInstance)
			.map(GroupRule.class::cast)
			.distinct()
			.forEach(this::addGroup);
	}

	@Override
	public boolean deleteRepositoryRule(RepositoryRule rule) {
		return repositoryRules.remove(rule);
	}

	@Override
	public Collection<Rule> getRules() {
		LinkedList<Rule> toposortRules = Lists.newLinkedList();
		getTopoSortGroupRules(toposortRules);
		toposortRules.addAll(repositoryRules);
		return toposortRules;
	}

	protected void getTopoSortGroupRules(LinkedList<? super Rule> toposortRules) {
		Queue<GroupRule> unmarkedNodes = Queues.newArrayDeque(getGroupRules());
		List<GroupRule> temporaryMarks = Lists.newArrayList();

		class Toposort {
			void visit(GroupRule groupRule) {
				if(temporaryMarks.contains(groupRule)) {
					throw new CyclicDependencyException();
				}
				if(unmarkedNodes.contains(groupRule)) {
					temporaryMarks.add(groupRule);
					if(groupRule.hasParent()) {
						visit(groupRule.getParent());
					}
					groupRule.getOwnGroupsStream().sequential().forEach(this::visit);
					unmarkedNodes.remove(groupRule);
					temporaryMarks.remove(groupRule);
					toposortRules.addLast(groupRule);
				}
			}
		}

		Toposort t = new Toposort();
		while(!unmarkedNodes.isEmpty()) {
			GroupRule groupRule = unmarkedNodes.peek();
			t.visit(groupRule);
		}
	}

	@Override
	public void write(Writer writer) throws IOException {
		for(Rule rule : getRules()) {
			rule.write(writer);
		}
	}
}
