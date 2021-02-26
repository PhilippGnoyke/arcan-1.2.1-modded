package it.unimib.disco.essere.test;

import java.nio.file.Paths;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class TestNeo4j {

	public static void main(String[] args) {
		Neo4jGraph graph = Neo4jGraph.open(Paths.get("C:","Users","ricca","Documents","Neo4j","default.graphdb").toString());
//      Node n = new Node();
//		Neo4jNode n = new Neo4jNodeImpl(null);
//		Neo4jVertex e = new Neo4jVertex(e, graph);
//		Vertex e = graph.addVertex("classe");
		//Vertex vertex = graph.addVertex(T.label, "class", "name", "ciao", "ClassType", "io.gru");
        
		
		
		graph.cypher("CREATE INDEX ON :person(name)");
		graph.tx().commit() ;
		Vertex vertex = graph.addVertex(T.label,"person","name","marko");
		Vertex vertex1 = graph.addVertex(T.label,"dog","name","puppy");
		graph.tx().commit() ;
		System.out.println(vertex+" ----- "+vertex.label()+" ----- "+vertex.keys()+" ----- "+vertex.id());
		System.out.println(vertex1+" ----- "+vertex1.label()+" ----- "+vertex1.keys()+" ----- "+vertex1.id());
		try {
			graph.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
