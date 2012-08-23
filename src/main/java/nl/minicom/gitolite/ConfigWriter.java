package nl.minicom.gitolite;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;

public class ConfigWriter {
	
	private static final int PADDING = 20;

	public void write(Config config, Writer writer) throws IOException {
		Collection<Group> groups = config.getGroups();
		if (!groups.isEmpty()) {
			for (Group group : groups) {
				writer.write(pad(group.getName(), PADDING) + " = " + Joiner.on(" ").join(group.getEntityNamesInGroup()) + "\n");
			}
			writer.write("\n");
		}
		
		Collection<Repository> repos = config.getRepositories();
		for (Repository repo : repos) {
			writer.write("repo " + repo.getName() + "\n");
			Multimap<Permission, Identifiable> permissions = repo.getPermissions();
			for (Permission right : permissions.keySet()) {
				Collection<String> names = Collections2.transform(permissions.get(right), new Function<Identifiable, String>() {
					@Override
					public String apply(Identifiable entity) {
						return entity.getName();
					}
				});
				
				writer.write("    " + pad(right.getName(), PADDING - 4) + " = ");
				writer.write(Joiner.on(" ").join(names) + "\n");
			}
			writer.write("\n");
		}
		
		writer.flush();
		writer.close();
	}
	
	private String pad(String input, int limit) {
		String result = input;
		while (result.length() < limit) {
			result += " ";
		}
		return result;
	}
	
}
