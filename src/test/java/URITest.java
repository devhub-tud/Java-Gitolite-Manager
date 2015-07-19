import org.junit.Test;

import java.net.URI;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class URITest {

	@Test
	public void testUri() {
		URI uri = URI.create("refs/.*").normalize();
		System.out.println(uri.getPath());
	}
}
