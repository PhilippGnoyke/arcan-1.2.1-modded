package it.unimib.disco.essere.main.graphmanager;

import java.nio.file.Path;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class GraphReader {
	private static final Logger logger = LogManager.getLogger(GraphReader.class);
	static Graph _graph = null;
	private static BaseConfiguration _config = new BaseConfiguration();
	
	public GraphReader(final Path pathDB){
		logger.debug("iniziato");
		_config.setProperty("gremlin.neo4j.directory", pathDB.toString());
		_config.setProperty("gremlin.neo4j.multiProperties", true);
		_config.setProperty("gremlin.neo4j.metaProperties", true);

		logger.debug("configuration initialized");
		logger.debug("config: " + _config + ", pathBD: " + pathDB.toString());
		_graph = Neo4jGraph.open(_config);
	}
	

	
	public Graph getGraph(){
		return _graph;
	}
}
