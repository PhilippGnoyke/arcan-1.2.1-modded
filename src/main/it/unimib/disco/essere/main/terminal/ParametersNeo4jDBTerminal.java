package it.unimib.disco.essere.main.terminal;

import java.io.File;
import java.nio.file.Paths;

import com.beust.jcommander.Parameter;


public class ParametersNeo4jDBTerminal {
	@Parameter(names = { "-neo4j" }, description = "if set write the neo4j database",descriptionKey="neo4j")
	public boolean _writeNeo4j = false;
	
	@Parameter(names = { "-neo4jDBFolder","-d" }, description = "Database folder (default here_path\\Neo4j\\default.graphdb)", converter = FileConverter.class, validateWith = ExistFile.class,descriptionKey="neo4j")
	public File _dbFolder = Paths.get("Neo4j", "default.graphdb").toAbsolutePath().toFile();

	private static ParametersNeo4jDBTerminal _parNeo4j;
}
