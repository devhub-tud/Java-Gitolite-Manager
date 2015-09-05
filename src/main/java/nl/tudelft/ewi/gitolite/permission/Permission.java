package nl.tudelft.ewi.gitolite.permission;

import java.util.Collection;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstraction for both {@link BasePermission} and {@link PermissionWithModifier}.
 * Permission objects can be used from {@link BasePermission} constants or using the
 * {@link Permission#valueOf(String)} method.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface Permission {

	/**
	 * @return the {@link BasePermission} for this {@link Permission}.
	 */
	BasePermission getBasePermission();

	/**
	 * @return the {@link PermissionModifier PermissionModifiers} for this {@link Permission}.
	 * If the current {@code Permission} is a {@link BasePermission}, then it returns an empty list.
	 */
	Collection<PermissionModifier> getModifiers();

	/**
	 * Regular expression that matches permissions
	 */
	Pattern PERMISSION_PATTERN = Pattern.compile("^(-|C|R|RW\\+?)((?:C?D?|D?C?)M?)$");

	/**
	 * @return String value of this permission.
	 */
	String valueOf();

	/**
	 * Parse a {@code Permission}.
	 * @param input String to parse
	 * @return the parsed permission.
	 */
	static Permission valueOf(String input) {
		Matcher matcher = PERMISSION_PATTERN.matcher(input);
		if(!matcher.matches()) {
			throw new IllegalArgumentException(String.format("Input %s should be in the format %s",
				input, PERMISSION_PATTERN.pattern()));
		}
		BasePermission basePermission = BasePermission.parse(matcher.group(1));
		SortedSet<PermissionModifier> modifiers = PermissionModifier.parse(matcher.group(2));
		if(modifiers.isEmpty()) {
			return basePermission;
		}
		return new PermissionWithModifier(basePermission, modifiers);
	}

}
