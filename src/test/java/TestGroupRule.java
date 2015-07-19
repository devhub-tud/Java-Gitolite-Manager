
import nl.tudelft.ewi.gitolite.config.parser.rules.GroupRule;
import nl.tudelft.ewi.gitolite.config.objects.Identifiable;
import nl.tudelft.ewi.gitolite.config.objects.IdentifiableImpl;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class TestGroupRule {

	@Test
	public void test1() throws IOException {
		Identifiable foo = new IdentifiableImpl("foo");
		Identifiable bar = new IdentifiableImpl("bar");

		GroupRule test = GroupRule.builder()
			.pattern("@test")
			.member(foo)
			.build();

		GroupRule other = GroupRule.builder()
			.pattern("@other")
			.group(test)
			.member(bar)
			.build();

		test.write(System.out);
		other.write(System.out);

		List<Identifiable> members = other.getInheritedMembers().collect(Collectors.toList());
		members.forEach(System.out::println);
		assertThat(members, contains(foo, bar));
	}

}
