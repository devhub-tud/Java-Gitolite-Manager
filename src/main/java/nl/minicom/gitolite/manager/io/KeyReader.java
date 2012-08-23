package nl.minicom.gitolite.manager.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import nl.minicom.gitolite.manager.models.Config;

import com.google.common.base.Preconditions;

public class KeyReader {

	public void readKeys(Config config, File keyDir) throws IOException {
		Preconditions.checkNotNull(config);
		Preconditions.checkNotNull(keyDir);
		Preconditions.checkArgument(keyDir.isDirectory(), "The argument 'keyDir' must be a directory!");
		
		File[] files = keyDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".pub");
			}
		});
		
		for (File keyFile : files) {
			String fileName = keyFile.getName();
			if (!fileName.contains("@")) {
				fileName = fileName.replace(".pub", "@.pub");
			}
			
			int indexOfAt = fileName.indexOf('@');
			String userName = fileName.substring(0, indexOfAt);
			String keyName = fileName.substring(indexOfAt + 1, fileName.indexOf(".pub"));
			String content = readKeyFile(keyFile);
			
			config.ensureUserExists(userName).defineKey(keyName, content);
		}
	}

	private String readKeyFile(File keyFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(keyFile));
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
