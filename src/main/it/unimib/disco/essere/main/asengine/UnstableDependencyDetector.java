package it.unimib.disco.essere.main.asengine;

import java.util.*;
import java.util.Map.Entry;

import it.unimib.disco.essere.main.graphmanager.EdgeMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.asengine.udutils.UDUtils;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import org.neo4j.cypher.internal.compiler.v2_2.functions.E;

public class UnstableDependencyDetector {
    private static final Logger logger = LogManager.getLogger(UnstableDependencyDetector.class);
    public Graph _graph;
    public PackageMetricsCalculator _calc;
    // private Neo4JGraphWriter graphW;
    private Map<String, List<String>> _smellMap;
    private Map<String, Double> _instabilityMap;
    private Map<String,Vertex> udSmells = new HashMap<>(); // Modded (reduce graph traversals)
    private Map<String,Vertex> packages; // Modded
    private EdgeMaps edgeMaps; // Modded


    public UnstableDependencyDetector(Graph graph, PackageMetricsCalculator calculator) {
        _graph = graph;
        _calc = calculator;
        _smellMap = new HashMap<>();
        _instabilityMap = new HashMap<>();
        List<Vertex> packageList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);
        for (Vertex vertex : packageList) {
            packages.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
        this.edgeMaps = new EdgeMaps(graph);
        // graphW = new Neo4JGraphWriter();
    }

    public UnstableDependencyDetector(Graph graph, PackageMetricsCalculator calculator,Map<String,Vertex> packages, EdgeMaps edgeMaps) {
        _graph = graph;
        _calc = calculator;
        _smellMap = new HashMap<>();
        _instabilityMap = new HashMap<>();
        this.packages = packages; //Modded
        this.edgeMaps = edgeMaps; //Modded
        // graphW = new Neo4JGraphWriter();
    }


    /**
     * Looks for unstable dependency smells regarding packages in the graph.
     * 
     * @return a map with every package affected by the smell and the
     *         dependences which caused it.
     * @throws TypeVertexException
     */
    public Map<String, List<String>> detect() throws TypeVertexException {
        _instabilityMap = _calc.calculatePackagesInstability();
        List<Edge> affEdges = edgeMaps.findEdgesByLabel(GraphBuilder.LABEL_PACKAGE_AFFERENCE);

        for (Edge e : affEdges) {
            String inVName = e.inVertex().property(GraphBuilder.PROPERTY_NAME).value().toString();
            String outVName = e.outVertex().property(GraphBuilder.PROPERTY_NAME).value().toString();
            if (_instabilityMap.get(inVName) > _instabilityMap.get(outVName)) {
                if (_smellMap.get(outVName) == null) {
                    List<String> smellers = new ArrayList<>();
                    _smellMap.put(outVName, smellers);
                }
                _smellMap.get(outVName).add(inVName);
            }
        }
        return _smellMap;
    }

    /**
     * Looks for unstable dependency smells regarding packages in the graph. The
     * detection updates the graph adding a node smell and storing the number of
     * "bad dependencies" which lead the package to be affected by the smell and
     * the degree of unstable dependency [badDependecies/totalDependencies].
     * 
     * @return TODO
     * 
     * @throws TypeVertexException
     */
    public boolean newDetect() throws TypeVertexException {
        List<Edge> affEdges = edgeMaps.findEdgesByLabel(GraphBuilder.LABEL_PACKAGE_AFFERENCE);
        int numBadDep;
        int numTotalDep;
        int ratio;
        if (affEdges != null) {
            for (Edge e : affEdges) {
                String inVName = e.inVertex().property(GraphBuilder.PROPERTY_NAME).value().toString();
                String outVName = e.outVertex().property(GraphBuilder.PROPERTY_NAME).value().toString();
                if ((double) e.inVertex().value(GraphBuilder.PROPERTY_INSTABILITY) > (double) e.outVertex()
                        .value(GraphBuilder.PROPERTY_INSTABILITY)) {
                    // creation of the smell node
                    Vertex smellNode = udSmells.get(outVName);
                    //Vertex smellNode = GraphUtils.findVertex(_graph, outVName, GraphBuilder.SMELL);

                    if (smellNode == null) {
                        int[] info = new int[2];
                        info[0] = 0; // num bad dependencies
                        info[1] = 0; // ratio (bad/total)
                        smellNode = GraphUtils.createUDSmellVertex(_graph, outVName, info);
                        udSmells.put(outVName,smellNode);

                        // creation of the edge between the smell and the
                        // package
                        // affected by it
                        smellNode.addEdge(GraphBuilder.LABEL_AFFECTED_PACKAGE, e.outVertex());
                    }
                    numBadDep = smellNode.value(GraphBuilder.PROPERTY_NUM_BAD_DEPENDENCIES);
                    numBadDep += 1;
                    smellNode.property(GraphBuilder.PROPERTY_NUM_BAD_DEPENDENCIES, numBadDep);

                    numTotalDep = e.outVertex().value(GraphBuilder.PROPERTY_NUM_TOTAL_DEPENDENCIES);

                    ratio = (int) (((double) numBadDep / (double) numTotalDep) * 100);
                    logger.debug("ratio : " + ratio + ", total dep: " + numTotalDep);
                    smellNode.property(GraphBuilder.PROPERTY_DOUD, ratio);

                    // creation of the edge between the smell and the packages
                    // causing it
                    smellNode.addEdge(GraphBuilder.LABEL_BAD_DEPENDENCY, e.inVertex());

                }
            }
            return true;
        } else {
            logger.info("***No Ustable Dependency Smell were found in the project.***");
            return false;
        }

    }

