package nl.minicom.gitolite.purger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class Purger {
	
	private static final Logger log = LoggerFactory.getLogger(Purger.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		File path = new File(args[0]);
		
		List<String> configRepos = parseConfig(path);
		List<String> presentRepos = parseDisk(path);
		
		log.info("Repos in config:");
		for (String configRepo : configRepos) {
			log.info(" - " + configRepo);
		}
		
		log.info("\nRepos on disk:");
		for (String presentRepo : presentRepos) {
			log.info(" - " + presentRepo);
		}
		
		log.info("\nPurging repositories:");
		for (String presentRepo : presentRepos) {
			if (presentRepo.equals("gitolite-admin.git")) {
				continue;
			}
			
			if (!configRepos.contains(presentRepo)) {
				log.info(" - " + presentRepo);
				purge(new File(path, presentRepo));
			}
		}
		
		log.info("");
		log.info("Completed disk cleaning");
	}

	private static void purge(File file) throws IOException {
		FileUtils.delete(file, FileUtils.RECURSIVE);
		
		File parent = file.getParentFile();
		while (parent != null && isEmptyDir(parent)) {
			FileUtils.delete(parent, FileUtils.RECURSIVE);
			parent = parent.getParentFile();
		}
	}

	private static boolean isEmptyDir(File file) {
		return file.list().length == 0;
	}

	private static List<String> parseDisk(File path) {
		List<String> paths = Lists.newArrayList(path.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory() && name.endsWith(".git");
			}
		}));
		
		String[] dirs = path.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return !name.endsWith(".git") && !name.startsWith(".") && new File(dir, name).isDirectory();
			}
		});
		
		for (String dir : dirs) {
			File subPath = new File(path, dir);
			List<String> subDirs = parseDisk(subPath);
			for (String subDir : subDirs) {
				paths.add(dir + "/" + subDir);
			}
		}
		
		return paths;
	}

	private static List<String> parseConfig(File path) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("git", "show", "HEAD:conf/gitolite.conf")
			.directory(new File(path, "gitolite-admin.git"))
			.redirectErrorStream(true)
			.start();
		
		List<String> repositories = Lists.newArrayList();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("repo ")) {
					String repoName = line.trim().substring(5);
					repositories.add(repoName + ".git");
				}
			}
		}
		
		if (process.waitFor() != 0) {
			return null;
		}
		
		return repositories;
	}
	
}
