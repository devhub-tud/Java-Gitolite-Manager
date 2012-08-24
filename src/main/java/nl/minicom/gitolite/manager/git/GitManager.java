package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.IOException;

public interface GitManager {

	void open() throws IOException;
	
	void remove(String filePattern) throws IOException;

	void clone(String uri) throws IOException;

	void init() throws IOException;

	boolean pull() throws IOException;

	void commitChanges() throws IOException;

	void push() throws IOException;

	File getWorkingDirectory();

}