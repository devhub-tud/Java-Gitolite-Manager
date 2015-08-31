package nl.tudelft.ewi.gitolite.config.permission;

import com.google.common.collect.Sets;
import nl.tudelft.ewi.gitolite.config.parser.rules.Writable;

import java.io.IOException;
import java.io.Writer;
import java.util.SortedSet;

/**
 * Sometimes you want to allow people to push, but not create a ref. Or rewind, but not delete a ref. The C and D qualifiers help here.
 * Note: These two can be combined, so you can have RWCD and RW+CD as well.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public enum PermissionModifier implements Writable {

	/**
	 * If a rule specifies {@code RWC} or {@code RW+C}, then rules that do NOT have the {@code C} qualifier will no longer permit creating a ref.
	 */
	C,

	/**
	 * If a rule specifies {@code RWD} or {@code RW+D}, then rules that do NOT have the D qualifier will no longer permit deleting a ref.
	 */
	D,

	/**
	 * When a rule has M appended to the permissions, rules that do NOT have it will reject a commit sequence that contains a merge commit (i.e., they only accept a straight line series of commits).
	 */
	M;

	public static SortedSet<PermissionModifier> parse(String input) {
		SortedSet<PermissionModifier> modifiers = Sets.newTreeSet();
		for(int i = 0, l = input.length(); i < l; i++) {
			modifiers.add(PermissionModifier.valueOf(input.substring(i, i + 1)));
		}
		return modifiers;
	}


	@Override
	public void write(Writer writer) throws IOException {
		writer.write(toString());
	}

}
