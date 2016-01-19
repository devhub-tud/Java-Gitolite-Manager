package nl.tudelft.ewi.gitolite.keystore;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
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

	public KeyStoreImpl(final File folder) {
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
	public KeyImpl put(Key draft) throws IOException {
		validate(draft);
		StringBuilder builder = new StringBuilder();
		builder.append(draft.getUser());
		if (!Strings.isNullOrEmpty(draft.getName())) {
			builder.append('@').append(draft.getName());
		}
		builder.append(PUB_FILE_EXT);


		Path path = folder.resolve(builder.toString());
		try(Writer writer = Files.newBufferedWriter(path)) {
			writer.write(draft.getContents());
		}

		KeyImpl key = new KeyImpl(path);
		keyMultimap.put(draft.getUser(), key);
		return key;
	}

	protected void validate(Key key) throws IOException {
		Preconditions.checkNotNull(key.getName());
		String content = Preconditions.checkNotNull(key.getContents());
		try {
			String[] parts = content.split("[\\r\\n\\s]+");
			String keyPart = parts[1];
			final byte[] bin = Base64.decodeBase64(keyPart);
			new ByteArrayBuffer(bin).getRawPublicKey();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Validation failed for key \"" + content + "\"", e);
		}
	}

	@Override
	public Collection<String> getUsers() {
		return Collections.unmodifiableCollection(keyMultimap.keys());
	}

	@Data
	@RequiredArgsConstructor
	protected class KeyImpl implements PersistedKey {

		public static final String PUB_FILE_EXT = ".pub";

		public static final String KEY_NAME_SEPARATOR = "@";

		private final Path path;

		@Override
		public String getUser() {
			String[] parts = getFileNameParts();
			return parts[0];
		}

		protected String[] getFileNameParts() {
			String fileName = path.getFileName().toString();
			fileName = fileName.substring(0, fileName.indexOf(PUB_FILE_EXT));
			return fileName.split(KEY_NAME_SEPARATOR);
		}

		@Override
		public String getName() {
			String[] parts = getFileNameParts();
			return parts.length > 1 ? parts[1] : EMPTY_KEY_NAME;
		}

		@Override
		public String getContents() throws IOException {
			return com.google.common.io.Files.readFirstLine(path.toFile(), Charset.defaultCharset());
		}

		@Override
		public void delete() throws IOException {
			Files.delete(getPath());
			keyMultimap.remove(getUser(), this);
		}

	}
}
