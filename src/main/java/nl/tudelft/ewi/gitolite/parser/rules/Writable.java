package nl.tudelft.ewi.gitolite.parser.rules;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Something that can be written to a text ouput stream in a predefined format.
 *
 * @author Jan-Willem Gmelig Meyling
 */
public interface Writable {

	/**
	 * Write to a writer.
	 * @param writer Writer instance to write to.
	 * @throws IOException If an IO error occurs.
	 */
	void write(Writer writer) throws IOException;

	/**
	 * Write to a {@code OutputStream}.
	 * @param out {@code OutputStream} to write to.
	 */
	default void write(OutputStream out) throws IOException {
		write(new OutputStreamWriter(out));
	}

}
