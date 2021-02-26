package it.unimib.disco.essere.test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
import it.unimib.disco.essere.main.asengine.filters.UDRateFilter;
import it.unimib.disco.essere.main.asengine.udutils.UDPrinter;
import it.unimib.disco.essere.main.asengine.udutils.UDUtils;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphReader;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.ClassMetricsCalculator;
import it.unimib.disco.essere.main.metricsengine.MetricsUploader;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;
import it.unimib.disco.essere.utils.XMLReader;

public class AltenTest {
    private static final Logger logger = LogManager.getLogger(AltenTest.class);
    private static XMLReader reader = new XMLReader();
    private static SystemBuilder sys = null;
    private static GraphBuilder graphB = null;
    private static GraphWriter graphW = null;
    private static Graph graph = null;
    private static PackageMetricsCalculator packageCalc = null;
    private static ClassMetricsCalculator classCalc = null;
    private static UnstableDependencyDetector UDDetector = null;
    private static HubLikeDetector HBDetector = null;
    private static CyclicDependencyDetector CDDetector = null;
    // private static String file = "C:/Users/Ilaria/Desktop/checkFiles/Analyzed
    // Projects/Apache-Maven results/";
    private static String file = "C:/Users/Ilaria/Desktop/checkFiles/Analyzed Projects/Struts results/";

    @BeforeClass
    public static void setupGraph() {
        // sys = new SystemBuilderByUrl(
        // "C:/gittest/ant-1.8.2/bin");

        sys = new SystemBuilderByUrl();
        // sys.readClass("C:/Users/Ilaria/Downloads/qualitasCorpus/quartz-1.8.6/quartz-all-1.8.6.jar");
        sys.readClass("C:/Users/ipiga/Desktop/compilati");
        // sys.readClass("C:/Users/Ilaria/Downloads/ProjectsToAnalyze/apache-maven-3.0.5/lib");
        // sys.readClass("C:/User/Ilaria/Downloads/ProjectsToAnalyze/struts-2.2.1/bin");

        graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap());

        graphW = new Neo4JGraphWriter();
        graphW.setup("D:/myprog/LabIngSoft/Alten_DB");
        // graphW.setup("C:/Users/ricca/Documents/Neo4j/default.graphdb");
        graph = graphW.init();
        logger.debug("Graph initializated");

        graphB.createGraph(graph);
        logger.debug("End of graph building");

        graphW.write(graph, false);
        logger.debug("Graph written into DB");

        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HBDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile());
        logger.debug("End setup");

        /*
         * OutputStream out; try { out = new
         * FileOutputStream("Hadoop-1.1.2.graphml");
         * graph.io(IoCore.graphml()).writer().normalize(true).create().
         * writeGraph(out, graph); } catch (IOException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); }
         */

    }

    public void readGraph() {
        logger.info("***Start Graph Reading***");
        GraphReader reader = new GraphReader(Paths.get("D:/myprog/LabIngSoft/Junit_2.0_DB"));
        // GraphReader reader = new
        // GraphReader(Paths.get("C:/Users/ricca/Documents/Neo4j/default.graphdb"));
        graph = reader.getGraph();
        graphW = new Neo4JGraphWriter();
        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HBDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile());
        logger.debug("End setup");

    }
    @Test
    public void instabilityUpdateTest() {
        MetricsUploader m = new MetricsUploader(graph);
        try {
            m.updateInstability();
            graphW.write(graph, false);
        } catch (TypeVertexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void unstableDependencyDetectorBySmellVertexTest() {
        try {
            MetricsUploader m = new MetricsUploader(graph);
            m.updateInstability();
            graphW.write(graph, false);

            UDUtils.cleanUDDetection(graph);
            UDDetector.newDetect();
            graphW.write(graph, false);

            Map<String, List<String>> results = UDDetector.getSmellMap();

            UDPrinter p = new UDPrinter(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(), UDDetector);

            p.print(results);

            p.closeAll();

            UDRateFilter filter = new UDRateFilter(graph);

            logger.debug("***filtering***");
            UDPrinter p2 = new UDPrinter(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(),
                    UDDetector);
            p2.print(filter.filter(0));
            p2.closeAll();

        } catch (IOException | TypeVertexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void cyclicDependencyDetectorTest() {
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

        printer.initializePrint(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(), GraphBuilder.CLASS);
        printer2.initializePrint(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(), GraphBuilder.CLASS);
        printerShape.initializePrint(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(), GraphBuilder.CLASS);

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

        printer3.initializePrint(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(), GraphBuilder.PACKAGE);
        printer4.initializePrint(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(), GraphBuilder.PACKAGE);
        printerShape2.initializePrint(Paths.get("C:/Users/ipiga/Desktop/AltenTest").toFile(),
                GraphBuilder.PACKAGE);

        printer3.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));
        printer4.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));
        printerShape2.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.PACKAGE));

        printer3.closePrint();
        printer4.closePrint();
        printerShape2.closePrint();
    }

    @Test
    public void hubLikeDetectorTest() {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Class", "FanIn", "FanOut", "Total Dependences");

        FileWriter writer;

        try {
            writer = new FileWriter(
                    Paths.get("C:/Users/ipiga/Desktop/AltenTest" + "HubLikeClasses.csv").toFile());
            CSVPrinter printer = new CSVPrinter(writer, formatter);

            Map<String, List<Integer>> hubLikeClasses = HBDetector.detect();

            for (Entry<String, List<Integer>> e : hubLikeClasses.entrySet()) {
                printer.print(e.getKey());
                printer.print(e.getValue().get(1));
                printer.print(e.getValue().get(2));
                printer.print(e.getValue().get(0));
                printer.println();
            }
            printer.close();
            writer.close();

        } catch (IOException | TypeVertexException e) {
            logger.debug(e.getMessage());
        }

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
