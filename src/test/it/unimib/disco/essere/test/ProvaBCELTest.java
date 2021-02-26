package it.unimib.disco.essere.test;

import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

public class ProvaBCELTest {
	private static final Logger logger = LogManager.getLogger(ProvaBCELTest.class);

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
		String p = 	Paths.get("C:", "Users", "ricca", "Downloads", "quartz-1.8.6-all-bin", "org").toString();
		String g = Paths.get("C:", "Users", "ricca", "Documents", "Neo4j", "default.graphdb").toString();
		SystemBuilder sys = new SystemBuilderByUrl();
		sys.readClass(p);
		GraphBuilder graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap());
		GraphWriter graphW = new Neo4JGraphWriter();
		graphW.setup(g);
		Graph graph = graphW.init();
		graphB.createGraph(graph);
		graphW.write(graph, false);
		new Object(){
			public void getName(){
				int i = 0;
				}   
			};
	}
	//
	//MATCH (r:class) WHERE r.ClassType = "SystemClass" RETURN r
	//MATCH (r:class) WITH r.name as name, count(*) as count WHERE count>=1 return name, count
}
