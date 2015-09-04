package nl.tudelft.ewi.gitolite.parser.rules;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.Value;
import nl.tudelft.ewi.gitolite.objects.Identifiable;
import nl.tudelft.ewi.gitolite.objects.Identifier;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Value
@Builder
@EqualsAndHashCode
public class RepositoryRule implements Rule {

	/**
	 * Regex pattern for the repositories.
	 * Due to projects like gtk+, the + character is now considered a valid character for an ordinary repo.
	 * Therefore, a regex like foo/.+ does not look like a regex to gitolite. Use foo/..* if you want that.
	 * Also, ..* by itself is not considered a valid repo regex. Try [a-zA-Z0-9].*. CREATOR/..* will also work.
	 */
	@Singular
	private final List<Identifiable> identifiables;

	/**
	 * Rules active for the repositories that match this pattern.
	 */
	@Singular
	private final List<AccessRule> rules;

	/**
	 * Configuration keys.
	 */
	@Singular
	private final List<ConfigKey> configKeys;

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
	public RepositoryRule(String pattern, AccessRule... rules) {
		this(Collections.singletonList(new Identifier(pattern)), Arrays.asList(rules), Collections.emptyList());
	}

	/**
	 * Constructor for {@code RepositoryRuleBlock}.
	 *
	 * @param patterns {@see identifiables}
	 * @param rules {@see rules}
	 * @param configKeys {@see configKeys}
	 */
	public RepositoryRule(final Collection<? extends Identifiable> patterns,
	                      final Collection<? extends AccessRule> rules,
	                      final Collection<? extends ConfigKey> configKeys) {
		assert patterns != null;
		assert rules != null;
		assert configKeys != null;

		this.identifiables = Lists.newArrayList(patterns);
		this.rules = Lists.newArrayList(rules);
		this.configKeys = Lists.newArrayList(configKeys);
	}

	public RepositoryRule addRule(AccessRule accessRule) {
		rules.add(accessRule);
		return this;
	}

	public boolean removeIdentifiable(Identifiable o) {
		return getIdentifiables().remove(o);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write("\nrepo");
		for(Identifiable identifiable : identifiables) {
			writer.write(' ');
			writer.write(identifiable.getPattern());
		}
		writer.write('\n');
		for(AccessRule rule : rules) {
			rule.write(writer);
		}
		writer.write('\n');
		for(ConfigKey config : configKeys) {
			config.write(writer);
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
