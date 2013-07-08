package nl.minicom.gitolite.manager;

import java.util.Collections;
import java.util.List;

import nl.minicom.gitolite.manager.exceptions.ModificationException;
import nl.minicom.gitolite.manager.models.Config;
import nl.minicom.gitolite.manager.models.Repository;

import com.google.common.collect.Lists;

public class Changes {
	
	public static Change createRepository(final String repoName) {
		return new Change("Create repository: " + repoName) {
			@Override
			public void apply(Config config) throws ModificationException {
				config.createRepository(repoName);
			}
		};
	}

	public static Change removeRepository(final Repository repository) {
		return new Change("Remove repository: " + repository.getName()) {
			@Override
			public void apply(Config config) throws ModificationException {
				config.removeRepository(repository);
			}
		};
	}

	private final List<Change> changes;
	
	Changes() {
		this.changes = Lists.newArrayList();
	}
	
	public void add(Change change) {
		changes.add(change);
	}

	public List<Change> listChanges() {
		return Collections.unmodifiableList(changes);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Change change : changes) {
			builder.append(change.toString() + '\n');
		}
		return builder.toString();
	}
	
	public static abstract class Change {
		
		private final String description;

		public Change(String description) {
			this.description = description;
		}
		
		public abstract void apply(Config config) throws ModificationException;
		
		@Override
		public String toString() {
			return description;
		}
		
	}
	
}
