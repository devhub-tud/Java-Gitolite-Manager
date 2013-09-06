package nl.minicom.gitolite.manager.models;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.minicom.gitolite.manager.exceptions.ModificationException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The {@link Recorder} class is a simple class which can be used to keep track 
 * of changes of certain objects. These changes can then later be replayed on other 
 * objects. 
 */
public class Recorder {
	
	private final List<Modification> modifications;
	private final AtomicBoolean recording;
	
	/**
	 * Constructs a new {@link Recorder} object.
	 */
	Recorder() {
		this.modifications = Lists.newArrayList();
		this.recording = new AtomicBoolean();
	}
	
	/**
	 * This method ensures that from now on, the {@link Recorder} will record all changes.
	 */
	void record() {
		recording.set(true);
	}
	
	/**
	 * This method can be called when you want to record a certain {@link Modification}.
	 * 
	 * @param modification
	 * 	The {@link Modification} to record, for later playback.
	 */
	void append(Modification modification) {
		if (recording.get()) {
			synchronized (modifications) {
				modifications.add(modification);
			}
		}
	}

	/**
	 * This method ensures that the {@link Recorder} stops recording changes.
	 * 
	 * @return
	 * 	The method returns an {@link ImmutableList} of {@link Modification} objects
	 * 	which represent all recorded changes since this {@link Recorder} started 
	 * 	recording changes.
	 */
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
	
	/**
	 * This abstract class represents a change which can be re-applied to any {@link Config} object.
	 */
	public abstract static class Modification {
		
		private final String description;

		/**
		 * Constructs a new {@link Modification} object, based on a description and some description paramters.
		 * 
		 * @param description
		 * 	The description of this {@link Modification} object.
		 * 
		 * @param parameters
		 * 	The parameters of this description.
		 */
		public Modification(String description, Object... parameters) {
			this.description = String.format(description, parameters);
		}
		
		/**
		 * This method applies the change of this {@link Modification} to the specified {@link Config} object.
		 * 
		 * @param config
		 * 	The {@link Config} object to apply the change to.
		 * 
		 * @throws ModificationException
		 * 	If the change conflicts with the current state of the specified {@link Config} object.
		 */
		public abstract void apply(Config config) throws ModificationException;
		
		@Override
		public String toString() {
			return description;
		}
		
	}
	
}
