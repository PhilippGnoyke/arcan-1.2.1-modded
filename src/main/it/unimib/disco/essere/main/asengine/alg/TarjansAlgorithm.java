package it.unimib.disco.essere.main.asengine.alg;

import it.unimib.disco.essere.main.asengine.cycleutils.SuperCycleShapeClassifier;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.metricsengine.MiscSmellMetricsCalculator;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

// Original implementation (modified to fit with Tinkerpop/Gremlin):
// https://github.com/williamfiset/Algorithms/blob/master/src/main/java/com/williamfiset/algorithms/graphtheory/TarjanSccSolverAdjacencyList.java

public class TarjansAlgorithm
{
    private static final float HASH_LOAD_FACTOR = 0.75f;

    private Graph graph;
    private List<Vertex> vertices;
    private String vertexType;
    private String depLabel;

    private int mainIndex;
    private Stack<Vertex> stack;
    private Map<Object, Integer> indices;
    private Map<Object, Integer> lowLink;
    private HashSet<Object> onStack;
    private List<Vertex> supercycles;
    private Map<Object, Vertex> vertexIdsToSupercycles;
    private long packCount;
    private long classCount;

    public TarjansAlgorithm(Graph graph, List<Vertex> vertices, String vertexType,
                            String depLabel, long packCount, long classCount)
    {
        this.graph = graph;
        this.vertices = vertices;
        this.vertexType = vertexType;
        this.depLabel = depLabel;
        mainIndex = 0;
        this.stack = new Stack<>();
        indices = new HashMap<>((int) (vertices.size() / HASH_LOAD_FACTOR + 1), HASH_LOAD_FACTOR);
        lowLink = new HashMap<>((int) (vertices.size() / HASH_LOAD_FACTOR + 1), HASH_LOAD_FACTOR);
        onStack = new HashSet<>();
        supercycles = new ArrayList<>();
        vertexIdsToSupercycles = new HashMap<>((int) (vertices.size() / HASH_LOAD_FACTOR + 1), HASH_LOAD_FACTOR);
        this.packCount = packCount;
        this.classCount = classCount;
    }

    public void calc()
    {
        for (Vertex vertex : vertices)
        {
            if (!indices.containsKey(vertex.id()))
            {
                search(vertex);
            }
        }
    }

    public List<Vertex> getSupercycles()
    {
        return supercycles;
    }

    public Map<Object, Vertex> getVertexIdsToSupercycleIds()
    {
        return vertexIdsToSupercycles;
    }

    private void search(Vertex vertex)
    {
        Object id = vertex.id();
        indices.put(id, mainIndex);
        lowLink.put(id, mainIndex);
        mainIndex++;
        stack.push(vertex);
        onStack.add(id);

        for (Iterator<Edge> it = vertex.edges(Direction.OUT, depLabel); it.hasNext(); )
        {
            Vertex other = it.next().inVertex();
            Object otherId = other.id();
            if (!indices.containsKey(otherId))
            {
                search(other);
                lowLink.put(id, Math.min(lowLink.get(id), lowLink.get(otherId)));
            }
            else if (onStack.contains(otherId))
            {
                lowLink.put(id, Math.min(lowLink.get(id), indices.get(otherId)));
            }
        }
        if (lowLink.get(id).intValue() == indices.get(id).intValue())
        {
            Set<Vertex> comps = new HashSet<>();
            Vertex comp = null;
            do
            {
                comp = stack.pop();
                comps.add(comp);
                onStack.remove(comp.id());
            }
            while (vertex != comp);
            if (comps.size() >= 2)
            {
                Vertex supercycle = createSmellNode(comps);
                supercycles.add(supercycle);
            }
        }
    }

    private boolean isClassLevel()
    {
        return GraphBuilder.isClassLevel(depLabel);
    }

    private Vertex createSmellNode(Set<Vertex> comps)
    {
        Vertex supercycle = GraphUtils.createSuperCycleSmellVertex(graph);
        supercycle.property(GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType);

        int order = 0; // Number of components in cycle

        for (Vertex comp : comps)
        {
            supercycle.addEdge(GraphBuilder.LABEL_SUPERCYCLE_AFFECTED, comp);
            order++;
            vertexIdsToSupercycles.put(comp.id(), supercycle);
        }
        supercycle.property(GraphBuilder.PROPERTY_ORDER, order);

        List<Edge> edges = GraphUtils.allEdgesBetweenVertices(comps, depLabel);
        int size = edges.size();
        supercycle.property(GraphBuilder.PROPERTY_SIZE, size);
        MiscSmellMetricsCalculator.calcSizeOverComplexity(supercycle, size, order);
        MiscSmellMetricsCalculator.calcSmellDensity(supercycle, order, size);

        if (isClassLevel())
        {
            supercycle.property(GraphBuilder.PROPERTY_SHARE_CLASSES, (double) order / classCount);
            Set<Vertex> affectedPackages =
                MiscSmellMetricsCalculator.calcNumPackages(supercycle, packCount, GraphBuilder.LABEL_SUPERCYCLE_AFFECTED);
            for (Vertex packageVertex : affectedPackages)
            {
                supercycle.addEdge(GraphBuilder.LABEL_CLASS_CYCLE_IN_PACK, packageVertex);
            }
        }
        else
        {
            supercycle.property(GraphBuilder.PROPERTY_SHARE_PACKAGES, (double) order / packCount);

        }
        assignShape(supercycle, comps, edges, order, size, vertexType);
        calcInheritEdges(supercycle, comps, size);
        MiscSmellMetricsCalculator.calcMEFS(supercycle, comps, edges);
        return supercycle;
    }

    private static void assignShape(Vertex supercycle, Set<Vertex> comps, List<Edge> edges, int order, int size, String vertexType)
    {
        SuperCycleShapeClassifier shapeClassifier = new SuperCycleShapeClassifier(supercycle, comps, edges, order, size, vertexType);
        String shape = shapeClassifier.classifyShape();
        supercycle.property(GraphBuilder.PROPERTY_SHAPE, shape);
    }

    private static void calcInheritEdges(Vertex supercycle, Set<Vertex> comps, int size)
    {
        String lblChild = PropertyEdge.LABEL_SUPER_DEPENDENCY.toString();
        List<Edge> inheritEdges = GraphUtils.allEdgesBetweenVertices(comps, lblChild);
        int inheritEdgesCount = inheritEdges.size();
        supercycle.property(GraphBuilder.PROPERTY_NUM_INHERIT_EDGES, inheritEdgesCount);
        double relInheritEdgesCount = (double) inheritEdgesCount / size;
        supercycle.property(GraphBuilder.PROPERTY_REL_NUM_INHERIT_EDGES, relInheritEdgesCount);
        supercycle.property(GraphBuilder.PROPERTY_NUM_SUBCYCLES, 0);
    }
}