package nl.tudelft.ewi.gitolite.config.parser.rules;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.sun.istack.internal.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.SneakyThrows;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.util.PrototypeGroupDefinition;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Builder
@EqualsAndHashCode(doNotUseGetters = true)
public class GroupRule implements PrototypeGroupDefinition<GroupRule, Identifiable>, Rule, Identifiable {

	@Getter
	private final String pattern;

	@Getter
	@Nullable
	private final GroupRule parent;

	@Singular
	private final List<Identifiable> members;

	@Singular
	private final List<GroupRule> groups;

	public GroupRule(final String pattern, final Identifiable... members) {
		this(pattern, null, Lists.newArrayList(members), Collections.emptyList());
	}

	public GroupRule(final String pattern,
	                 @Nullable final GroupRule parent,
	                 final Collection<? extends Identifiable> members,
	                 final Collection<? extends GroupRule> groups) {
		Preconditions.checkNotNull(pattern);
		Preconditions.checkNotNull(groups);
		Preconditions.checkNotNull(members);
		Preconditions.checkArgument(pattern.matches("^\\@\\w[\\w._\\@+-]+$"), "\"%s\" is not a valid group name", pattern);

		this.pattern = pattern;
		this.parent = parent;
		this.groups = Lists.newArrayList(groups);
		this.members = Lists.newArrayList(members);
	}

	@Override
	public Stream<GroupRule> getOwnGroups() {
		return groups.stream();
	}

	@Override
	public Stream<Identifiable> getOwnMembers() {
		return members.stream();
	}

	@Override
	public boolean remove(final Identifiable value) {
		return members.remove(value) || parent != null && parent.remove(value);
	}

	@Override
	public void add(Identifiable value) {
		members.add(value);
	}

	@Override
	public void add(GroupRule group) {
		groups.add(group);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(String.format("%-20s=   %s\n", pattern, Joiner.on(' ').join(Stream.concat(getOwnGroups(), getOwnMembers())
			.map(Identifiable::getPattern)
			.iterator())));
		writer.flush();
	}


	@Override
	@SneakyThrows
	public String toString() {
		try(StringWriter stringWriter = new StringWriter()) {
			write(stringWriter);
			return stringWriter.toString();
		}
	}

}
