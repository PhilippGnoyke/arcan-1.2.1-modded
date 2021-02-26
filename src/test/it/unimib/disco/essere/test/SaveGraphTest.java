package it.unimib.disco.essere.test;

import java.io.File;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;

import it.unimib.disco.essere.main.TerminalExecutor;
import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.asengine.cycleutils.CDFilterUtils;
import it.unimib.disco.essere.main.asengine.cycleutils.CyclePrinter;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintShapesDocker;
import it.unimib.disco.essere.main.asengine.filters.CDShapeFilter;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.metricsengine.ClassMetricsCalculator;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByFolderOfJars;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByJar;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

public class SaveGraphTest {
    private static final String ARCAN_OUTPUT_URL = "/ArcanOutput";
    private static final String FILTERED_URL = "/filtered";
    private static final Logger logger = LogManager.getLogger(SaveGraphTest.class);
    private Graph _graphTinkerpop = null;
    private Graph _graphNeo4j = null;
    SystemBuilder _sys = null;
    private PackageMetricsCalculator _metricsCalculator = null;
    private ClassMetricsCalculator _classMetricsCalculator = null;
    private UnstableDependencyDetector _unstableDependencyDetector = null;
    private CyclicDependencyDetector _cycleDetector = null;
    private HubLikeDetector _hubLikeDetector = null;
    private String _arcanSubfolder = null;
    private File projectFolder = Paths
            .get("C:/Users/ipiga/Desktop/problemi Arcan/apache-tomcat-6.0.0-con-neo4j/apache-tomcat-6.0.0-con-neo4j/lib")
            .toFile();
    private File dbFolder = Paths.get("C:/Users/ipiga/Desktop/problemi Arcan/problemTomcatDB").toFile();
    private String _nameFile = "CDpackage.csv";
    private File outputFilePath = Paths.get("C:/Users/ipiga/Desktop/problemi Arcan/").toFile();

    @Test
    public void buildGraphTinkerpop() {
        logger.info("***Start graph building***");
        _sys = null;
        GraphBuilder graphB = null;

        _sys = new SystemBuilderByFolderOfJars();

        logger.info("***Start graph building***");
        _sys.readClass(projectFolder.toString());
        graphB = new GraphBuilder(_sys.getClassesHashMap(), _sys.getPackagesHashMap());
        logger.info("***Start Opening Tinkerpop***");
        Graph graph = TinkerGraph.open();
        logger.info("***Graph initializated***");
        logger.info("***Graph initializated*** - graph:   " + graph);
        logger.info("***Graph initializated*** - builder: " + graphB);
        graphB.createGraph(graph);
        logger.info("***Graph created from compiled file***");
        logger.info("***Graph created from compiled file*** - graph:" + graph);
        logger.info("***End of graph building***");
        _graphTinkerpop = graph;
    }

    @Test
    public void buildGraphNeo4j() {
        logger.info("***Start graph building***");

        GraphBuilder graphB = null;
        GraphWriter graphW = null;
        _sys = new SystemBuilderByFolderOfJars();

        logger.info("***Start graph building*** - " + projectFolder.toString());
        _sys.readClass(projectFolder.toString());
        graphB = new GraphBuilder(_sys.getClassesHashMap(), _sys.getPackagesHashMap());
        graphW = new Neo4JGraphWriter();
        logger.info("***Start Writing Neo4j***");
        logger.info("***Start Writing Neo4j*** - " + dbFolder.toPath());
        graphW.setup(dbFolder.toPath().toAbsolutePath().toString());
        Graph graph = graphW.init();
        logger.info("***Graph initializated***");
        logger.info("***Graph initializated*** - graph:   " + graph);
        logger.info("***Graph initializated*** - builder: " + graphB);
        graphB.createGraph(graph);
        logger.info("***Graph readed from compiled file***");
        graphW.write(graph, false);
        logger.info("***End of graph building***");
        _graphNeo4j = graph;
    }

    @Test
    public void testCycleTinkerpopAndNeo4j() {
        testCycleAndPrint(_graphTinkerpop);
        testCycleAndPrint(_graphNeo4j);

    }

    
    public void testCycleAndPrint(Graph graph) {
        logger.info("***Start cycles filtering***");
        Neo4JGraphWriter graphW = new Neo4JGraphWriter();
        CDFilterUtils.cleanCDShapeFilter(_graphNeo4j);

        CDShapeFilter filter = new CDShapeFilter(_graphNeo4j);

        //filter.getCircleCycles(GraphBuilder.CLASS, GraphBuilder.LABEL_CLASS_DEPENDENCY);
        filter.getCircleCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);

        //filter.getCliqueCycles(GraphBuilder.CLASS, GraphBuilder.LABEL_CLASS_DEPENDENCY);
        filter.getCliqueCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);

        //filter.getStarAndChainCycles(GraphBuilder.CLASS);
        filter.getStarAndChainCycles(GraphBuilder.PACKAGE);

        if (_graphNeo4j instanceof Neo4jGraph) {
            graphW.write(_graphNeo4j, false);
        }
/*
        CyclePrinter printerShape = new PrintShapesDocker(_nameFile);
        printerShape.initializePrint(outputFilePath, GraphBuilder.CLASS);
        printerShape.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.CLASS));
        printerShape.closePrint();*/

        CyclePrinter printerShape2 = new PrintShapesDocker(_nameFile);
        printerShape2.initializePrint(outputFilePath, GraphBuilder.PACKAGE);
        printerShape2.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.PACKAGE));
        printerShape2.closePrint();
        logger.info("***End of cycles filtering***");
    }

}
