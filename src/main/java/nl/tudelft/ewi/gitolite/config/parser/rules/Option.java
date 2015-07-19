package nl.tudelft.ewi.gitolite.config.parser.rules;

import lombok.Getter;

/**
 * @author Jan-Willem Gmelig Meyling
 */
public class Option extends ConfigKey {

	@Getter
	private final String option;

	public Option(String option, Object value) {
		super("gitolite-options." + option, value);
		this.option = option;
	}

	@Override
	public String toString() {
		return String.format("    option %s = %s", getOption(), escapeValue());
	}

}
