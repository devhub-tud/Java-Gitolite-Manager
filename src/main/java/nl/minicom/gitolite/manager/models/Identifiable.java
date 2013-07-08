package nl.minicom.gitolite.manager.models;

import java.util.Comparator;

/**
 * This interface represents either a gitolite {@link User} or {@link InternalGroup}.
 * 
 * @author Michael de Jong <michaelj@minicom.nl>
 */
public interface Identifiable {
	
	Comparator<Identifiable> SORT_BY_TYPE_AND_NAME = new Comparator<Identifiable>() {
		@Override
		public int compare(Identifiable o1, Identifiable o2) {
			boolean o1IsGroup = o1 instanceof InternalGroup;
			boolean o2IsGroup = o2 instanceof InternalGroup;
			
			if (o1IsGroup == o2IsGroup) {
				return o1.getName().compareTo(o2.getName());
			}
			else if (o1IsGroup) {
				return -1;
			}
			return 1;
		}
	};

	/**
	 * @return
	 * 	The name of the {@link Identifiable}.
	 */
	String getName();
	
}
