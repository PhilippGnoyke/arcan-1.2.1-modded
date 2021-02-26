package it.unimib.disco.essere.main;

import java.io.File;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutputDirUtils {
	private static final Logger logger = LogManager.getLogger(ArcanOutputDirUtils.class);

	private String _outputfolder;
	public static final String OUTPUT_NAME = "output";
	public static final String OUTPUT_URL = File.separator + OUTPUT_NAME;

	public void createDir(final File projectFolder) {
		createDir(projectFolder, false);
	}

	public void createDir(final File projectFolder, final boolean singleFile) {
		createDir(projectFolder, OUTPUT_URL, singleFile);
	}

	public void createDir(final File projectFolder, final String output_url, final boolean singleFile) {
		String projectFolderString = projectFolder.getAbsolutePath();
		if (singleFile) {
			projectFolderString = projectFolder.getParentFile().getAbsolutePath();
		}
		if(".".equals(projectFolder.getName())){
			logger.debug("the project folder name is \".\" "+projectFolderString);
			if(output_url!=null && output_url.startsWith(File.separator)){
				projectFolderString = projectFolderString.substring(0, projectFolderString.length()-2);
			}else{
				projectFolderString = projectFolderString.substring(0, projectFolderString.length()-1);
			}
		}
		_outputfolder = projectFolderString + output_url;
		createOutputDir(_outputfolder);
	}

	public void createDirFullPath() {
		createDirFullPath(new File(OUTPUT_NAME));
	}

	public void createDirFullPath(final File outputFolder) {
		_outputfolder = outputFolder.getAbsolutePath();
		createOutputDir(_outputfolder);
	}

	public void createDirFullPath(final File outputFolder, final boolean singleFile) {
		if (singleFile) {
			_outputfolder = outputFolder.getAbsoluteFile().getParentFile().getAbsolutePath();
			createOutputDir(_outputfolder);
		} else {
			_outputfolder = outputFolder.getAbsolutePath();
			createOutputDir(_outputfolder);
		}
	}

	public static void createOutputDir(final String directoryName, final boolean singleFile) {
		if(singleFile){
			createOutputDirSingleFile(directoryName);
		}else{
			createOutputDir(directoryName);
		}
	}

	private static void createOutputDir(final String directoryName) {
		File theDir = new File(directoryName);
		createOutputDir(theDir);
	}

	private static void createOutputDirSingleFile(String directoryName) {
		File theDir = new File(directoryName);
		File f = theDir.getParentFile().getParentFile();
		logger.debug("parent directory of the jar: " + f + " name:" + theDir.getName());
		f = Paths.get(f.getAbsolutePath(), theDir.getName()).toFile();
		logger.debug("output arcan: " + f);
		createOutputDir(f);
	}

	private static void createOutputDir(File theDir) {
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			logger.debug("creating directory: " + theDir.getName());
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException e) {
				logger.error(e.getMessage());
			}
			if (result) {
				logger.debug("DIR created");
			}
		}
	}

	public File getFileInOutputFolder(final String fileName){
		File f = Paths.get(_outputfolder, fileName).toAbsolutePath().toFile();
		return f;
	}

	public static File getFileInOutputFolder(final File output, final String fileName){
		File f = Paths.get(output.getAbsolutePath(), fileName).toAbsolutePath().toFile();
		return f;
	}

	public File getOutputFolder(){
		return Paths.get(_outputfolder).toAbsolutePath().toFile();
	}
}