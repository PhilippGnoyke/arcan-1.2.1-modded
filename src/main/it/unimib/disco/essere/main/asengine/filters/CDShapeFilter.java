package it.unimib.disco.essere.main.asengine.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.asengine.cycleutils.CDFilterUtils;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;

public class CDShapeFilter implements FilterInterface {
    private static final Logger logger = LogManager.getLogger(CDShapeFilter.class);
    private Graph graph;

    public CDShapeFilter(Graph graph) {
        this.graph = graph;
    }

    // main filter
    public void filter() {

    }

    // tiny cycles filter
    /**
     * @param vertexType
     */
    public void getTinyCycles(String vertexType) {
        List<Vertex> tinyCycles = graph.traversal().V().has(GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType)
                .has(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES, 2).toList();
        for (Vertex v : tinyCycles) {
            logger.debug("tiny cycle: " + v.id());
            Iterator<Edge> edges = v.edges(Direction.OUT, GraphBuilder.LABEL_CYCLE_AFFECTED);
            CDFilterUtils.getMinMaxOfWeight(graph, v,GraphBuilder.LABEL_CYCLE_AFFECTED);
            while (edges.hasNext()) {
                Edge e = edges.next();
                logger.debug("node in cycle: " + e.inVertex().value(GraphBuilder.PROPERTY_NAME));
            }
        }
    }

    /**
     * @param vertexType
     * @param edgeLabel
     */
    public void getCircleCycles(String vertexType, String edgeLabel) {
        Iterator<Vertex> circleCycles = graph.traversal().V().has(GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType)
                .has(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES, P.gt(2));
        while (circleCycles.hasNext()) {
            Vertex smell = circleCycles.next();
            int numVertices = smell.value(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES);
            long numEdges = CDFilterUtils.getNumberOfEdges(graph, smell, edgeLabel);
            if (numEdges == numVertices) {
                logger.debug("Cycle: " + smell.id() + " is a circle");
                Vertex shapeVertex = CDFilterUtils.createShapeVertex(graph, GraphBuilder.CIRCLE, vertexType);
                shapeVertex.addEdge(GraphBuilder.LABEL_IS_CIRCLE_SHAPED, smell);
                CDFilterUtils.getMinMaxOfWeight(graph, shapeVertex,GraphBuilder.LABEL_IS_CIRCLE_SHAPED);

            }
        }
    }

    /**
     * @param vertexType
     * @param edgeLabel
     */
    public void getCliqueCycles(String vertexType, String edgeLabel) {
        Iterator<Vertex> cliqueCycles = graph.traversal().V().has(GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType)
                .has(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES, P.gt(2));
        int numVertices;
        long numEdges;
        
        while (cliqueCycles.hasNext()) {
            Vertex smell = cliqueCycles.next();
            numVertices = smell.value(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES);
            numEdges = CDFilterUtils.getNumberOfEdges(graph, smell, edgeLabel);
            if (numEdges == Math.pow(numVertices, 2) - numVertices) {
                logger.debug("Cycle: " + smell.id() + " is a clique by 1 formula");
                List<Vertex> vertices = CDFilterUtils.getAllOutVertices(smell, GraphBuilder.LABEL_CYCLE_AFFECTED);
                List<Edge> edges = CDFilterUtils.getAllEdgesOfCycle(graph, smell, edgeLabel);
                boolean isClique = true;
                for (Vertex v : vertices) {
                    Iterator<Edge> i = v.edges(Direction.OUT, edgeLabel);
                    long count = 0;
                    while (i.hasNext()) {
                        Edge e = i.next();
                        if (edges.contains(e)) {
                            count += 1;
                        }
                    }
                    if (count != numVertices - 1) {
                        isClique = false;
                    }
                }
                if (isClique == true) {
                    logger.debug("The Cycle is a clique by 2 formula");
                    Vertex shapeVertex = CDFilterUtils.createShapeVertex(graph, GraphBuilder.CLIQUE, vertexType);
                    shapeVertex.addEdge(GraphBuilder.LABEL_IS_CLIQUE_SHAPED, smell);
                    CDFilterUtils.getMinMaxOfWeight(graph, shapeVertex,GraphBuilder.LABEL_IS_CLIQUE_SHAPED);
                }
            }
        }
    }

    /**
     * @param vertexType
     */
    public void getStarAndChainCycles(String vertexType) {
        List<Edge> edges = graph.traversal().V().has(GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType)
                .has(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES, P.eq(2)).outE(GraphBuilder.LABEL_CYCLE_AFFECTED)
                .toList();

        /*
         * Key: the vertex(class/package) in the middle of the star; Value: the
         * list of smell vertex(type tiny cycle) which are part of the star.
         */
        Map<Vertex, List<Vertex>> stars = new HashMap<>();
        for (Edge e : edges) {
            List<Vertex> cyclesInStars;
            if (!stars.containsKey(e.inVertex())) {
                cyclesInStars = new ArrayList<>();

            } else {
                cyclesInStars = stars.get(e.inVertex());
            }
            cyclesInStars.add(e.outVertex());
            stars.put(e.inVertex(), cyclesInStars);
        }

        logger.debug("Star and Chain Map: " + stars);

        for (Entry<Vertex, List<Vertex>> entry : stars.entrySet()) {
            List<Vertex> cyclesInStar = entry.getValue();
//            CDFilterUtils.getMinOfWeight(graph, cyclesInStar, edgeLabel);
            
            if (cyclesInStar.size() > 2) {
                getStarCycles(entry.getKey(), cyclesInStar, vertexType);
            } else if (cyclesInStar.size() == 2) {
                getChainCycles(cyclesInStar.toArray(new Vertex[2]), vertexType);
            }
        }

    }

