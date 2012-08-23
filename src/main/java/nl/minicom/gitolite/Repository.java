package nl.minicom.gitolite;

import java.util.Comparator;

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

	static final Comparator<Repository> SORT_ALPHABETICALLY = new Comparator<Repository>() {
		@Override
		public int compare(Repository arg0, Repository arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	};

	private final String name;
	private final TreeMultimap<Permission, Identifiable> rights;
	
	Repository(String name) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		
		this.name = name;
		this.rights = TreeMultimap.create(Permission.SORT_ON_ORDINAL, Identifiable.SORT_BY_TYPE_AND_ALPHABETICALLY);
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * This method sets the {@link Permission} level for a specified {@link User} of {@link Group}.
	 * 
	 * @param entity
	 * 		The {@link Group} or {@link User} to set the permission for.
	 * 
	 * @param level
	 * 		The {@link Permission} which the specified {@link User} or {@link Group} should have.
	 */
	public void setPermission(Identifiable entity, Permission level) {
		Preconditions.checkNotNull(entity);
		Preconditions.checkNotNull(level);
		rights.put(level, entity);
	}

	/**
	 * @return
	 * 		An {@link ImmutableMultimap} containing all the {@link User}s and {@link Group}s 
	 * 		who have some kind of access on this {@link Repository} object. They're ordered
	 * 		by highest {@link Permission} to lowest {@link Permission}, and each permission 
	 * 		contains one or more {@link User}s and {@link Group}s.
	 */
	public ImmutableMultimap<Permission, Identifiable> getPermissions() {
		return ImmutableMultimap.copyOf(rights);
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
