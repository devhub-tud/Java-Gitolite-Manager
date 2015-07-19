package nl.tudelft.ewi.gitolite.config.permission;

import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class Permission {

	private final static Pattern pattern = Pattern.compile("^(-|C|R|RW\\+?)((?:C?D?|D?C?)M?)$");

	public static Permission C = new Permission(BasePermission.C);
	public static Permission R = new Permission(BasePermission.R);
	public static Permission RW = new Permission(BasePermission.RW);
	public static Permission RW_PLUS = new Permission(BasePermission.RW_PLUS);

	public static Permission valueOf(String input) {
		Matcher matcher = pattern.matcher(input);
		if(!matcher.matches()) {
			throw new IllegalArgumentException(String.format("Input %s should be in the format %s",
				input, pattern.pattern()));
		}
		BasePermission basePermission = BasePermission.parse(matcher.group(1));
		SortedSet<PermissionModifier> modifiers = PermissionModifier.parse(matcher.group(2));
		return new Permission(basePermission, modifiers);
	}

	private final BasePermission basePermission;

	private final SortedSet<PermissionModifier> modifiers;

	public Permission(BasePermission basePermission, PermissionModifier... modifiers) {
		this(basePermission, Sets.newTreeSet(Arrays.asList(modifiers)));
	}

	public Permission(BasePermission basePermission, SortedSet<PermissionModifier> modifiers) {
		this.basePermission = basePermission;
		this.modifiers = modifiers;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(3 + modifiers.size());
		builder.append(basePermission.getField());
		for(PermissionModifier permissionModifier : modifiers) {
			builder.append(permissionModifier.toString());
		}
		return builder.toString();
	}

}
