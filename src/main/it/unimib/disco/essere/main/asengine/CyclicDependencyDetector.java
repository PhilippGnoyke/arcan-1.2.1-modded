package it.unimib.disco.essere.main.asengine;

import java.io.File;
import java.util.*;

import it.unimib.disco.essere.main.graphmanager.EdgeMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.unimib.disco.essere.main.asengine.alg.SedgewickWayneDFSCycleDetectionAlg;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;

public class CyclicDependencyDetector {

    private static final String INTERNAL = "Internal";
    public static final String PACKAGE_CYCLES = "packageCycles";
    public static final String CLASSES_CYCLES = "classesCycles";
    private static final Logger logger = LogManager.getLogger(CyclicDependencyDetector.class);
    private Graph graph;
    //private List<Vertex> classes = null; // Modded (removed)
    private File path = null;
    private final List<Vertex> cdClassSmells = new LinkedList<>(); // Modded (reduce graph traversals)
    private List<Vertex> cdPackageSmells = new LinkedList<>(); // Modded (reduce graph traversals)

    private Map<String,Vertex> packages; // Modded
    private Map<String,Vertex> classes; // Modded
    private EdgeMaps edgeMaps; // Modded



    private boolean _suppressNonAsTdEvolution = false; // Modded


    public CyclicDependencyDetector(Graph graph, File path) {
        this.graph = graph;
        this.path = path;
        //Modded
        List<Vertex> classList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        List<Vertex> packageList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);
        for (Vertex vertex : classList) {
            classes.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
        for (Vertex vertex : packageList) {
            packages.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
        this.edgeMaps = new EdgeMaps(graph);
    }

    // Modded
    public CyclicDependencyDetector(Graph graph,File path,boolean suppressNonAsTdEvolution,
                                    Map<String,Vertex> classes, Map<String,Vertex> packages, EdgeMaps edgeMaps) {
        this.graph = graph;
        this.path = path;
        this._suppressNonAsTdEvolution = suppressNonAsTdEvolution;
        this.classes = classes;
        this.packages = packages;
        this.edgeMaps = edgeMaps;
    }

    // Modded
    public CyclicDependencyDetector(Graph graph, EdgeMaps edgeMaps) {
        this.graph = graph;
        this._suppressNonAsTdEvolution = true;
        this.edgeMaps = edgeMaps;
    }



    public void detect() {
        detectCyclesGephiInternal();
    }

    /**
     * Detects the project cycles using the gephi algorithm. It analyzes all
     * packages and classes of the system.
     */
    /**
    private void detectCyclesGephi() {
        logger.debug("***Start Cycle detection with Gephi algorithm***");
        SedgewickWayneDFSCycleDetectionAlg algoC = new SedgewickWayneDFSCycleDetectionAlg(this,
                classes,
                GraphUtils.findEdgesByLabel(graph, GraphBuilder.LBL_CLASS_DEP), graph, path,
                GraphBuilder.CLASS,_suppressNonAsTdEvolution);
        algoC.execute();

        SedgewickWayneDFSCycleDetectionAlg algoP = new SedgewickWayneDFSCycleDetectionAlg(this,
                packages,
                GraphUtils.findEdgesByLabel(graph, GraphBuilder.LABEL_PACKAGE_AFFERENCE.toString()), graph, path,
                GraphBuilder.PACKAGE,_suppressNonAsTdEvolution);

        algoP.execute();

        logger.debug("***End Cycle detection with Gephi algorithm***");
    }
     /**
    /**
     * Detect the project cycles using the gephi algorithm. It only analyzes
     * classes and packages internal to the system (not retrieved).
     */
    private void detectCyclesGephiInternal() {
        //List<Vertex> classVertices = GraphUtils.filterProperty(
        //        (List<Vertex>) classes.values(), GraphBuilder.PROPERTY_CLASS_TYPE,
        //        GraphBuilder.SYSTEM_CLASS);

        //List<Vertex> packageVertices = GraphUtils.filterProperty(
        //        (List<Vertex>) packages.values(), GraphBuilder.PROPERTY_PACKAGE_TYPE,
        //        GraphBuilder.SYSTEM_PACKAGE);

        logger.debug("***Start Internal Class Cycle detection with Gephi algorithm***");
        SedgewickWayneDFSCycleDetectionAlg algoC = new SedgewickWayneDFSCycleDetectionAlg(this,new HashSet<>(classes.values()),
                GraphBuilder.LBL_CLASS_DEP, graph, path,
                GraphBuilder.CLASS,_suppressNonAsTdEvolution,edgeMaps);
        algoC.execute();

        logger.debug("***End Internal Class Cycle detection with Gephi algorithm***");

        logger.debug("***Start Internal Package Cycle detection with Gephi algorithm***");

        SedgewickWayneDFSCycleDetectionAlg algoP = new SedgewickWayneDFSCycleDetectionAlg(this,new HashSet<>(packages.values()),
                GraphBuilder.LABEL_PACKAGE_AFFERENCE, graph, path,
                GraphBuilder.PACKAGE,_suppressNonAsTdEvolution,edgeMaps);

        algoP.execute();

        logger.debug("***End Internal Package Cycle detection with Gephi algorithm***");
    }


    //Modded
    public void detectSubCycles(Set<Vertex>comps, String vertexType, String depLabel)
    {
        SedgewickWayneDFSCycleDetectionAlg algo = new SedgewickWayneDFSCycleDetectionAlg(this,comps,
            depLabel, graph, path, vertexType,_suppressNonAsTdEvolution,edgeMaps);
        algo.execute();
    }


    // Maybe move it to CDUtils
    // Modded from here
    public List<Vertex> getListOfCycleSmells(String vertexType) {
        return vertexType.equals(GraphBuilder.CLASS) ? cdClassSmells : cdPackageSmells;
    }

    public void registerCdVertex(Vertex vertex, String vertexType)
    {
        if(vertexType.equals(GraphBuilder.CLASS))
        {
            cdClassSmells.add(vertex);
        }
        else
        {
            cdPackageSmells.add(vertex);
        }
    }
}
