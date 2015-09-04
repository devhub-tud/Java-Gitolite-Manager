package nl.tudelft.ewi.gitolite.keystore;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link KeyStore} implementation based on {@code Path}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class KeyStoreImpl implements KeyStore {

	public static final String PUB_FILE_EXT = ".pub";
	public static final String KEY_NAME_SEPARATOR = "@";

	private final Path folder;

	private final Multimap<String, KeyImpl> keyMultimap;

	@Inject
	public KeyStoreImpl(final @Named("gitolite.keydir.dir") File folder) {
		this.keyMultimap = TreeMultimap.create();
		this.folder = folder.toPath();
		scan();
	}

	public void scan() {
		keyMultimap.clear();
		filesAsStream(folder)
			.filter(path -> path.getFileName().toString().contains(PUB_FILE_EXT))
			.map(KeyImpl::new).forEach(key -> keyMultimap.put(key.getUser(), key));
	}

	@SneakyThrows
	protected static Stream<Path> filesAsStream(Path path) {
		DirectoryStream<Path> stream = Files.newDirectoryStream(path);
		return StreamSupport.stream(stream.spliterator(), false)
			.onClose(closeAndPropagate(stream));
	}

	protected static Runnable closeAndPropagate(Closeable closable) {
		return () -> {
			try {
				closable.close();
			}
			catch (IOException e) {
				Throwables.propagate(e);
			}
		};
	}

	@Override
	public KeyImpl getKey(String identifiable, String name) {
		return keyMultimap.get(identifiable).stream()
			.filter(key -> key.getName().equals(name))
			.findAny().get();
	}

	@Override
	public Collection<KeyImpl> getKeys(String identifiable) {
		return keyMultimap.get(identifiable);
	}

	@Override
	public KeyImpl put(String identifiable, String name, String contents) throws IOException {
		StringBuilder builder = new StringBuilder();
		builder.append(identifiable);
		if (!Strings.isNullOrEmpty(name)) {
			builder.append('@').append(name);
		}
		builder.append(PUB_FILE_EXT);


		Path path = folder.resolve(builder.toString());
		try(Writer writer = Files.newBufferedWriter(path)) {
			writer.write(contents);
		}

		KeyImpl key = new KeyImpl(identifiable, name, path);
		keyMultimap.put(identifiable, key);
		return key;
	}

	@Value
	@AllArgsConstructor
	protected class KeyImpl implements Key {

		private final String user;

		private final String name;

		private final Path path;

		public KeyImpl(Path path) {
			this.path = path;
			String fileName = path.getFileName().toString();
			fileName = fileName.substring(0, fileName.indexOf(PUB_FILE_EXT));
			String[] parts = fileName.split(KEY_NAME_SEPARATOR);
			this.user = parts[0];
			this.name = parts.length > 1 ? parts[1] : EMPTY_KEY_NAME;
		}

		@Override
		public String getContents() throws IOException {
			return com.google.common.io.Files.readFirstLine(path.toFile(), Charset.defaultCharset());
		}

		@Override
		public void delete() throws IOException {
			Files.delete(path);
			keyMultimap.remove(user, this);
		}

	}
}
