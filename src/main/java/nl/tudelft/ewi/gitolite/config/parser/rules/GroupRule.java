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
import nl.tudelft.ewi.gitolite.config.objects.Identifier;
import nl.tudelft.ewi.gitolite.config.util.RecursiveAndPrototypeStreamingGroup;

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
@EqualsAndHashCode
public class GroupRule implements RecursiveAndPrototypeStreamingGroup<GroupRule, Identifier>, Rule, Identifiable {

	public final static GroupRule ALL = new GroupRule("@all") {

		@Override
		public boolean contains(Identifier value) {
			return true;
		}

	};

	@Getter
	private final String pattern;

	@Getter
	@Nullable
	private GroupRule parent;

	@Singular
	private final List<Identifier> members;

	@Singular
	private final List<GroupRule> groups;

	public GroupRule(final String pattern, final Identifier... members) {
		this(pattern, null, Lists.newArrayList(members), Collections.emptyList());
	}

	public GroupRule(final String pattern,
	                 @Nullable final GroupRule parent,
	                 final Collection<? extends Identifier> members,
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
	public Stream<Identifier> getOwnMembersStream() {
		return members.stream();
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
	public boolean remove(Identifier element) {
		return members.remove(element) || parent != null && parent.remove(element);
	}

	@Override
	public boolean delete(GroupRule group) {
		return groups.remove(group);
	}

	@Override
	public void add(Identifier value) {
		members.add(value);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(String.format("%-20s=   %s\n", pattern, Joiner.on(' ').join(Stream.concat(getOwnGroupsStream(), getOwnMembersStream())
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
