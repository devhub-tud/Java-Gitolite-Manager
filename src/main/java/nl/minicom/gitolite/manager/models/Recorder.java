package nl.minicom.gitolite.manager.models;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.minicom.gitolite.manager.exceptions.ModificationException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Recorder {
	
	private final List<Modification> modifications;
	private final AtomicBoolean recording;
	
	Recorder() {
		this.modifications = Lists.newArrayList();
		this.recording = new AtomicBoolean();
	}
	
	void record() {
		recording.set(true);
	}
	
	void append(Modification modification) {
		if (recording.get()) {
			synchronized (modifications) {
				modifications.add(modification);
			}
		}
	}

	ImmutableList<Modification> stop() {
		recording.set(false);
		synchronized (modifications) {
			return ImmutableList.<Modification>builder().addAll(modifications).build();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		synchronized (modifications) {
			for (Modification change : modifications) {
				builder.append(change.toString() + '\n');
			}
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
