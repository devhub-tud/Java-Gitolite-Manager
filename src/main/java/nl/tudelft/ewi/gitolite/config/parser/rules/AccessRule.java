package nl.tudelft.ewi.gitolite.config.parser.rules;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.permission.Permission;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class AccessRule implements Writable {

	private final static String REFS_HEADS = "refs/heads/";

	/**
	 * If no refex is supplied, it defaults to refs/.*, for example in a rule like this:
	 *
	 * {@code <pre>
	 *    RW              =   alice
	 * </pre>}
	 */
	public final static String DEFAULT_REFEX = "refs/.*";

	/**
	 * The permission field gives the type of access this rule line permits.
	 */
	@Getter
	private final Permission permission;

	/**
	 * A refex is a word I made up to mean "a regex that matches a ref".
	 *
	 * <ul>
	 *    <li>
	 *       If no refex is supplied, it defaults to {@code refs/.*}.
	 *       A refex not starting with {@code refs/} (or {@code VREF/}) is assumed to start with {@code refs/heads/}.
	 *    </li>
	 *    <li>
	 *       A refex is implicitly anchored at the start, but not at the end.
	 *    </li>
	 *    <li>
	 *       In regular expression lingo, a {@code ^} is assumed at the start
	 *       (but no {@code $} at the end is assumed).
	 *    </li>
	 * </ul>
	 */
	@Getter
	private final String refex;

	/**
	 * Like the repos on the repo line, you can have any number of user names and/or user group names on the rule line.
	 * (However, please note that there is no concept of regular expressions for user names).
	 */
	@Getter
	private final InlineUserGroup members;

	/**
	 * Shorthand that allows you to write constructs like: {@code new RepositoryRule(Permission.RW_PLUS, foo)}.
	 * @param permission {@link Permission} for this {@code Rule}.
	 * @param identifiable {@link Identifiable Identifiables} for this {@code Rule}.
	 * @see AccessRule#AccessRule(Permission, String, InlineUserGroup)
	 */
	public AccessRule(final Permission permission, final Identifiable... identifiable) {
		this(permission, new InlineUserGroup(identifiable));
	}

	/**
	 * Shorthand that allows you to write constructs like: {@code new RepositoryRule(Permission.RW_PLUS, foo)}.
	 * @param permission {@link Permission} for this {@code Rule}.
	 * @param identifiable {@link Identifiable Identifiables} for this {@code Rule}.
	 * @see AccessRule#AccessRule(Permission, String, InlineUserGroup)
	 */
	public AccessRule(final Permission permission, final GroupRule... identifiable) {
		this(permission, new InlineUserGroup(identifiable));
	}

	public AccessRule(final Permission permission, final InlineUserGroup members) {
		this(permission, null, members);
	}

	/**
	 * Shorthand that allows you to write constructs like: {@code new RepositoryRule(Permission.RW_PLUS, foo)}.
	 * @param permission {@link Permission} for this {@code Rule}.
	 * @param refex refex for the rule
	 * @param identifiable {@link Identifiable Identifiables} for this {@code Rule}.
	 * @see AccessRule#AccessRule(Permission, String, InlineUserGroup)
	 */
	public AccessRule(final Permission permission, String refex, final Identifiable... identifiable) {
		this(permission, refex, new InlineUserGroup(identifiable));
	}

	public AccessRule(final Permission permission, final String refex, final InlineUserGroup members) {
		this.permission = permission;
		this.refex = refex;
		this.members = members;
	}

	public String getAdjustedRefex() {
		if(Strings.isNullOrEmpty(refex)) {
			return DEFAULT_REFEX;
		}
		else if(isVREF() || refex.startsWith("refs/")) {
			return refex;
		}
		else {
			return REFS_HEADS.concat(refex);
		}
	}

	public boolean isVREF() {
		return refex.startsWith("VREF/");
	}

	@Override
	public void write(final Writer writer) throws IOException {
		String permissionStr = permission.toString();
		String tempRexes = refex == null ? "" : refex;
		writer.write(String.format("    %-7s %-7s = ", permissionStr, tempRexes));
		members.write(writer);
		writer.write('\n');
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
