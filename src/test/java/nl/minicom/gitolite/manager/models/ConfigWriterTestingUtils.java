package nl.minicom.gitolite.manager.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.ConfigWriter;

import org.junit.Assert;
import org.junit.Ignore;

@Ignore
public class ConfigWriterTestingUtils {
	
	protected void validateWrittenConfig(String file, Config config) throws IOException {
		StringWriter result = new StringWriter();
		ConfigWriter.write(config, result);
		
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
	
	private String readFile(String file) throws IOException {
		InputStream in = getClass().getResourceAsStream("/" + file);
		if (in == null) {
			throw new FileNotFoundException();
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
		finally {
			reader.close();
		}
		
		return builder.toString();
	}
	
}
