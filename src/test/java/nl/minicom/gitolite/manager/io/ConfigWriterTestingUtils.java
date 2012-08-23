package nl.minicom.gitolite.manager.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import nl.minicom.gitolite.manager.io.ConfigWriter;
import nl.minicom.gitolite.manager.models.Config;

import org.junit.Assert;
import org.junit.Ignore;

@Ignore
public class ConfigWriterTestingUtils {
	
	protected void validateWrittenConfig(String file, Config config) throws IOException {
		StringWriter result = new StringWriter();
		ConfigWriter writer = new ConfigWriter();
		writer.write(config, result);
		
		String contents = readFile(file);
		Assert.assertEquals(compact(contents), compact(result.toString()));
	}
	
	private String compact(String value) {
		value = value.trim();
		while (value.contains("  ")) {
			value = value.replace("  ",  " ");
		}
		return value;
	}
	
	private String readFile(String file) {
		InputStream in = getClass().getResourceAsStream("/" + file);
		if (in == null) {
			Assert.fail("Could not locate file: " + file);
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder builder = new StringBuilder();
		
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				if (builder.length() > 0) {
					builder.append("\n");
				}
				builder.append(line);
			}
		} 
		catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		finally {
			try {
				reader.close();
			} 
			catch (IOException e) {
				Assert.fail(e.getMessage());
			}
		}
		
		return builder.toString();
	}
	
}
