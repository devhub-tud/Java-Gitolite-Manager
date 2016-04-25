package nl.tudelft.ewi.gitolite.repositories;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@link RepositoriesManager} implementation based on {@code Path}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@EqualsAndHashCode
public class PathRepositoriesManager implements RepositoriesManager {

	public static final int DEFAULT_BLOCK_SIZE = 4096;

	/**
	 * Repository map.
	 */
	private final Map<URI, PathRepositoryImpl> repositoryMap;

	/**
	 * Folder that contains the repositories.
	 */
	private final Path root;

	/**
	 * {@link RepositoriesManager} implementation based on {@code Path}.
	 * @param root Folder that contains the repositories.
	 */
	@SneakyThrows
	public PathRepositoriesManager(final File root) {
		this.root = root.toPath();
		this.repositoryMap = Maps.newHashMap();
		this.reload();
	}

	@Override
	public Collection<PathRepositoryImpl> getRepositories() {
		synchronized (this.repositoryMap) {
			return ImmutableList.copyOf(repositoryMap.values());
		}
	}

	@Override
	public PathRepositoryImpl getRepository(URI uri) throws RepositoryNotFoundException {
		synchronized (this.repositoryMap) {
			return Optional.ofNullable(this.repositoryMap.get(uri))
				.orElseThrow(RepositoryNotFoundException::new);
		}
	}

	@SneakyThrows
	protected static Stream<Path> directoriesAsStream(Path path) {
		return Files.walk(path, 4)
			.filter(Files::isDirectory)
			.filter(directory -> directory.getFileName().toString().endsWith(".git"));
	}

	@Override
	public void reload() throws IOException {
		synchronized (this.repositoryMap) {
			this.repositoryMap.clear();
			directoriesAsStream(root)
				.map(PathRepositoryImpl::new)
				.forEach(repo -> this.repositoryMap.put(repo.getURI(), repo));
		}
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
			synchronized (PathRepositoriesManager.this.repositoryMap) {
				PathRepositoriesManager.this.repositoryMap.remove(getURI());
			}
			FileUtils.deleteDirectory(path.toFile());
		}

		@Override
		public FileSize getSize() throws IOException {
			return new FileSize(Files.walk(path)
				.filter(p -> p.toFile().isFile())
				.mapToLong(PathRepositoriesManager::fileSize)
				.sum());
		}

	}

	@SneakyThrows
	public static long fileSize(Path path) {
		long size = Files.size(path);
		// Git objects are very small, but have to be stored in the smallest available block,
		// so there is quite some overhead here.
		return (size < DEFAULT_BLOCK_SIZE) ? DEFAULT_BLOCK_SIZE : size;
	}

}
