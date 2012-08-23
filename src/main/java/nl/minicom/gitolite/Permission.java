package nl.minicom.gitolite;

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

	public static Permission getByName(String permissionName) {
		Preconditions.checkNotNull(permissionName);
		Preconditions.checkArgument(!permissionName.isEmpty());
		
		for (Permission permission : values()) {
			if (permission.getName().equals(permissionName)) {
				return permission;
			}
		}
		return null;
	}

	private final String name;
	
	private Permission(String name) {
		this.name = Preconditions.checkNotNull(name);
	}
	
	public String getName() {
		return name;
	}
	
}
