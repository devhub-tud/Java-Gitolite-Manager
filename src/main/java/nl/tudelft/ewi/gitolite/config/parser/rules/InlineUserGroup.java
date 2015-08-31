package nl.tudelft.ewi.gitolite.config.parser.rules;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import com.google.common.base.Joiner;

import lombok.Singular;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.util.RecursiveGroupDefinition;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Builder
@EqualsAndHashCode(doNotUseGetters = true)
public class InlineUserGroup implements RecursiveGroupDefinition<GroupRule, Identifiable>, Writable {

	@Singular
	private final List<GroupRule> groups;

	@Singular
	private final List<Identifiable> members;

	public InlineUserGroup(GroupRule... groups) {
		this(Arrays.asList(groups), Collections.emptyList());
	}

	public InlineUserGroup(Identifiable... identifiable) {
		this(Collections.emptyList(), Arrays.asList(identifiable));
	}

	public InlineUserGroup(Collection<? extends GroupRule> groups, Collection<? extends Identifiable> members) {
		this.groups = Lists.newArrayList(groups);
		this.members = Lists.newArrayList(members);
	}

	@Override
	public Stream<GroupRule> getGroups() {
		return groups.stream();
	}

	@Override
	public void add(GroupRule group) {
		groups.add(group);
	}

	@Override
	public Stream<Identifiable> getMembers() {
		return members.stream();
	}

	@Override
	public boolean remove(Identifiable value) {
		return members.remove(value);
	}

	@Override
	public void add(Identifiable value) {
		members.add(value);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(writeString());
	}

	private String writeString() {
		return Joiner.on(' ').join(Stream.concat(getGroups(), getMembers())
			.map(Identifiable::getPattern)
			.iterator());
	}

	@Override
	public String toString() {
		return writeString();
	}

}
