import lombok.SneakyThrows;
import nl.tudelft.ewi.gitolite.permission.BasePermission;
import nl.tudelft.ewi.gitolite.permission.Permission;
import nl.tudelft.ewi.gitolite.permission.PermissionWithModifier;
import nl.tudelft.ewi.gitolite.permission.PermissionModifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(Parameterized.class)
public class PermissionTest {

	@Parameterized.Parameters(name = "Test Parse {0}")
	public static Collection primeNumbers() {
		return Arrays.asList(new Object[][]{
			{"R", BasePermission.R},
			{"RW", BasePermission.RW},
			{"C", BasePermission.C},
			{"RWM", new PermissionWithModifier(BasePermission.RW, PermissionModifier.M)},
			{"RWC", new PermissionWithModifier(BasePermission.RW, PermissionModifier.C)},
			{"RWCM", new PermissionWithModifier(BasePermission.RW, PermissionModifier.C, PermissionModifier.M)},
			{"RWD", new PermissionWithModifier(BasePermission.RW, PermissionModifier.D)},
			{"RWDM", new PermissionWithModifier(BasePermission.RW, PermissionModifier.D, PermissionModifier.M)},
			{"RWCD", new PermissionWithModifier(BasePermission.RW, PermissionModifier.C, PermissionModifier.D)},
			{"RWCDM", new PermissionWithModifier(BasePermission.RW, PermissionModifier.C, PermissionModifier.D, PermissionModifier.M)},
			{"RW+", BasePermission.RW_PLUS},
			{"RW+D", new PermissionWithModifier(BasePermission.RW_PLUS, PermissionModifier.D)},
			{"RW+C", new PermissionWithModifier(BasePermission.RW_PLUS, PermissionModifier.C)},
			{"RW+CD", new PermissionWithModifier(BasePermission.RW_PLUS, PermissionModifier.C, PermissionModifier.D)},
			{"RW+M", new PermissionWithModifier(BasePermission.RW_PLUS, PermissionModifier.M)},
		});
	}

	private final String type;

	private final Permission expected;

	public PermissionTest(String type, Permission expected) {
		this.type = type;
		this.expected = expected;
	}

	@Test
	public void testParse() {
		Permission permission = Permission.valueOf(type);
		Assert.assertEquals(expected, permission);
	}

	@Test
	@SneakyThrows
	public void testToString() {
		StringWriter stringWriter = new StringWriter();
		expected.write(stringWriter);
		Assert.assertEquals(type, stringWriter.toString());
	}

}
