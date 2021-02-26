package it.unimib.disco.essere.main.graphmanager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;

import it.unimib.disco.essere.analysis.file.util.DirUtils;

public class Neo4JGraphWriter implements GraphWriter {
    private static final Logger logger = LogManager.getLogger(Neo4JGraphWriter.class);
    final private BaseConfiguration config;
    
    //"C:", "Users", "Ilaria", "Desktop", "myprog", "LabIngSoft", "neo4jDB"
    
    public Neo4JGraphWriter(BaseConfiguration config){
        this.config = config;
    }
    
    public Neo4JGraphWriter(){
        config = new BaseConfiguration();
    }

    @Override
    public void setup(String url) {
        config.setProperty("gremlin.neo4j.directory", url);
        config.setProperty("gremlin.neo4j.multiProperties", true);
        config.setProperty("gremlin.neo4j.metaProperties", true);
        Path path = Paths.get(url);
        
        if(Files.exists(path)){
        try {
            DirUtils.clean(path);
        } catch (IOException e2) {
            logger.error(e2.getMessage());
        }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            logger.error(e1.getMessage());
        }

    }

    @Override
    public Graph init() {
        Neo4jGraph graph = Neo4jGraph.open(config);
        return graph;
    }

    @Override
    public void write(Graph graph, boolean closeAfterWrite) {
        
        OutputStream out;
        
        try {
            out = new FileOutputStream("ToySystem-graph.graphml");
            graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(out, graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            graph.tx().commit();
            if(closeAfterWrite){
                graph.close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            graph.tx().close();
            }
    }

}
