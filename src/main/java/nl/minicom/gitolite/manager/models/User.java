package nl.minicom.gitolite.manager.models;

import java.util.Comparator;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * This class represents a user in Gitolite.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class User implements Identifiable {

	static final Comparator<User> SORT_BY_NAME = new Comparator<User>() {
		@Override
		public int compare(User arg0, User arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	};

	private final String name;
	private final Map<String, String> keys;
	
	/**
	 * Constructs a new {@link User} object with the provided name and public key.
	 * 
	 * @param name
	 * 	The name of the user.
	 */
	User(String name) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		
		this.name = name;
		this.keys = Maps.newTreeMap();
	}

	/**
	 * @return
	 * 	The name of the {@link User}.
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * This method allows you to set (and override existing) SSH keys for this particular {@link User}.
	 * 
	 * @param name
	 * 	The name of the key. This may not be NULL.
	 * 
	 * @param content
	 * 	The content of the public key file. This may not be NULL.
	 */
	public void defineKey(String name, String content) {
		Preconditions.checkNotNull(name);
		Preconditions.checkNotNull(content);
		
		keys.put(name, content);
	}
	
	/**
	 * @return
	 * 	An {@link ImmutableMap} of SSH keys for this user. The key of the {@link Map}
	 * 	is the name of the key, and the value is the contents of the associated key file.
	 */
	public ImmutableMap<String, String> getKeys() {
		return ImmutableMap.copyOf(keys);
	}
	
	/**
	 * This method removes the SSH key with the specified name from this {@link User} object.
	 *  
	 * @param name
	 * 	The name of the SSH key to remove.
	 */
	public void removeKey(String name) {
		keys.remove(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(name)
			.toHashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof User)) {
			return false;
		}
		
		return new EqualsBuilder()
			.append(name, ((User) other).name)
			.isEquals();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
