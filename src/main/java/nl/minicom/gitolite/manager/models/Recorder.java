package nl.minicom.gitolite.manager.models;

import java.util.List;

import nl.minicom.gitolite.manager.exceptions.ModificationException;

import com.google.common.collect.Lists;

public class Recorder {
	
	private final List<Modification> modifications;
	private boolean recording;
	
	Recorder() {
		this.modifications = Lists.newArrayList();
		this.recording = false;
	}
	
	void record() {
		recording = true;
	}
	
	void append(Modification modification) {
		if (recording) {
			modifications.add(modification);
		}
	}

	List<Modification> stop() {
		recording = false;
		return Lists.newArrayList(modifications);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Modification change : modifications) {
			builder.append(change.toString() + '\n');
		}
		return builder.toString();
	}
	
	public static abstract class Modification {
		
		private final String description;

		public Modification(String description, Object... parameters) {
			this.description = String.format(description, parameters);
		}
		
		public abstract void apply(Config config) throws ModificationException;
		
		@Override
		public String toString() {
			return description;
		}
		
	}
	
}
