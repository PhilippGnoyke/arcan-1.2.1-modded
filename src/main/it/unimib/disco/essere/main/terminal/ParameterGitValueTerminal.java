package it.unimib.disco.essere.main.terminal;

import java.io.File;
import java.nio.file.Paths;

import com.beust.jcommander.Parameter;

public class ParameterGitValueTerminal {
	@Parameter(names = { "-dbGitFolder", "-dgit" }, hidden = true, description = "Database git folder (default here_path\\Neo4j-commit\\default.graphdb)", converter = FileConverter.class, validateWith = ExistFile.class)
	public File _dbGitFolder = Paths.get("Neo4j-commit", "default.graphdb").toAbsolutePath().toFile();

	@Parameter(names = { "-gitHistory", "-gH", "-gh" }, hidden = true, description = "Enable History evaluation of git repository and detection of implicit cross package dependency")
	public boolean _gitHistory;

	@Parameter(names = "-printGitCommit", hidden = true, description = "Enable printing of git history")
	public boolean _printGitCommit;

	@Parameter(names = { "-gitFolder", "-g" }, hidden = true, description = "Git folder (.git folder of git cache files)", converter = FileConverter.class, validateWith = ExistFile.class)
	// File _gitFolder = Paths.get("C:", "gittest", "quartz", ".git").toFile();
	public File _gitFolder = null;

	@Parameter(names = { "-compileGitNoMvn", "-cgnm" }, hidden = true, description = "Compile every commit of a project.")
	public boolean _compileGitNoMvn = false;
}
