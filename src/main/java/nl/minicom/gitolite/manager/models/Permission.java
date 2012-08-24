package nl.minicom.gitolite.manager.models;

import java.util.Comparator;

import com.google.common.base.Preconditions;

/**
 * This enum represents all the available permissions in Gitolite.
 *
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public enum Permission {

	ALL			("RW+"),
	READ_WRITE  ("RW"),
	READ_ONLY 	("R");

	static final Comparator<Permission> SORT_ON_ORDINAL = new Comparator<Permission>() {
		@Override
		public int compare(Permission arg0, Permission arg1) {
			return arg0.ordinal() - arg1.ordinal();
		}
	};

	/**
	 * This method finds and returns the {@link Permission} with the specified level.
	 * 
	 * @param level
	 * 	The level of the {@link Permission} to look for.
	 * 	This may not be NULL nor an empty {@link String}.
	 * 
	 * @return
	 * 	The {@link Permission} with the specified name.
	 */
	public static Permission getByLevel(String level) {
		Preconditions.checkNotNull(level);
		Preconditions.checkArgument(!level.isEmpty());
		
		for (Permission permission : values()) {
			if (permission.getLevel().equals(level)) {
				return permission;
			}
		}
		return null;
	}

	private final String level;

	private Permission(String level) {
		this.level = Preconditions.checkNotNull(level);
	}
	
	/**
	 * @return
	 * 	The level of the {@link Permission}.
	 */
	public String getLevel() {
		return level;
	}
	
}
