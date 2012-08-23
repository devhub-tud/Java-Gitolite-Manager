package nl.minicom.gitolite.manager.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.User;

import com.google.common.base.Preconditions;

public class KeyWriter {

	public void writeKeys(Config config, File keyDir) throws IOException {
		Preconditions.checkNotNull(config);
		Preconditions.checkNotNull(keyDir);
		Preconditions.checkArgument(keyDir.isDirectory(), "The argument 'keyDir' must be a directory!");
		
		for (User user : config.getUsers()) {
			for (Entry<String, String> keyEntry : user.getKeys().entrySet()) {
				String userName = user.getName();
				String keyName = keyEntry.getKey();
				String keyContent = keyEntry.getValue();
				
				createKeyFile(keyDir, userName, keyName, keyContent);
			}
		}
	}

	private void createKeyFile(File keyDir, String userName, String keyName, String keyContent) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(userName);
		if (keyName != null && !keyName.isEmpty()) {
			builder.append("@" + keyName);
		}
		builder.append(".pub");
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(new File(keyDir, builder.toString()));
			writer.write(keyContent);
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
}
