package nl.tudelft.ewi.gitolite.config.config;

import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.RepositoryRule;
import nl.tudelft.ewi.gitolite.config.parser.rules.Rule;

import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public interface Config {

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

}
