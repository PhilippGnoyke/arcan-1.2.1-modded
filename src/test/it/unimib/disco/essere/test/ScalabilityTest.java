package it.unimib.disco.essere.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.analysis.file.util.DirUtils;

public class ScalabilityTest {
    public static void main(String[] args) {
        Path path = Paths.get("C:", "Users", "Ilaria", "Desktop", "myprog", "LabIngSoft", "neo4jTestDB");
        BaseConfiguration conf = new BaseConfiguration();
        conf.setProperty("gremlin.neo4j.directory", path.toString());
        conf.setProperty("gremlin.neo4j.multiProperties", true);
        conf.setProperty("gremlin.neo4j.metaProperties", true);

        try {
            DirUtils.cleanIfExists(path);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        Neo4jGraph graph = Neo4jGraph.open(conf);
        Vertex lastVertex = null;
        for(int i = 0; i < 1000; ++i){
            Vertex vertex = graph.addVertex(T.label, "class", "name", "class " + i, "ClassType",
                    "standard");
            if(lastVertex != null){
                vertex.addEdge("dependsOn",lastVertex);
            }
            lastVertex = vertex;
        }
        
        graph.tx().commit();
        try {
            graph.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
