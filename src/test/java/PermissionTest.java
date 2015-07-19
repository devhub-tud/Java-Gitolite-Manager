import nl.tudelft.ewi.gitolite.config.permission.BasePermission;
import nl.tudelft.ewi.gitolite.config.permission.Permission;
import nl.tudelft.ewi.gitolite.config.permission.PermissionModifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
			{"R", new Permission(BasePermission.R)},
			{"RW", new Permission(BasePermission.RW)},
			{"RWM", new Permission(BasePermission.RW, PermissionModifier.M)},
			{"RWC", new Permission(BasePermission.RW, PermissionModifier.C)},
			{"RWCM", new Permission(BasePermission.RW, PermissionModifier.C, PermissionModifier.M)},
			{"RWD", new Permission(BasePermission.RW, PermissionModifier.D)},
			{"RWDM", new Permission(BasePermission.RW, PermissionModifier.D, PermissionModifier.M)},
			{"RWCD", new Permission(BasePermission.RW, PermissionModifier.C, PermissionModifier.D)},
			{"RWCDM", new Permission(BasePermission.RW, PermissionModifier.C, PermissionModifier.D, PermissionModifier.M)},
			{"RW+", new Permission(BasePermission.RW_PLUS)},
			{"RW+D", new Permission(BasePermission.RW_PLUS, PermissionModifier.D)},
			{"RW+C", new Permission(BasePermission.RW_PLUS, PermissionModifier.C)},
			{"RW+CD", new Permission(BasePermission.RW_PLUS, PermissionModifier.C, PermissionModifier.D)},
			{"RW+M", new Permission(BasePermission.RW_PLUS, PermissionModifier.M)},
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
	public void testToString() {
		Assert.assertEquals(type, expected.toString());
	}

}
