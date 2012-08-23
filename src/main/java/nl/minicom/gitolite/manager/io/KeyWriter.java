package nl.minicom.gitolite.manager.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.User;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class KeyWriter {

	public Set<File> writeKeys(Config config, File keyDir) throws IOException {
		Preconditions.checkNotNull(config);
		Preconditions.checkNotNull(keyDir);
		Preconditions.checkArgument(keyDir.isDirectory(), "The argument 'keyDir' must be a directory!");
	
		Set<File> keysWritten = Sets.newHashSet();
		for (User user : config.getUsers()) {
			for (Entry<String, String> keyEntry : user.getKeys().entrySet()) {
				String userName = user.getName();
				String keyName = keyEntry.getKey();
				String keyContent = keyEntry.getValue();
				
				keysWritten.add(createKeyFile(keyDir, userName, keyName, keyContent));
			}
		}
		
		return keysWritten;
	}

	private File createKeyFile(File keyDir, String userName, String keyName, String keyContent) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(userName);
		if (StringUtils.isNotEmpty(keyName)) {
			builder.append("@" + keyName);
		}
		builder.append(".pub");
		
		FileWriter writer = null;
		File file = new File(keyDir, builder.toString());
		try {
			writer = new FileWriter(file);
			writer.write(keyContent);
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		return file;
	}
	
}
