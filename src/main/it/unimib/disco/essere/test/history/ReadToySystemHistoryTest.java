package it.unimib.disco.essere.test.history;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import it.unimib.disco.essere.main.asengine.ImplicitCrossModuleDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphReader;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;

@RunWith(JUnit4.class)
public class ReadToySystemHistoryTest {
	private static final Logger logger = LogManager.getLogger(ReadToySystemHistoryTest.class);

	static Path _g = Paths.get("C:", "Users", "ricca", "Documents", "Neo4j", "default.graphdb");

	private static GraphReader _asd = null;


	@BeforeClass
	public static void readGraph(){
		_asd  = new GraphReader(_g);
	}

	@Test
	public void implcitDependencies() throws TypeVertexException{
		new ImplicitCrossModuleDependencyDetector(_asd.getGraph()).detect();
		logger.debug("finito");
	}
}
