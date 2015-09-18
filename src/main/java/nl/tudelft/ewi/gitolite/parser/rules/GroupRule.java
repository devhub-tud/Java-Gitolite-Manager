package nl.tudelft.ewi.gitolite.parser.rules;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.SneakyThrows;
import nl.tudelft.ewi.gitolite.objects.Identifiable;
import nl.tudelft.ewi.gitolite.objects.Identifier;
import nl.tudelft.ewi.gitolite.util.RecursiveAndPrototypeStreamingGroup;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@Data
@Builder
//@EqualsAndHashCode(doNotUseGetters = true)
public class GroupRule implements RecursiveAndPrototypeStreamingGroup<GroupRule, Identifier>, Rule, Identifiable {

	/**
	 * {@code @all} is a special group name that is often convenient to use if you really mean "all repos" or "all users".
	 */
	public final static GroupRule ALL = new GroupRule("@all") {

		@Override
		public boolean contains(Object value) {
			return true;
		}

		@Override
		public String toString() { return "@all"; }

		@Override
		public void write(Writer writer) {}

	};

	@Getter
	private final String pattern;

	@Getter
	private GroupRule parent;

	@Singular
	private final List<Identifier> members;

	@Singular
	private final List<GroupRule> groups;

	public GroupRule(final String pattern, final Identifier... members) {
		this(pattern, null, Lists.newArrayList(members), Collections.emptyList());
	}

	public GroupRule(final String pattern,
	                 final GroupRule parent,
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
	public boolean remove(Object element) {
		return members.remove(element) | groups.remove(element) | (parent != null && parent.remove(element));
	}

	@Override
	public boolean add(Identifier value) {
		return members.add(value);
	}

	@Override
	public boolean addAll(Collection<? extends Identifier> c) {
		return members.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return members.removeAll(c) | groups.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return members.retainAll(c) | groups.retainAll(c);
	}

	@Override
	public void clear() {
		members.clear();
		groups.clear();
	}

	@Override
	public boolean removeIf(Predicate<? super Identifier> filter) {
		return members.removeIf(filter);
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(String.format("%-20s=   %s\n", pattern, Joiner.on(' ').join(Stream.concat(getOwnGroupsStream(), getOwnMembersStream())
			.map(Identifiable::getPattern)
			.iterator())));
		writer.flush();
	}


	@Override
	public String toString() {
		return (String.format("%-20s=   %s\n", pattern, Joiner.on(' ').join(Stream.concat(getOwnGroupsStream(), getOwnMembersStream())
			.map(Identifiable::getPattern)
			.iterator())));
	}

}
