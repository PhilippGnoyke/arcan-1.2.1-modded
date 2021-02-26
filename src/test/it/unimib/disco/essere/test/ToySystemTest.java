package it.unimib.disco.essere.test;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.ClassMetricsCalculator;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

@RunWith(JUnit4.class)
public class ToySystemTest {
    private static final Logger logger = LogManager.getLogger(ToySystemTest.class);
    private static SystemBuilder sys = null;
    private static GraphBuilder graphB = null;
    private static GraphWriter graphW = null;
    private static Graph graph = null;
    private static PackageMetricsCalculator calc = null;
    private static ClassMetricsCalculator classCalc = null;
    private static UnstableDependencyDetector UDDetector = null;
    private static HubLikeDetector HLDetector = null;
    private static CyclicDependencyDetector CDDetector = null;

    @BeforeClass
    public static void setupGraph() {
        sys = new SystemBuilderByUrl();
        // SystemBuilder sys = new
        // SystemBuilderByJar("C:/Users/Ilaria/Downloads/quartz-1.8.6/quartz-all-1.8.6.jar");
        sys.readClass(
                "C:/Users/Ilaria/Desktop/myprog/LabIngSoft/ToySystem/target/classes/it/unimib/disco/essere/toysystem");
        graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap()); 
        graphW = new Neo4JGraphWriter();
        graphW.setup("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/ToySystemNeo4jDB");
        graph = graphW.init();
        graphB.createGraph(graph);
        graphW.write(graph, false);
        calc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, calc);
        HLDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("C:/Users/Ilaria/Desktop/ToySystemCycles").toFile());
    }
    
    @Test
    public void runTest() {
        int counterClass = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS).size();
        int counterPackage = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE).size();

        int counterChildren = GraphUtils.findEdgesByLabel(graph, PropertyEdge.LABEL_SUPER_DEPENDENCY.toString()).size();
        int counterDependences = GraphUtils.findEdgesByLabel(graph, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString()).size();
        int counterPackageDependences = GraphUtils.findEdgesByLabel(graph, PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString()).size();
        int counterAfferenceDependences = GraphUtils.findEdgesByLabel(graph, GraphBuilder.LABEL_AFFERENCE).size();
        int counterEfferenceDependences = GraphUtils.findEdgesByLabel(graph, PropertyEdge.LABEL_EFFERENCE.toString()).size();
        int counterImplementationDependences =  GraphUtils.findEdgesByLabel(graph, PropertyEdge.LABEL_INTERFACE_DEPENDENCY.toString()).size();

        assertEquals(20, counterClass);
        assertEquals(6, counterPackage);
        assertEquals(5, counterChildren);
        assertEquals(23, counterDependences);
        assertEquals(20, counterPackageDependences);
        assertEquals(12, counterAfferenceDependences);
        assertEquals(12, counterEfferenceDependences);
        assertEquals(2, counterImplementationDependences);
        
        
    }
    
    
    
    @Test
    public void unstableDependencyDetectorTest(){
        try {
            UDDetector.detect();
        } catch (TypeVertexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void unstableDependencyDetectorBySmellVertexTest(){
        try {
            UDDetector.newDetect();
        } catch (TypeVertexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void HubLikeDetectorTest(){
        try {
            Map<String, List<Integer>> smells = HLDetector.detect();
            for(String s : smells.keySet()){
                logger.debug("Class affected by Hub-Like AS: " + s);
            }
            
        } catch (TypeVertexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void findNodeTest(){
        GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.olympusextension.Zeus",GraphBuilder.CLASS);
        GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.olympusextension.Ares",GraphBuilder.CLASS);
        
    }
    
    @Test
    public void calculateCBOTest() {
        Vertex classVertexClass1 = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.origin.Class1",GraphBuilder.CLASS);
        Vertex classVertexZeus = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.olympusextension.Zeus",GraphBuilder.CLASS);
        try {
            int cbo = classCalc.calculateCBO(classVertexClass1);
            int cboZeus = classCalc.calculateCBO(classVertexZeus);

            assertEquals(2.0, cbo, 0.0);
            logger.debug("CBO di Class1: " + cbo);
            assertEquals(3.0, cboZeus, 0.0);
            logger.debug("CBO di Zeus: " + cboZeus);
        } catch (TypeVertexException e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void calculateDistanceFromTheMainSequenceTest() {
        Vertex packageVertex = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.olympusextension",GraphBuilder.PACKAGE);
        Vertex packageVertex2 = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.origin",GraphBuilder.PACKAGE);
        try {
            double distance = calc.calculateDistanceFromTheMainSequence(packageVertex);
            double distance2 = calc.calculateDistanceFromTheMainSequence(packageVertex2);

            assertEquals(0.4, distance, 0.1);
            logger.debug("distanza: " + distance);
            assertEquals(0.25, distance2, 0.0);
            logger.debug("distanza2: " + distance2);
        } catch (TypeVertexException e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void calculateInstabilityTest() {
        Vertex packageVertex = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.olympusextension",GraphBuilder.PACKAGE);
        Vertex packageVertex2 = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.origin",GraphBuilder.PACKAGE);
        try {
            double instability = calc.calculateInstability(packageVertex);
            double instability2 = calc.calculateInstability(packageVertex2);

            assertEquals(1.0, instability, 0.0);
            logger.debug("instability: " + instability);
            assertEquals(0.75, instability2, 0.0);
            logger.debug("instability2: " + instability2);
        } catch (TypeVertexException e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void calculateAbstractnessTest() {
        Vertex packageVertex = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.olympusextension",GraphBuilder.PACKAGE);
        Vertex packageVertex2 = GraphUtils.findVertex(graph, "it.unimib.disco.essere.toysystem.origin",GraphBuilder.PACKAGE);

        try {
            double abstractness = calc.calculateAbstractness(packageVertex);
            double abstractness2 = calc.calculateAbstractness(packageVertex2);

            assertEquals(0.4, abstractness, 0.1);
            logger.debug("astrattezza: " + abstractness);
            assertEquals(0.0, abstractness2, 0.0);
            logger.debug("astrattezza2: " + abstractness2);
        } catch (TypeVertexException e) {
            logger.error(e.getMessage());
        }

    }

    @Test
    public void calculateLCOMTest() {
        classCalc.calculateLCOM(sys.getClasses().get(4));
    }
    
    @Test
    public void calculateCyclesTest(){
        
        CDDetector.detect();
    }

    @AfterClass
    public static void closeGraph() {
        try {
            graph.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
