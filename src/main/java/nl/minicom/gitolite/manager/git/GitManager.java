package nl.minicom.gitolite.manager.git;

import java.io.File;
import java.io.IOException;

public interface GitManager {

	public abstract void open() throws IOException;

	public abstract void clone(String uri);

	public abstract void init();

	public abstract boolean pull();

	public abstract void commitChanges();

	public abstract void push();

	public abstract File getWorkingDirectory();

}