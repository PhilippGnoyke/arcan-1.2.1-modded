package it.unimib.disco.essere.main.asengine.alg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import it.unimib.disco.essere.main.TerminalExecutor;
import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.asengine.cycleutils.PrintToMatrix;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintToTable;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.asengine.cycleutils.CyclePrinter;

public class SedgewickWayneDFSCycleDetectionAlg {

    private static final Logger logger = LogManager.getLogger(SedgewickWayneDFSCycleDetectionAlg.class);

    // private ProgressTicket progressTicket;
    // private GraphModel gm;

    private List<Vertex> marked; // marked[v] = has vertex v been marked?
    private Map<Vertex, Vertex> edgeTo; // edgeTo[v] = previous vertex on path
                                        // to v
    private List<Vertex> onStack; // onStack[v] = is vertex on the stack?

   // private List<Stack<Vertex>> cycle;
    private Vertex cycle; // directed cycle (or null if no such
    // cycle)
    private String vertexType;
    private int numCyclesVertices;

    private List<Vertex> neigh;

    private List<Vertex> vertices = null;
    private List<Edge> edges = null;
    private Graph graph;

    private int cycleCounter = 0;

    private CyclePrinter printer = null;
    private CyclePrinter printer2 = null;

    private CyclicDependencyDetector cyclicDependencyDetector; // Modded

    public SedgewickWayneDFSCycleDetectionAlg(
        CyclicDependencyDetector cyclicDependencyDetector,List<Vertex> vertices, List<Edge> edges, Graph graph,
        File path, String vertexType, boolean suppressNonAsTdEvolution)
    {
        this.cyclicDependencyDetector = cyclicDependencyDetector;
        this.vertices = vertices;
        this.edges = edges;
        this.graph = graph;
        this.vertexType = vertexType;
        // select the needed printer

        // Modded (additional condition)
        if(!suppressNonAsTdEvolution)
        {
            this.printer = new PrintToMatrix(vertices);
            this.printer2 = new PrintToTable(vertices);

            printer.initializePrint(path, vertexType);
            printer2.initializePrint(path, vertexType);
        }

    }

    public void execute() {

        marked = new ArrayList<Vertex>(vertices.size());
        onStack = new ArrayList<Vertex>(vertices.size());
        edgeTo = new HashMap<Vertex, Vertex>(vertices.size());

        //cycle = new ArrayList<Stack<Vertex>>();

        try {
            // Init the progress tick to the number of nodes to be visited
            // Progress.start(progressTicket, graph.getNodeCount());
            // Progress.setDisplayName(progressTicket, "Visiting nodes...");

            for (Vertex v : vertices) {
                if(v == null){
                }
                if (!marked.contains(v)) {
                    dfs(graph, v);
                }
            }

            printer.closePrint();
            printer2.closePrint();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

    /**
     * implements the Depht First Search algorithm in order to detect cycles in the graph.
     *
     * @param graph
     * @param node
     */
    private void dfs(final Graph graph, final Vertex node) {
        //logger.debug("node at dfs start: " + node);
            // A new node has been visited
            marked.add(node);
            onStack.add(node);

            neigh = new ArrayList<Vertex>();

            // For directed graphs, take only target nodes

            for (Edge e : edges) {
                if (e.outVertex().equals(node) && !e.outVertex().equals(e.inVertex())) {
                    neigh.add(e.inVertex());
                }
            }
            for (Vertex w : neigh) {

                if (!marked.contains(w)) {
                    edgeTo.put(w, node);
                    dfs(graph, w);
                } // trace back directed cycle
                else if (onStack.contains(w)) {
                    Stack<Vertex> oneCycle = new Stack<Vertex>();
                    cycle = GraphUtils.createCycleSmellVertex(graph);
                    cyclicDependencyDetector.registerCdVertex(cycle, vertexType);
                    cycle.property(GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType);
                    numCyclesVertices = 0;

                    for (Vertex x = node; !x.equals(w); x = edgeTo.get(x)) {
                        oneCycle.push(x);
                        cycle.addEdge(GraphBuilder.LABEL_CYCLE_AFFECTED, x, GraphBuilder.PROPERTY_ORDER_IN_CYCLE, numCyclesVertices);
                        numCyclesVertices += 1;
                    }
                    oneCycle.push(w);
                    cycle.addEdge(GraphBuilder.LABEL_CYCLE_AFFECTED, w, GraphBuilder.PROPERTY_ORDER_IN_CYCLE, numCyclesVertices);
                    numCyclesVertices += 1;
                    oneCycle.push(node);
                    cycle.addEdge(GraphBuilder.LABEL_START_CYCLE,  node);

                    cycle.property(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES, numCyclesVertices);
                    // Add to the list of cycles
                    // cycle.add(oneCycle);
                    logger.debug("***Start print cycle to CSV*** - " + cycleCounter);
                    //printer.printCycles(oneCycle);
                    logger.debug("Cycle: " + oneCycle);
                    //printer2.printCycles(oneCycle);
                    logger.debug("***End print cycle to CSV*** - " + cycleCounter);
                    ++cycleCounter;
                }
            }
            onStack.remove(node);

    }
}
