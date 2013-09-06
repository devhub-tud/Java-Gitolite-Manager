package nl.minicom.gitolite.manager.models;

import java.util.Comparator;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.models.Recorder.Modification;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.TreeMultimap;

/**
 * This class represents a repository in Gitolite. You can set access permissions 
 * for {@link User}s who should be able to access the represented repository.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class Repository {

	static final Comparator<Repository> SORT_BY_NAME = new Comparator<Repository>() {
		@Override
		public int compare(Repository arg0, Repository arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	};

	private final String name;
	private final TreeMultimap<Permission, Identifiable> rights;
	private final Recorder recorder;
	
	/**
	 * Constructs a new {@link Repository} object with the specified name.
	 * 
	 * @param name
	 * 	The name of the {@link Repository}.
	 * 
	 * @param recorder
	 * 	The {@link Recorder} to use when recording changes of this {@link Repository}.
	 */
	Repository(String name, Recorder recorder) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		Preconditions.checkNotNull(recorder);
		
		this.name = name;
		this.recorder = recorder;
		this.rights = TreeMultimap.create(Permission.SORT_ON_ORDINAL, Identifiable.SORT_BY_TYPE_AND_NAME);
	}

	/**
	 * @return
	 * 	The name of the repository
	 */
	public String getName() {
		return name;
	}

	/**
	 * This method sets the {@link Permission} level for a specified {@link User}.
	 * 
	 * @param user
	 * 	The {@link User} to set the permission for.
	 * 
	 * @param level
	 * 	The {@link Permission} which the specified {@link User} should have.
	 */
	public void setPermission(User user, final Permission level) {
		Preconditions.checkNotNull(user);
		Preconditions.checkNotNull(level);
		
		synchronized (rights) {
			rights.put(level, user);
		}
		
		final String userName = user.getName();
		recorder.append(new Modification("Setting permission for user: '%s' to repository: '%s'", userName, getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				Repository repo = config.getRepository(getName());
				User user = config.getUser(userName);
				repo.setPermission(user, level);
			}
		});
	}

	/**
	 * This method sets the {@link Permission} level for a specified {@link Group}.
	 * 
	 * @param group
	 * 	The {@link Group} to set the permission for.
	 * 
	 * @param level
	 * 	The {@link Permission} which the specified {@link Group} should have.
	 */
	public void setPermission(Group group, final Permission level) {
		Preconditions.checkNotNull(group);
		Preconditions.checkNotNull(level);
		
		synchronized (rights) {
			rights.put(level, group);
		}
		
		final String groupName = group.getName();
		recorder.append(new Modification("Setting permission for: '%s' to: '%s'", groupName, getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				Repository repo = config.getRepository(getName());
				Group group = config.getGroup(groupName);
				repo.setPermission(group, level);
			}
		});
	}

	/**
	 * This method revokes all rights on this {@link Repository} for the 
	 * specified {@link User}.
	 * 
	 * @param user
	 * 	The {@link User} whose permissions need to be revoked.
	 */
	public void revokePermissions(User user) {
		synchronized (rights) {
			for (Permission permission : Permission.values()) {
				rights.remove(permission, user);
			}
		}
		
		final String userName = user.getName();
		recorder.append(new Modification("Revoking permission for: '%s' from: '%s'", userName, getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				Repository repo = config.getRepository(getName());
				if (repo == null) {
					throw new ModificationException();
				}
				
				User user = config.getUser(userName);
				if (user == null) {
					throw new ModificationException();
				}
				
				repo.revokePermissions(user);
			}
		});
	}

	/**
	 * This method revokes all rights on this {@link Repository} for the 
	 * specified {@link Group}.
	 * 
	 * @param group
	 * 	The {@link Group} whose permissions need to be revoked.
	 */
	public void revokePermissions(Group group) {
		synchronized (rights) {
			for (Permission permission : Permission.values()) {
				rights.remove(permission, group);
			}
		}
		
		final String groupName = group.getName();
		recorder.append(new Modification("Revoking permission for: '%s' to: '%s'", groupName, getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				Repository repo = config.getRepository(getName());
				if (repo == null) {
					throw new ModificationException();
				}
				
				Group group = config.getGroup(groupName);
				if (group == null) {
					throw new ModificationException();
				}
				
				repo.revokePermissions(group);
			}
		});
	}

	/**
	 * @return
	 * 	An {@link ImmutableMultimap} containing all the {@link User}s and {@link Group}s 
	 * 	who have some kind of access on this {@link Repository} object. They're ordered
	 * 	by highest {@link Permission} to lowest {@link Permission}, and each permission 
	 * 	contains one or more {@link User}s and {@link Group}s.
	 */
	public ImmutableMultimap<Permission, Identifiable> getPermissions() {
		synchronized (rights) {
			return ImmutableMultimap.copyOf(rights);
		}
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(name)
			.toHashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Repository)) {
			return false;
		}
		return new EqualsBuilder()
			.append(name, ((Repository) other).name)
			.isEquals();
	}
	
}
