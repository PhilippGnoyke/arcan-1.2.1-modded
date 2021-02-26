package it.unimib.disco.essere.main.asengine;

import java.util.*;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.ClassMetricsCalculator;

public class HubLikeDetector {
    private static final int THRESHOLD = 4;
    private static final Logger logger = LogManager.getLogger(HubLikeDetector.class);
    private Graph _graph;
    private ClassMetricsCalculator calc;
    private Map<String, List<Integer>> smells;
    private int totalNumDependences;
    private int totalNumFanIn;
    private int totalNumFanOut;
    private int classesWithFanIn;
    private int classesWithFanOut;
    private List<Vertex> hdSmells = new ArrayList<>(); // Modded (reduce graph traversals)

    private Set<Integer> fanins = new HashSet<>();
    private Set<Integer> fanouts = new HashSet<>();

    public HubLikeDetector(Graph graph, ClassMetricsCalculator calc) {
        this._graph = graph;
        this.calc = calc;
        smells = new HashMap<>();
    }

    public Map<String, List<Integer>> detect() throws TypeVertexException {
        logger.debug("start hub like class detection");

        List<Vertex> classes = new ArrayList<>();
        for (Vertex v : GraphUtils.findVerticesByLabel(_graph, GraphBuilder.CLASS)) {
            if (v.value(GraphBuilder.PROPERTY_CLASS_TYPE).equals(GraphBuilder.SYSTEM_CLASS)) {
                classes.add(v);
            }
        }
        int medianFanIn = 0;
        int medianFanOut = 0;

        Map<String, List<Integer>> numDependences = getNumDependences(classes);

        if (!classes.isEmpty() && totalNumDependences != 0) {
            logger.debug("median fanIn " + median(fanins));
            logger.debug("n classes with fanIn: " + classesWithFanIn);
            logger.debug("n classes with fanOut: " + classesWithFanOut);
            logger.debug("total n fanIn " + totalNumFanIn);
            logger.debug("total n fanOut " + totalNumFanOut);
            medianFanIn = median(fanins);
            medianFanOut = median(fanouts);
            logger.debug("median fanIn: " + medianFanIn);
            logger.debug("median fanOut: " + medianFanOut);

        } else {
            logger.error("No classes or dependences found to analyze");
        }

        if (numDependences != null && medianFanIn != 0 && medianFanOut != 0) {

            for (Entry<String, List<Integer>> entry : numDependences.entrySet()) {
                if (entry.getValue().get(1) > medianFanIn && entry.getValue().get(2) > medianFanOut
                        && Math.abs(entry.getValue().get(1) - entry.getValue().get(2)) <= entry.getValue().get(0)
                                / THRESHOLD) {
                    smells.put(entry.getKey(), entry.getValue());
                    Vertex smellNode = GraphUtils.findVertex(_graph, entry.getKey(), GraphBuilder.SMELL);

                    if (smellNode == null) {
                        smellNode = GraphUtils.createHLSmellVertex(_graph, entry.getKey(), entry.getValue().get(1),
                                entry.getValue().get(2), entry.getValue().get(0));
                        hdSmells.add(smellNode);
                        logger.debug("smell vertex: " + smellNode + " with key: " + entry.getKey());
                        Vertex classNode = GraphUtils.findVertex(_graph, entry.getKey(), GraphBuilder.CLASS);
                        logger.debug("class node vertex: " + classNode + " with key: " + entry.getKey());
                        smellNode.addEdge(GraphBuilder.LABEL_AFFECTED_CLASS, classNode);
                        Iterator<Edge> classesIn = classNode.edges(Direction.IN,
                                PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
                        Iterator<Edge> classesOut = classNode.edges(Direction.OUT,
                                PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
                        while (classesIn.hasNext()) {
                            Edge e = classesIn.next();
                            smellNode.addEdge(GraphBuilder.LABEL_IS_HL_IN, e.outVertex());
                            logger.debug("in edge:" + e + " out vertex: " + e.outVertex());
                        }
                        while (classesOut.hasNext()) {
                            Edge e = classesOut.next();
                            smellNode.addEdge(GraphBuilder.LABEL_IS_HL_OUT, e.inVertex());
                            logger.debug("out edge:" + e + " out vertex: " + e.inVertex());
                        }
                    }
                }
            }
        }

        logger.debug("end hub like class detection");

        return smells;
    }

    public Map<String, List<Integer>> getSmells() {
        return smells;
    }

    public Map<String, List<Integer>> getNumDependences(List<Vertex> classes) throws TypeVertexException {
        Map<String, List<Integer>> allDependences = new HashMap<>();

        int fanIn = 0;
        int fanOut = 0;

        for (Vertex v : classes) {
            List<Integer> dep = new ArrayList<>();

            fanIn = calc.calculateFanIn(v);
            fanOut = calc.calculateFanOut(v);
            int vNumDependences = fanIn + fanOut;

            totalNumDependences += vNumDependences;
            if (fanIn > 0) {
                fanins.add(fanIn);

                totalNumFanIn += fanIn;
                ++classesWithFanIn;
            }
            if (fanOut > 0) {
                fanouts.add(fanOut);

                totalNumFanOut += fanOut;
                ++classesWithFanOut;
            }

            dep.add(vNumDependences);
            dep.add(fanIn);
            dep.add(fanOut);
            allDependences.put(v.value(GraphBuilder.PROPERTY_NAME), dep);

        }
        return allDependences;
    }

    private int median(Set<Integer> fans) {        
        if (fans.isEmpty()) {
            return 0;
        } else {
            List<Integer> l = new ArrayList<>();
            l.addAll(fans);
            l.sort(null);
            return l.get(fans.size() / 2);
        }
    }

    // Modded
    public List<Vertex> getListOfHubLikeSmells() {
        return hdSmells;
    }

}
