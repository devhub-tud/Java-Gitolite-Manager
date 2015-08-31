package nl.tudelft.ewi.gitolite.config.repositories;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * {@link RepositoriesManager} implementation based on {@code Path}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class PathRepositoriesManager implements RepositoriesManager {

	/**
	 * Folder that contains the repositories.
	 */
	private final Path root;

	/**
	 * Map that contains the repositories.
	 */
	private final Map<URI, PathRepositoryImpl> repositories;

	/**
	 * {@link RepositoriesManager} implementation based on {@code Path}.
	 * @param root Folder that contains the repositories.
	 */
	public PathRepositoriesManager(final File root) {
		this.root = root.toPath();
		this.repositories = Maps.newHashMap();
		scan();
	}

	/**
	 * Scan for repositories.
	 */
	public void scan() {
		repositories.clear();
		directoriesAsStream(root).forEach(path -> {
			PathRepositoryImpl pathRepository = new PathRepositoryImpl(path);
			repositories.put(pathRepository.getURI(), pathRepository);
		});
	}

	@Override
	public Collection<PathRepositoryImpl> getRepositories() {
		return repositories.values();
	}

	@Override
	public PathRepositoryImpl getRepository(URI uri) throws RepositoryNotFoundException {
		PathRepositoryImpl pathRepository = repositories.get(uri);
		if(pathRepository == null) {
			throw new RepositoryNotFoundException();
		}
		return pathRepository;
	}

	@SneakyThrows
	protected static Stream<Path> directoriesAsStream(Path path) {
		DirectoryStream<Path> stream = Files.newDirectoryStream(path);
		return StreamSupport.stream(stream.spliterator(), false)
			.filter(Files::isDirectory)
			.flatMap(a -> a.toString().contains(".git") ? Stream.of(a) : directoriesAsStream(a))
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


	/**
	 * Implementation for {@link Repository} based on {@link File}.
	 *
	 * @author Jan-Willem Gmelig Meyling
	 */
	@Value
	public class PathRepositoryImpl implements Repository {

		/**
		 * The repository folder, use for repository folder interaction, such as {@link Repository#delete()}.
		 */
		private final Path path;

		@Override
		public URI getURI() {
			URI rootURI = root.toUri();
			URI repositoryURI = path.toUri();
			return rootURI.relativize(repositoryURI);
		}

		@Override
		public void delete() throws IOException {
			Files.delete(path);
		}

	}

}
