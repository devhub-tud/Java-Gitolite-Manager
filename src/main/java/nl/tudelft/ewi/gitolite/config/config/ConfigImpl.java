package nl.tudelft.ewi.gitolite.config.config;

import nl.tudelft.ewi.gitolite.config.parser.rules.Rule;
import nl.tudelft.ewi.gitolite.config.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRuleBlock;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Simple config implementation.
 *
 * Takes a list of {@link GroupRule GroupRules} and {@link RepositoryRuleBlock RepositoryRuleBlocks}.
 *
 * This implementation does not support:
 *
 * <ul>
 *    <li>Group redefinitions or group definitions after repository blocks.</li>
 *    <li>Imports</li>
 * </ul>
 *
 * @author Jan-Willem Gmelig Meyling
 */
public class ConfigImpl extends AbstractCollection<Rule> implements Collection<Rule> {

	private final List<GroupRule> groupRules;

	private final List<RepositoryRuleBlock> repositoryRuleBlocks;

	public ConfigImpl(List<GroupRule> groupRules, List<RepositoryRuleBlock> repositoryRuleBlocks) {
		this.groupRules = groupRules;
		this.repositoryRuleBlocks = repositoryRuleBlocks;
	}

	@Override
	public Iterator<Rule> iterator() {
		return Stream.concat(groupRules.stream(), repositoryRuleBlocks.stream()).iterator();
	}

	@Override
	public int size() {
		return (int) Stream.concat(groupRules.stream(), repositoryRuleBlocks.stream()).count();
	}

	@Override
	public boolean remove(Object o) {
		return groupRules.remove(o) || repositoryRuleBlocks.remove(o);
	}

	public boolean add(GroupRule groupRule) {
		return groupRules.add(groupRule);
	}

	public boolean add(RepositoryRuleBlock repositoryRuleBlock) {
		return repositoryRuleBlocks.add(repositoryRuleBlock);
	}

}
