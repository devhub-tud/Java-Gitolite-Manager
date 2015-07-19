package nl.tudelft.ewi.gitolite.config.parser.rules;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.objects.IdentifiableImpl;
import nl.tudelft.ewi.gitolite.config.objects.Rule;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class RepositoryRuleBlock implements Rule {

	/**
	 * Regex pattern for the repositories.
	 * Due to projects like gtk+, the + character is now considered a valid character for an ordinary repo.
	 * Therefore, a regex like foo/.+ does not look like a regex to gitolite. Use foo/..* if you want that.
	 * Also, ..* by itself is not considered a valid repo regex. Try [a-zA-Z0-9].*. CREATOR/..* will also work.
	 */
	private final List<Identifiable> identifiables;

	/**
	 * Rules active for the repositories that match this pattern.
	 */
	private final List<RepositoryRule> rules;

	/**
	 * Helper method to quickly initialize rules in the following way:
	 *
	 * {@code
	 * <pre>
	 *    new RepositoryRuleBlock("foo", new RepositoryRule(RW_PLUS, members);
	 * </pre>}
	 *
	 * @param pattern {@see identifiables}
	 * @param rules {@see rules}
	 */
	public RepositoryRuleBlock(String pattern, RepositoryRule... rules) {
		this(Arrays.asList(new IdentifiableImpl(pattern)), Arrays.asList(rules));
	}

	/**
	 * Constructor for {@code RepositoryRuleBlock}.
	 *
	 * @param patterns {@see identifiables}
	 * @param rules {@see rules}
	 */
	public RepositoryRuleBlock(List<Identifiable> patterns, List<RepositoryRule> rules) {
		this.identifiables = patterns;
		this.rules = rules;
	}

	public RepositoryRuleBlock addRule(RepositoryRule repositoryRule) {
		rules.add(repositoryRule);
		return this;
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write("repo");
		for(Identifiable identifiable : identifiables) {
			writer.write(' ');
			writer.write(identifiable.getPattern());
		}
		writer.write('\n');
		for(RepositoryRule rule : rules) {
			rule.write(writer);
		}
		writer.flush();
	}

	@Override
	@SneakyThrows
	public String toString() {
		try(StringWriter stringWriter = new StringWriter()) {
			write(stringWriter);
			return stringWriter.toString();
		}
	}

}