    public void getStarCycles(Vertex centre, List<Vertex> cyclesInStar, String vertexType) {
        logger.debug("Cycles " + cyclesInStar + (" are part of a star shaped cycle with centre in " + centre));
        // creation of supernode star
        Vertex star = CDFilterUtils.createShapeVertex(graph, GraphBuilder.STAR, vertexType);
        int numCyclesInStar = cyclesInStar.size();
        for (Vertex c : cyclesInStar) {
            if (GraphBuilder.CLASS.equals(vertexType)) {
                List<Vertex> verticesInCycle = CDFilterUtils.getAllOutVertices(c, GraphBuilder.LABEL_CYCLE_AFFECTED);
                
                for (Vertex v : verticesInCycle) {
                    // check if the vertices (case classes) involved in the star
                    // are inner classes and consider it as false positives
                    if (!CDFilterUtils.checkIfNestedClass(centre, v)/* & !v.equals(centre)*/) {
                        logger.debug("without nested");
                        star.addEdge(GraphBuilder.LABEL_IS_PART_OF_STAR, c);
                        
                    }else{
                        logger.debug("nested");
                        --numCyclesInStar;
                    }
                }
            } else{
                star.addEdge(GraphBuilder.LABEL_IS_PART_OF_STAR, c);
            }
        }

        if (star.edges(Direction.OUT, GraphBuilder.LABEL_IS_PART_OF_STAR).hasNext() && numCyclesInStar > 1) {
            star.addEdge(GraphBuilder.LABEL_IS_CENTRE_OF_STAR, centre);
            CDFilterUtils.getMinMaxOfWeight(graph, star,GraphBuilder.LABEL_IS_PART_OF_STAR);
        }else{
            //should save the remaining tinies and check if they are chains
            star.remove();
        }
        
    }

    public void getChainCycles(Vertex[] tinyChainCycles, String vertexType) {
        Vertex[] chainVertices = new Vertex[2];
        for (int i = 0; i < tinyChainCycles.length; ++i) {
            if (tinyChainCycles[i].edges(Direction.IN, GraphBuilder.LABEL_IS_PART_OF_CHAIN).hasNext()) {
                chainVertices[i] = tinyChainCycles[i].edges(Direction.IN, GraphBuilder.LABEL_IS_PART_OF_CHAIN).next()
                        .outVertex();
            } 
        }

        if (chainVertices[0] == null && chainVertices[1] == null) {
            logger.debug("creating new node chain linked to " + tinyChainCycles[0] + " and to " + tinyChainCycles[1]);
            // create new node chain
            Vertex newChain = CDFilterUtils.createShapeVertex(graph, GraphBuilder.CHAIN, vertexType);
            newChain.addEdge(GraphBuilder.LABEL_IS_PART_OF_CHAIN, tinyChainCycles[0]);
            newChain.addEdge(GraphBuilder.LABEL_IS_PART_OF_CHAIN, tinyChainCycles[1]);
            CDFilterUtils.getMinMaxOfWeight(graph, newChain,GraphBuilder.LABEL_IS_PART_OF_CHAIN);
            
        } else if (chainVertices[0] != null && chainVertices[1] != null && !chainVertices[0].equals(chainVertices[1])) {
            logger.debug("Merging two node chain");
            // merge two node chain
            List<Vertex> cyclesInChain = new ArrayList<>();
            Iterator<Edge> cyclesInChainIterator = chainVertices[0].edges(Direction.OUT,
                    GraphBuilder.LABEL_IS_PART_OF_CHAIN);
            while (cyclesInChainIterator.hasNext()) {
                Edge e = cyclesInChainIterator.next();
                cyclesInChain.add(e.inVertex());
                e.remove();
            }
            logger.debug("Deleted node chain " + chainVertices[0] + " and linked its cycle to node chain "
                    + chainVertices[1]);
            chainVertices[0].remove();

            for (Vertex v : cyclesInChain) {
                chainVertices[1].addEdge(GraphBuilder.LABEL_IS_PART_OF_CHAIN, v);
            }
            CDFilterUtils.getMinMaxOfWeight(graph, chainVertices[1],GraphBuilder.LABEL_IS_PART_OF_CHAIN);

        } else {
            // link the tiny cycle to existent node chain
            if (chainVertices[0] == null) {
                logger.debug("1)Linking " + tinyChainCycles[0] + " to existent node chain "
                        + "; the vertex already linked was " + tinyChainCycles[1]);
                chainVertices[1].addEdge(GraphBuilder.LABEL_IS_PART_OF_CHAIN, tinyChainCycles[0]);
                CDFilterUtils.getMinMaxOfWeight(graph, chainVertices[1],GraphBuilder.LABEL_IS_PART_OF_CHAIN);
            } else {
                logger.debug("2)Linking " + tinyChainCycles[1] + " to existent node chain"
                        + "; the vertex already linked was " + tinyChainCycles[0]);
                chainVertices[0].addEdge(GraphBuilder.LABEL_IS_PART_OF_CHAIN, tinyChainCycles[1]);
                CDFilterUtils.getMinMaxOfWeight(graph, chainVertices[0],GraphBuilder.LABEL_IS_PART_OF_CHAIN);
            }
        }
    }

}
