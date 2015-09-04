package nl.tudelft.ewi.gitolite.parser.rules;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import com.google.common.base.Joiner;

import lombok.Singular;
import nl.tudelft.ewi.gitolite.objects.Identifiable;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.util.RecursiveStreamingGroup;

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
public class InlineUserGroup implements RecursiveStreamingGroup<GroupRule, Identifier>, Writable {

	@Singular
	private final List<GroupRule> groups;

	@Singular
	private final List<Identifier> members;

	public InlineUserGroup(GroupRule... groups) {
		this(Arrays.asList(groups), Collections.emptyList());
	}

	public InlineUserGroup(Identifier... identifiable) {
		this(Collections.emptyList(), Arrays.asList(identifiable));
	}

	public InlineUserGroup(Collection<? extends GroupRule> groups, Collection<? extends Identifier> members) {
		this.groups = Lists.newArrayList(groups);
		this.members = Lists.newArrayList(members);
	}

	@Override
	public Stream<GroupRule> getOwnGroupsStream() {
		return groups.stream();
	}

	@Override
	public void add(GroupRule group) {
		groups.add(group);
	}

	@Override
	public Stream<Identifier> getOwnMembersStream() {
		return members.stream();
	}

	@Override
	public boolean remove(Identifier value) {
		return members.remove(value);
	}

	@Override
	public boolean remove(GroupRule group) {
		return groups.remove(group);
	}

	@Override
	public void add(Identifier value) {
		members.add(value);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(writeString());
	}

	private String writeString() {
		return Joiner.on(' ').join(Stream.concat(getOwnGroupsStream(), getOwnMembersStream())
			.map(Identifiable::getPattern)
			.iterator());
	}

	@Override
	public String toString() {
		return writeString();
	}

}
