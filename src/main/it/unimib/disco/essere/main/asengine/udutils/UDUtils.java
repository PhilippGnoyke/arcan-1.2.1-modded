package it.unimib.disco.essere.main.asengine.udutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.unimib.disco.essere.main.graphmanager.EdgeMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;

public class UDUtils {
    private static final Logger logger = LogManager.getLogger(UDUtils.class);
    /**
     * Create the map which will contain the results of unstable dependency detection
     * 
     * @param smellMap
     * @param d
     * @param v
     */

    public static void createMap(Map<String, List<String>> smellMap, Direction d, Vertex v) {
        createMap(smellMap,d,v,new EdgeMaps(v.graph()));
    }

    public static void createMap(Map<String, List<String>> smellMap, Direction d, Vertex v, EdgeMaps edgeMaps) {
        List<Edge> badDependencies;
        badDependencies = edgeMaps.getEdgesByOutVertex(GraphBuilder.LABEL_BAD_DEPENDENCY, v);
        List<String> correlatedPackages = new ArrayList<>();
        GraphUtils.logger.debug("bad dep: " + badDependencies);
        for(Edge e : badDependencies){
           correlatedPackages.add(e.inVertex().value(GraphBuilder.PROPERTY_NAME));
        }  
        smellMap.put(v.value(GraphBuilder.PROPERTY_NAME), correlatedPackages);
    }

    /**
         * Clean up all the edges/vertices related to the detection of unstable
         * dependency from the graph
         * 
         * @param graph
         */
     // TODO to move into smell's detectors utils and find more efficent routine
        public static void cleanUDDetection(Graph graph) {
            logger.debug("***Start clean of UD detection***");
            Iterator<Edge> i = graph.traversal().E().hasLabel(GraphBuilder.LABEL_BAD_DEPENDENCY,
                    GraphBuilder.LABEL_AFFECTED_PACKAGE);
            while (i.hasNext()) {
                Edge e = i.next();
                e.remove();
            }
            Iterator<Vertex> iv = graph.traversal().V().has(GraphBuilder.PROPERTY_SMELL_TYPE,
                    GraphBuilder.UNSTABLE_DEPENDENCY);
            while (iv.hasNext()) {
                Vertex v = iv.next();
                v.remove();
            }
            //Neo4JGraphWriter graphW = new Neo4JGraphWriter();
            //graphW.write(graph, false);
            logger.debug("***End clean of UD detection***");
        }

}
