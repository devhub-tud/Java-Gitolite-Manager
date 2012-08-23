package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.IOException;

public interface GitManager {

	void open() throws IOException;
	
	void remove(String filePattern);

	void clone(String uri);

	void init();

	boolean pull();

	void commitChanges();

	void push();

	File getWorkingDirectory();

}