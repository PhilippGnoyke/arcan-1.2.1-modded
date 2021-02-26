package it.unimib.disco.essere.test.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

public class ProvaBCEL {
    private static final Logger logger = LogManager.getLogger(ProvaBCEL.class);

    public static void main(String args[]) {

        /*
         * Path path = Paths.get("C:", "Users", "Ilaria", "Desktop", "myprog",
         * "LabIngSoft", "ToySystem", "target", "classes", "it", "unimib",
         * "disco", "essere", "toysystem"); Path libpath = Paths.get("C:",
         * "Users", "Ilaria", "Desktop", "myprog", "LabIngSoft", "ToySystem",
         * "librerie"); Path javalibpath = Paths.get("C:", "Users", "Ilaria",
         * "Desktop", "myprog", "LabIngSoft", "ToySystem", "javalibs");
         * 
         * setup(path, libpath, javalibpath);
         */

        SystemBuilder sys = new
        SystemBuilderByUrl();
        //SystemBuilder sys = new SystemBuilderByJar("C:/Users/Ilaria/Downloads/quartz-1.8.6/quartz-all-1.8.6.jar");
        sys.readClass("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/ToySystem/target/classes/it/unimib/disco/essere/toysystem");
        GraphBuilder graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap());
        GraphWriter graphW = new Neo4JGraphWriter();
        graphW.setup("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/neo4jDB");
        Graph graph = graphW.init();
        graphB.createGraph(graph);
        graphW.write(graph, true);

    }
  //find . -name "_.*"
    //MATCH (r:class) WHERE r.ClassType = "SystemClass" RETURN r
    //MATCH (r:class) WITH r.name as name, count(*) as count WHERE count>=1 return name, count
}
