package nl.tudelft.ewi.gitolite.config.repositories;

import lombok.Value;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * A wrapper for a file size, which enhances it with a pretty print.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Value
public class FileSize implements Comparable<FileSize>, Serializable {

	private final static String[] UNITS = new String[] { "B", "KB", "MB", "GB", "TB" };

	/**
	 * The actual size in bytes.
	 */
	private final long size;

	@Override
	public String toString() {
		int unitIndex = (int) (Math.log10(size) / 3);
		double unitValue = 1 << (unitIndex * 10);
		return new DecimalFormat("#,##0.#").format(size / unitValue) + " " + UNITS[unitIndex];
	}

	@Override
	public int compareTo(FileSize o) {
		return Long.compare(getSize(), o.getSize());
	}

}
