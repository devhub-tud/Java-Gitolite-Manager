package nl.tudelft.ewi.gitolite.config.permission;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public enum BasePermission implements Permission {

	/**
	 * Allow pretty much anything -- fast-forward, rewind or remove branches or tags.
	 */
	RW_PLUS("RW+"),

	/**
	 * Allow fast-forward push of a branch, or create new branch/tag.
	 */
	RW("RW"),

	/**
	 * Allow read operations only.
	 */
	R("R"),

	/**
	 * Create repositories.
	 */
	C("C"),

	/**
	 * Deny access.
	 */
	DENY("-");


	private final String field;

	BasePermission(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public static BasePermission parse(String input) {
		for(BasePermission basePermission : values()) {
			if(basePermission.field.equals(input)) {
				return basePermission;
			}
		}
		throw new IllegalArgumentException("No permission found for " + input);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(getField());
	}

	@Override
	public BasePermission getBasePermission() {
		return this;
	}

	@Override
	public Collection<PermissionModifier> getModifiers() {
		return Collections.emptyList();
	}

}
