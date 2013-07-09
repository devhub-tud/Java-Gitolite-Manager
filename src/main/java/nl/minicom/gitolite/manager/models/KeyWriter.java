package nl.minicom.gitolite.manager.models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;


import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * This class contains a method to write all registered SSH keys in 
 * a specified {@link Config} object, to a specified directory.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public final class KeyWriter {

	/**
	 * This method writes all SSH keys currently present in the provided {@link Config} object
	 * to the specified key directory. Existing keys are not removed, but may be overwritten.
	 * 
	 * @param config
	 * 	The {@link Config} object, containing all the SSH keys. This cannot be NULL.
	 * 
	 * @param keyDir
	 * 	The directory where all the keys should be stored. This cannot be NULL.
	 * 
	 * @return
	 * 	A {@link Set} of {@link File} handles of all written SSH key files.
	 * 
	 * @throws IOException
	 * 	If a problem occurred when writing the SSH key files.
	 */
	public static Set<File> writeKeys(Config config, File keyDir) throws IOException {
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

	private static File createKeyFile(File keyDir, String userName, String name, String content) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(userName);
		if (StringUtils.isNotEmpty(name)) {
			builder.append("@" + name);
		}
		builder.append(".pub");
		
		FileWriter writer = null;
		File file = new File(keyDir, builder.toString());
		try {
			writer = new FileWriter(file);
			writer.write(content);
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
		
		return file;
	}
	
	private KeyWriter() {
		//Prevent instantiation.
	}
	
}
