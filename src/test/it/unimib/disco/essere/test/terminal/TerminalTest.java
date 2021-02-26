package it.unimib.disco.essere.test.terminal;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.beust.jcommander.JCommander;

import it.unimib.disco.essere.main.TerminalExecutor;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
@RunWith(JUnit4.class)
/**
 * The normal behavior is to calculate and searching all the metrics and AS in one project without specify libs and class-path folder
 * @author RR
 *
 */
public class TerminalTest {
	private static final Logger logger = LogManager.getLogger(TerminalExecutor.class);
	static TerminalExecutor tt = null;
	@BeforeClass
	public static void beforeClass(){
		tt = new TerminalExecutor();
	}

	/*@Test
	public void runTerminalCommands(){
		String s = "\""+Paths.get("C:","Users","ricca","workspaceThinkerpop","ToySystem","target","classes","it","unimib","disco","essere","toysystem").toString()+"\"";
		String [] args = {"-p",s };
		logger.debug(args[0]+" "+args[1]);
		TerminalExecutor tt = new TerminalExecutor();
		new JCommander(tt, args);
		tt.run();

		int counterClass = 0;
		int counterPackage = 0;
		Iterator<Vertex> i = tt.graph().vertices();
		while (i.hasNext()) {
			Vertex v = i.next();

			if (v.label().equals("class")) {
				++counterClass;
			}

			if (v.label().equals("package")) {
				++counterPackage;
			}
		}

		int counterChildren = 0;
		int counterDependences = 0;
		int counterPackageDependences = 0;
		int counterAfferenceDependences = 0;
		int counterEfferenceDependences = 0;

		Iterator<Edge> iter = tt.getGraph().edges();
		while (iter.hasNext()) {
			Edge e = iter.next();
			String label = e.label();
			switch (label) {
			case "isChildOf":
				++counterChildren;
				break;
			case "dependsOn":
				++counterDependences;
				break;
			case "belongsTo":
				++counterPackageDependences;
				break;
			case "isAfferentOf":
				++counterAfferenceDependences;
				break;
			case "isEfferentOf":
				++counterEfferenceDependences;
				break;
			}
		}


		System.out.println("counterClass: " + counterClass);
		System.out.println("counterPackage: " + counterPackage);
		System.out.println("counterChildren: " + counterChildren);
		System.out.println("counterDependences: " + counterDependences);
		System.out.println("counterPackageDependences: " + counterPackageDependences);
		System.out.println("counterAfferenceDependences: " + counterAfferenceDependences);
		System.out.println("counterEfferenceDependences: " + counterEfferenceDependences);

		assertEquals(16, counterClass);
		assertEquals(5, counterPackage);
		assertEquals(5, counterChildren);
		assertEquals(28, counterDependences);
		assertEquals(16, counterPackageDependences);
		assertEquals(11, counterAfferenceDependences);
		assertEquals(11, counterEfferenceDependences);

		try {
			Map<String, List<String>> d = tt.getUnstableDependencyDetector().detect();
			for(String k : d.keySet()){
				for(String v :d.get(k)){
					logger.debug(k+" - "+v);
				}
			}
		} catch (TypeVertexException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void runTerminalCommandsTest(){
		String s = "\""+Paths.get("C:","Users","Ilaria","Desktop","myprog","LabIngSoft","ToySystemNeo4jDB").toString()+"\"";
		String [] args = {"-cycle","-d",s };
		logger.debug(args[0]+" "+args[1]);
		TerminalExecutor tt = new TerminalExecutor();
		new JCommander(tt, args);
		tt.run();


	}

	@Test
	public void errorRunTerminalCommandsTest(){
		String s = "\""+Paths.get("C:","Users","Ilaria","Desktop","myprog","LabIngSoft","ToySystemNeo4jDBError").toString()+"\"";
		String [] args = {"-cycle","-d",s };
		logger.debug(args[0]+" "+args[1]);
		TerminalExecutor tt = new TerminalExecutor();
		new JCommander(tt, args);
		tt.run();


	}

	@AfterClass
	public static void closeGraph() {
		tt.closeGraph();
	}
*/
}