    /**
     * Gives information about the package affected by the smell, that are the
     * number of bad dependences, the number of total dependency and their
     * ratio.
     * 
     * @return a map with every package affected by the smell and the list
     *         containing the number of bad dependences, the number of total
     *         dependency and their ratio.
     * @throws TypeVertexException
     */
    public Map<String, Integer[]> getStatistics() throws TypeVertexException {
        Map<String, Double> instabilityMap = _calc.calculatePackagesInstability();
        List<Edge> affEdges = edgeMaps.findEdgesByLabel(GraphBuilder.LABEL_PACKAGE_AFFERENCE);
        Map<String, Integer[]> statMap = new HashMap<>();
        logger.debug("size of list of afferent edges" + affEdges.size());

        // fills the map with every package and a list of info about it
        for (Edge e : affEdges) {
            String inVName = e.inVertex().property(GraphBuilder.PROPERTY_NAME).value().toString();
            String outVName = e.outVertex().property(GraphBuilder.PROPERTY_NAME).value().toString();
            if (statMap.get(outVName) == null) {
                Integer[] statistics = new Integer[3];
                statistics[0] = 0; // num of "bad dependencies"
                statistics[1] = 0; // num of total dependencies
                statistics[2] = 0; // ratio of bad dep on total dep
                statMap.put(outVName, statistics);
            }
            if (instabilityMap.get(inVName) > instabilityMap.get(outVName)) {
                ++statMap.get(outVName)[0];
            }
            statMap.get(outVName)[1] += 1;

        }

        List<String> toDelete = new ArrayList<>();

        /*
         * check if the map contains packages with no "bad dependencies" or no
         * dependencies at all and delete them. Otherwise, it computes the
         * ration of bad dependencies on total dependencies.
         */
        for (Entry<String, Integer[]> e : statMap.entrySet()) {
            if (e.getValue()[0] == 0 || e.getValue()[1] == 0) {
                toDelete.add(e.getKey());
            } else {
                e.getValue()[2] = (int) (((double) e.getValue()[0] / (double) e.getValue()[1]) * 100);
            }
        }

        logger.debug("size to delete " + toDelete.size());
        for (String s : toDelete) {
            statMap.remove(s);
        }
        return statMap;
    }

    public Map<String, Double> getInstabilityMap() {
        Map<String, Double> instabilityMap = new HashMap<>();
        for (Vertex p : packages.values()) {
            instabilityMap.put(p.value(GraphBuilder.PROPERTY_NAME), p.value(GraphBuilder.PROPERTY_INSTABILITY));
        }
        return instabilityMap;
    }

    // ottiene la mappa che viene spedita al printer
    public Map<String, List<String>> getSmellMap() {
        Map<String, List<String>> smellMap = new HashMap<String, List<String>>();
        List<Edge> badDependencies = new ArrayList<>();
        List<Vertex> smells = getListOfUDSmells(); // Modded
        /*List<Vertex> smells = GraphUtils.findVerticesByProperty(_graph, GraphBuilder.SMELL,
                GraphBuilder.PROPERTY_SMELL_TYPE, GraphBuilder.UNSTABLE_DEPENDENCY);*/
        Direction d = Direction.OUT;

        if (smells != null) {
            for (Vertex v : smells) {
                UDUtils.createMap(smellMap, d, v,edgeMaps);
            }
        }
        return smellMap;
    }

    // Modded
    public List<Vertex> getListOfUDSmells() {
        return new ArrayList<>(udSmells.values());
    }

}
