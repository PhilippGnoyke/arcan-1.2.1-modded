package it.unimib.disco.essere.main.terminal;

import java.io.File;

import com.beust.jcommander.Parameter;

public class ParameterInputProjectInputTerminal {
	@Parameter(names = { "-projectFolder", 	"-p" }, description = "The project folder (default is a folder of java classes)", converter = FileConverter.class, validateWith = ExistFile.class,descriptionKey="project")
	public File _projectFolder;

	@Parameter(names = { "-jar", "-JR", "-jr" }, description = "The project folder constains only one jar",descriptionKey="project")
	public boolean _jarMode = false;

	@Parameter(names = { "-class", "-CL", "-cl" }, description = "The project folder contains only classes",descriptionKey="project")
	public boolean _classMode = true;

	@Parameter(names = { "-folderOfJars", "-FJ", "-fj" }, description = "The project folder contains only jars",descriptionKey="project")
	public boolean _jarsFolderMode = false;
}
