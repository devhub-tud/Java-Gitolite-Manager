package nl.tudelft.ewi.gitolite.parser.rules;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * Configuration files may be extended with configuration keys if this is enabled in the {@code .gitolite.rc} file.
 * See <a href="http://gitolite.com/gitolite/rc.html">http://gitolite.com/gitolite/rc.html</a>.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class ConfigKey implements Rule {

	private final static Pattern UNSAFE_PATT = Pattern.compile("[`~#\\$\\&()|<>]");

	@Getter
	protected final String key;

	@Getter
	protected final String value;

	/**
	 * Create a new {@code ConfigKey}.
	 *
	 * @param key Key name.
	 * @param value Value under key.
	 */
	public ConfigKey(String key, Object value) {
		this.key = key;
		this.value = value.toString();
		if(UNSAFE_PATT.matcher(this.value).find()) {
			throw new IllegalArgumentException(String.format("Value contains unsafe characters!"));
		}
	}

	@Override
	public void write(Writer writer) throws IOException {
		writer.write(toString());
		writer.write('\n');
		writer.flush();
	}

	protected String escapeValue() {
		if(!value.contains(" ")) {
			return value;
		}
		return String.format(
			value.contains("\"") ? "'%s'" : "\"%s\"",
			value
		);
	}

	@Override
	public String toString() {
		return String.format("    config %s = %s", key, escapeValue());
	}

}
