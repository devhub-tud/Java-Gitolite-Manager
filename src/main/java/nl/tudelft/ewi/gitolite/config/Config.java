package nl.tudelft.ewi.gitolite.config;

import nl.tudelft.ewi.gitolite.objects.Identifiable;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.parser.rules.Rule;
import nl.tudelft.ewi.gitolite.parser.rules.Writable;

import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface Config extends Writable {

	/**
	 * Get a group from the config
	 * @param name the name for the group
	 * @return the group
	 */
	GroupRule getGroup(String name);

	/**
	 * Add a group.
	 * @param groupRule GroupRule for the group.
	 */
	void addGroup(GroupRule groupRule);

	/**
	 * Delete a group from the config.
	 * @param groupRule GroupRule for the group.
	 * @return true if the group was removed.
	 */
	boolean deleteGroup(GroupRule groupRule);

	/**
	 * @return all group rules
	 */
	Collection<? extends GroupRule> getGroupRules();

	/**
	 * Remove an identifier
	 * @param identifier The identifier to use
	 */
	void deleteIdentifierUses(Identifiable identifier);

	/**
	 * Get the repository rules for a set of identifiables.
	 * @param identifiables Set of identifiables
	 * @return a collection of repository rules
	 */
	Collection<? extends RepositoryRule> getRepositoryRule(Identifiable... identifiables);

	/**
	 * Get the repository rule for a set of identifiables.
	 * @param identifiables Set of identifiables.
	 * @return a repository rule.
	 */
	RepositoryRule getFirstRepositoryRule(Identifiable... identifiables);

	/**
	 * Add a RepositoryRule.
	 * @param repositoryRule RepositoryRule to add.
	 */
	void addRepositoryRule(RepositoryRule repositoryRule);

	/**
	 * Delete a repository rule.
	 * @param rule Rule to remove.
	 * @return true if the repository rule was removed.
	 */
	boolean deleteRepositoryRule(RepositoryRule rule);

	/**
	 * @return a list of rules
	 */
	Collection<Rule> getRules();

	void clear();
}
