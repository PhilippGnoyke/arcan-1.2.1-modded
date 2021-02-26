package it.unimib.disco.essere.test;

import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.asengine.cycleutils.CDFilterUtils;
import it.unimib.disco.essere.main.asengine.cycleutils.CyclePrinter;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintShapes;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintToMatrix;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintToTable;
import it.unimib.disco.essere.main.asengine.filters.CDShapeFilter;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphReader;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.metricsengine.ClassMetricsCalculator;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

public class CycleTest {
    private static final Logger logger = LogManager.getLogger(CycleTest.class);
    private static SystemBuilder sys = null;
    private static GraphBuilder graphB = null;
    private static GraphWriter graphW = null;
    private static Graph graph = null;
    private static PackageMetricsCalculator packageCalc = null;
    private static ClassMetricsCalculator classCalc = null;
    private static UnstableDependencyDetector UDDetector = null;
    private static HubLikeDetector HLDetector = null;
    private static CyclicDependencyDetector CDDetector = null;

    @Before
    public void setupGraph() {
        sys = new SystemBuilderByUrl();
        // SystemBuilder sys = new
        // SystemBuilderByJar("C:/Users/Ilaria/Downloads/quartz-1.8.6/quartz-all-1.8.6.jar");
        sys.readClass(
                "D:/myprog/LabIngSoft/CyclesTesterSystem/bin");
        graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap()); 
        graphW = new Neo4JGraphWriter();
        graphW.setup("D:/myprog/LabIngSoft/MatrixDB");
        graph = graphW.init();
        graphB.createGraph(graph);
        graphW.write(graph, false);
        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HLDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile());
    }
    
    
    public void readGraph() {
        logger.info("***Start Graph Reading***");
        GraphReader reader = new GraphReader(Paths.get("D:/myprog/LabIngSoft/MatrixDB"));
        // GraphReader reader = new
        // GraphReader(Paths.get("C:/Users/ricca/Documents/Neo4j/default.graphdb"));
        graph = reader.getGraph();
        graphW = new Neo4JGraphWriter();
        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HLDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile());
        logger.debug("End setup");


    }
    
    
    public void checkDFSTest(){
        CDFilterUtils.cleanCDDetection(graph);
        CDDetector.detect();
        graphW.write(graph, false);
        CyclePrinter printer = new PrintToMatrix(GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS));
        CyclePrinter printer2 = new PrintToTable(GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS));
        printer.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(), GraphBuilder.CLASS);
        printer2.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(), GraphBuilder.CLASS);
        printer.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.CLASS));
        printer2.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.CLASS));
        printer.closePrint();
        printer2.closePrint();
        
    }
    
    @Test
    public void cyclicDependencyDetectorTest() {
        CDFilterUtils.cleanCDDetection(graph);
        CDDetector.detect();
        graphW.write(graph, false);
        CDShapeFilter filter = new CDShapeFilter(graph);
        filter.getCircleCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getCircleCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);
        filter.getCliqueCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getStarAndChainCycles(GraphBuilder.CLASS);
        graphW.write(graph, false);
    }
    
    @Test
    public void cyclicDependencyDetectorWithPrintTest() {
        CDFilterUtils.cleanCDShapeFilter(graph);
        CDFilterUtils.cleanCDDetection(graph);
        CDDetector.detect();
        graphW.write(graph, false);

        CDShapeFilter filter = new CDShapeFilter(graph);

        filter.getCircleCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getCircleCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);

        filter.getCliqueCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getCliqueCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);

        filter.getStarAndChainCycles(GraphBuilder.CLASS);
     
        filter.getStarAndChainCycles(GraphBuilder.PACKAGE);
        
        graphW.write(graph, false);

        List<Vertex> classes = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        List<Vertex> packages = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);

        CyclePrinter printer = new PrintToMatrix(classes);
        CyclePrinter printer2 = new PrintToTable(classes);
        CyclePrinter printerShape = new PrintShapes();

        printer.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(), GraphBuilder.CLASS);
        printer2.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(), GraphBuilder.CLASS);
        printerShape.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(), GraphBuilder.CLASS);

        printer.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.CLASS));
        printer2.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.CLASS));
        
        logger.debug("lista di shape: " + GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.CLASS));
        printerShape.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.CLASS));
        

        printer.closePrint();
        printer2.closePrint();
        printerShape.closePrint();

        CyclePrinter printer3 = new PrintToMatrix(packages);
        CyclePrinter printer4 = new PrintToTable(packages);
        CyclePrinter printerShape2 = new PrintShapes();

        printer3.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(), GraphBuilder.PACKAGE);
        printer4.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(), GraphBuilder.PACKAGE);
        printerShape2.initializePrint(Paths.get("C:/Users/ipiga/Desktop/MatrixCycles").toFile(),
                GraphBuilder.PACKAGE);

        printer3.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));
        printer4.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));
        printerShape2.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.PACKAGE));

        printer3.closePrint();
        printer4.closePrint();
        printerShape2.closePrint();
    }


    
    @After
    public void closeGraph() {
        try {
            graph.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
