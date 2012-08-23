package nl.minicom.gitolite.manager.models;

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

	private final String name;
	private final Map<String, String> keys;
	
	/**
	 * Constructs a new {@link User} object with the provided name and public key.
	 * 
	 * @param name
	 * 		The name of the user.
	 */
	User(String name) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(!name.isEmpty());
		
		this.name = name;
		this.keys = Maps.newTreeMap();
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void defineKey(String name, String content) {
		keys.put(name, content);
	}
	
	public ImmutableMap<String, String> getKeys() {
		return ImmutableMap.copyOf(keys);
	}
	
	public void removeKey(String name) {
		keys.remove(name);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(name)
			.toHashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof User)) {
			return false;
		}
		
		return new EqualsBuilder()
			.append(name, ((User) other).name)
			.isEquals();
	}
	
}
