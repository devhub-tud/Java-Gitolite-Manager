package nl.minicom.gitolite.manager.models;

import java.util.Comparator;

public interface Identifiable {
	
	static final Comparator<Identifiable> SORT_BY_TYPE_AND_ALPHABETICALLY = new Comparator<Identifiable>() {
		@Override
		public int compare(Identifiable o1, Identifiable o2) {
			boolean o1IsGroup = o1 instanceof Group;
			boolean o2IsGroup = o2 instanceof Group;
			
			if (o1IsGroup == o2IsGroup) {
				return o1.getName().compareTo(o2.getName());
			}
			else if (o1IsGroup) {
				return -1;
			}
			return 1;
		}
	};

	String getName();
	
}
