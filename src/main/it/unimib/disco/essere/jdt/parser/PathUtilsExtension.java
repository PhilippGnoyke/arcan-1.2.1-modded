package it.unimib.disco.essere.jdt.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarException;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import it.unimib.disco.essere.analysis.file.util.DirUtils;

public final class PathUtilsExtension{
	private static final Logger logger = LogManager.getLogger(PathUtilsExtension.class);
	private PathUtilsExtension(){
		
	}

	/**
	 * Collect java files inside the given path, that are inside pathSrc
	 * 
	 * @param path
	 *            the root path where to start the search
	 * @param pathSrc
	 *            the path contain the src used in order to create compilation
	 *            units
	 * @param files
	 *            the list that will contain all the founded .java files
	 */
	public static List<Path> collectJavaFiles(final Path path,
			final List<Path> pathSrc) {
		final List<Path> files = new ArrayList<>();
		try {
			final Path startPath = Paths.get(path.toAbsolutePath().toString());
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(final Path dir,
						final BasicFileAttributes attrs) {
					if (!directoryContainedInList(dir, pathSrc)){
//						return FileVisitResult.CONTINUE;
						pathSrc.add(dir);
					}
					return FileVisitResult.CONTINUE;
////					return FileVisitResult.SKIP_SUBTREE;
				}
	
				@Override
				public FileVisitResult visitFile(final Path file,
						final BasicFileAttributes attrs) {
					if (file.toFile().isFile()
							&& file.toFile().getName().endsWith(".java"))
						if (listContainFile(file.toFile(), pathSrc)){
							files.add(file.toAbsolutePath());
							if (!directoryEqualsInList(file.getParent(), pathSrc)){
//								return FileVisitResult.CONTINUE;
								pathSrc.add(file.getParent());
							}
						}
					return FileVisitResult.CONTINUE;
				}
	
				@Override
				public FileVisitResult visitFileFailed(final Path file,
						final IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (final IOException e) {
			logger.info(e.getClass() + " ERROR: " + e.getMessage());
		}
		return ImmutableList.copyOf(files);
	}
	
	/**
	 * Collect java files inside the given path, that are inside pathSrc
	 * 
	 * @param path
	 *            the root path where to start the search
	 * @param pathSrc
	 *            the path contain the src used in order to create compilation
	 *            units
	 * @param files
	 *            the list that will contain all the founded .java files
	 */
	public static List<Path> collectFilesWithExtension(final Path path, final String endsWith) {
		final List<Path> files = new ArrayList<>();
		final List<Path> pathSrc = new ArrayList<>();
		try {
			final Path startPath = Paths.get(path.toAbsolutePath().toString());
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				int level = 0;
				@Override
				public FileVisitResult preVisitDirectory(final Path dir,
						final BasicFileAttributes attrs) {
					if (!directoryContainedInList(dir, pathSrc)){
						pathSrc.add(dir);
					}
					level++;
					return FileVisitResult.CONTINUE;
				}
	
				@Override
				public FileVisitResult visitFile(final Path file,
						final BasicFileAttributes attrs) {
					if (level<3
							&&file.toFile().isFile()
							&& file.toFile().getName().endsWith(endsWith))
						if (listContainFile(file.toFile(), pathSrc)){
							files.add(file.toAbsolutePath());
							if (!directoryEqualsInList(file.getParent(), pathSrc)){
								pathSrc.add(file.getParent());
							}
						}
					return FileVisitResult.CONTINUE;
				}
	
				@Override
				public FileVisitResult postVisitDirectory(final Path file,
						final IOException e){
					level--;
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(final Path file,
						final IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (final IOException e) {
			logger.info(e.getClass() + " ERROR: " + e.getMessage());
		}
		return ImmutableList.copyOf(files);
	}
	
	/**
	 * Collect java files inside the given path, that are inside pathSrc
	 * 
	 * @param path
	 *            the root path where to start the search
	 * @param pathSrc
	 *            the path contain the src used in order to create compilation
	 *            units
	 * @param files
	 *            the list that will contain all the founded .java files
	 */
	public static void deleteIfNotJavaOrJarOrClassFiles(final Path path) {
		final List<Path> pathSrc = new ArrayList<>();
		try {
			final Path startPath = Paths.get(path.toAbsolutePath().toString());
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(final Path dir,
						final BasicFileAttributes attrs) {
					if (!directoryContainedInList(dir, pathSrc)){
//						return FileVisitResult.CONTINUE;
						pathSrc.add(dir);
					}
					return FileVisitResult.CONTINUE;
////					return FileVisitResult.SKIP_SUBTREE;
				}
	
				@Override
				public FileVisitResult visitFile(final Path file,
						final BasicFileAttributes attrs) throws IOException {
					if (file.toFile().isFile()
							&& !(
									file.toFile().getName().endsWith(".java")
									||file.toFile().getName().endsWith(".jar")
									||
									file.toFile().getName().endsWith(".class")
									||file.toFile().getName().endsWith("javacommand.txt")
									||file.toFile().getName().endsWith("javaerror.txt")
									||file.toFile().getName().endsWith("javas.txt")
									||file.toFile().getName().endsWith("javas.txt")))
						if (listContainFile(file.toFile(), pathSrc)){
							file.toFile().delete();
							if (!directoryEqualsInList(file.getParent(), pathSrc)){
//								return FileVisitResult.CONTINUE;
								pathSrc.add(file.getParent());
							}
						}
					return FileVisitResult.CONTINUE;
				}
	
				@Override
				public FileVisitResult visitFileFailed(final Path file,
						final IOException e) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (final IOException e) {
			logger.info(e.getClass() + " ERROR: " + e.getMessage());
		}
	}
	
	/**
	 * Check if a list of directories contains given directory or file
	 * 
	 * @param f
	 *            the directory or file to search for
	 * @param pathSrc
	 *            the list where to search
	 * @return true, if successful
	 */
	static boolean listContainFile(final File f,
			final List<Path> pathSrc) {
	
		for (final Path ph : pathSrc)
			if (f.getAbsoluteFile().toString().indexOf(ph.toString()) != -1)
				return true;
	
		return false;
	}
	/**
	 * Check if a directory is contained in another directory
	 * 
	 * @param dir
	 *            the directory or file to search for
	 * @param pathSrc
	 *            the list where to search
	 * @return true, if successful
	 */
	static boolean directoryContainedInList(final Path dir,
			final Iterable<Path> pathSrc) {
		final String dirPath = dir.toString();
	
		for (final Path ph : pathSrc) {
			final String phStr = ph.toAbsolutePath().toString();
			if (phStr.contains(dirPath) || dirPath.contains(phStr))
				return true;
		}
	
		return false;
	}
	
	/**
	 * Check if a directory is contained in another directory
	 * 
	 * @param dir
	 *            the directory or file to search for
	 * @param pathSrc
	 *            the list where to search
	 * @return true, if successful
	 */
	static boolean directoryEqualsInList(final Path dir,
			final Iterable<Path> pathSrc) {
		final String dirPath = dir.toString();
	
		for (final Path ph : pathSrc) {
			final String phStr = ph.toAbsolutePath().toString();
			if (phStr.equals(dirPath) || dirPath.equals(phStr))
				return true;
		}
	
		return false;
	}
	
	/**
	 * Collect all jars.
	 * 
	 * TODO convert to the File visitor mode.
	 * 
	 * @param sDir
	 *            the Dir representing the root of the project
	 * @return the list of jar's paths
	 */
	public static List<Path> collectAllJars(final File sDir) {
	
		final File[] faFiles = sDir.listFiles();
		final List<Path> tmpPath = new ArrayList<Path>();
	
		for (final File file : faFiles) {
			if (file.isFile() && file.getName().endsWith("jar"))
				if (isValidJar(new File(file.getAbsolutePath())))
					tmpPath.add(file.toPath());
			if (file.isDirectory()) tmpPath.addAll(collectAllJars(file));
		}
	
		return tmpPath;
	}
	
	/**
	 * Checks if is valid jar.
	 * 
	 * @param file
	 *            the file
	 * @return true, if is valid jar
	 */
	static boolean isValidJar(final File file) {
		try (JarFile jarfile = new JarFile(file);) {
			return true;
		} catch (final JarException e) {
			return false;
		} catch (final IOException e) {
			return false;
		}
	}
	
}
