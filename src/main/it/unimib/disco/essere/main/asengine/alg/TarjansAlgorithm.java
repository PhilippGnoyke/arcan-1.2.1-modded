package it.unimib.disco.essere.main.asengine.alg;

import it.unimib.disco.essere.main.ETLE;
import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.cycleutils.CDFilterUtils;
import it.unimib.disco.essere.main.asengine.cycleutils.SuperCycleShapeClassifier;
import it.unimib.disco.essere.main.graphmanager.*;
import it.unimib.disco.essere.main.metricsengine.MEFSCalculator;
import it.unimib.disco.essere.main.metricsengine.MiscSmellMetricsCalculator;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.*;
import java.util.concurrent.TimeUnit;

// Original implementation (modified to fit with Tinkerpop/Gremlin):
// https://github.com/williamfiset/Algorithms/blob/master/src/main/java/com/williamfiset/algorithms/graphtheory/TarjanSccSolverAdjacencyList.java

public class TarjansAlgorithm
{
    private static final float HASH_LOAD_FACTOR = 0.75f;

    private Graph graph;
    private Map<String, Vertex> vertices;
    private String vertexType;
    private String depLabel;

    private int mainIndex;
    private Stack<Vertex> stack;
    private Map<Object, Integer> indices;
    private Map<Object, Integer> lowLink;
    private HashSet<Object> onStack;
    private List<Vertex> supercycles;
    private List<Vertex> subcycles;
    private Map<String, Vertex> vertexIdsToSupercycles;
    private long packCount;
    private long classCount;
    private EdgeMaps edgeMaps;
    private ClassFilter classFilter;
    private ExTimeLogger exTimeLogger;


    public TarjansAlgorithm(Graph graph, Map<String, Vertex> vertices, List<Vertex> subcycles, String vertexType, long packCount,
                            long classCount, EdgeMaps edgeMaps, ClassFilter classFilter, ExTimeLogger exTimeLogger)
    {
        this.graph = graph;
        this.vertexType = vertexType;
        mainIndex = 0;
        this.stack = new Stack<>();
        indices = new HashMap<>((int) (vertices.size() / HASH_LOAD_FACTOR + 1), HASH_LOAD_FACTOR);
        lowLink = new HashMap<>((int) (vertices.size() / HASH_LOAD_FACTOR + 1), HASH_LOAD_FACTOR);
        onStack = new HashSet<>();
        supercycles = new ArrayList<>();
        this.subcycles = subcycles;
        vertexIdsToSupercycles = new HashMap<>((int) (vertices.size() / HASH_LOAD_FACTOR + 1), HASH_LOAD_FACTOR);
        this.packCount = packCount;
        this.classCount = classCount;
        this.edgeMaps = edgeMaps;
        this.classFilter = classFilter;
        this.exTimeLogger = exTimeLogger;
        depLabel = vertexType.equals(GraphBuilder.CLASS) ? GraphBuilder.LBL_CLASS_DEP : GraphBuilder.LBL_PACK_DEP;

        if (classFilter != null)
        {
            this.vertices = new HashMap<>();
            for (Vertex vertex : vertices.values())
            {
                String compName = vertex.value(GraphBuilder.PROPERTY_NAME);
                if (classFilter.isSharedComponent(compName))
                {
                    this.vertices.put(compName, vertex);
                }
            }
        }
        else
        {
            this.vertices = vertices;
        }
    }

    public void calc()
    {
        for (Vertex vertex : vertices.values())
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


    private void search(Vertex vertex)
    {
        Object id = vertex.id();
        indices.put(id, mainIndex);
        lowLink.put(id, mainIndex);
        mainIndex++;
        stack.push(vertex);
        onStack.add(id);

        List<Edge> edges = edgeMaps.getEdgesByOutVertex(depLabel,vertex);
        if(edges!= null)
        {
            for (Edge edge : edges)
            {
                Vertex other = edge.inVertex();
                if (classFilter != null)
                {
                    if (!classFilter.isSharedComponent(other.value(GraphBuilder.PROPERTY_NAME)))
                    {
                        continue;
                    }
                }
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
        }
        if (lowLink.get(id).intValue() == indices.get(id).intValue())
        {
            Map<String, Vertex> comps = new HashMap<>();
            Vertex comp = null;
            do
            {
                comp = stack.pop();
                comps.put(comp.value(GraphBuilder.PROPERTY_NAME).toString(), comp);
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

    private Vertex createSmellNode(Map<String, Vertex> comps)
    {
        List<Vertex> compsList = new ArrayList<>(comps.values());
        Vertex supercycle = GraphUtils.createSuperCycleSmellVertex(graph);
        supercycle.property(GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType);
        edgeMaps.initSupercycle(supercycle,compsList,depLabel);

        int order = 0; // Number of components in cycle

        for (Vertex comp : compsList)
        {
            supercycle.addEdge(GraphBuilder.LABEL_SUPERCYCLE_AFFECTED, comp);
            order++;
            vertexIdsToSupercycles.put(comp.id().toString(), supercycle);
        }
        supercycle.property(GraphBuilder.PROPERTY_ORDER, order);

        List<Edge> edges = edgeMaps.getSuperCycleEdges(supercycle);
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
        calcInheritEdges(supercycle, comps, size);
        assignSubCycles(supercycle, new HashSet<>(comps.values()));
        assignShape(supercycle, comps, edges, order, size);
        calcMEFS(supercycle, compsList, edges);
        return supercycle;
    }

    public void calcMEFS(Vertex smell, List<Vertex> comps, List<Edge> edges)
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CDS_SUPERCYCLE_CLASS_CD_MEFS : ETLE.Event.CDS_SUPERCYCLE_PACK_CD_MEFS);
        MEFSCalculator mefsCalculator = new MEFSCalculator(new HashSet<>(comps), edges);
        smell.property(GraphBuilder.PROPERTY_MEFS_SIZE, mefsCalculator.getMEFSSize());
        smell.property(GraphBuilder.PROPERTY_REL_MEFS_SIZE, mefsCalculator.getRelativeMEFSSize());
        smell.property(GraphBuilder.PROPERTY_MEFS, mefsCalculator.getEdgeFeedbackSet());
        smell.property(GraphBuilder.PROPERTY_MEFS_SIZE_WO_TINYS, mefsCalculator.getMEFSSizeWOTinys());
        smell.property(GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS, mefsCalculator.getRelativeMEFSSizeWOTinys());
        smell.property(GraphBuilder.PROPERTY_MEFS_WO_TINYS, mefsCalculator.getEdgeFeedbackSetWOTinys());
        smell.property(GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS_REDUCTION, mefsCalculator.getMEFSSizeWOTinysReduction());
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CDS_SUPERCYCLE_CLASS_CD_MEFS : ETLE.Event.CDS_SUPERCYCLE_PACK_CD_MEFS);
    }


    private void assignShape(Vertex supercycle, Map<String, Vertex> comps, List<Edge> edges, int order, int size)
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CDS_SUPERCYLE_CLASS_CD_SHAPE : ETLE.Event.CDS_SUPERCYLE_PACK_CD_SHAPE);
        SuperCycleShapeClassifier shapeClassifier = new SuperCycleShapeClassifier(supercycle, comps, edges, order, size, vertexType,edgeMaps,exTimeLogger);
        String shape = shapeClassifier.classifyShape();
        supercycle.property(GraphBuilder.PROPERTY_SHAPE, shape);
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CDS_SUPERCYLE_CLASS_CD_SHAPE : ETLE.Event.CDS_SUPERCYLE_PACK_CD_SHAPE);
    }

    //TODO Optimize, avoid graph traversal
    private void calcInheritEdges(Vertex supercycle, Map<String, Vertex> comps, int size)
    {
        String lblChild = PropertyEdge.LABEL_SUPER_DEPENDENCY.toString();
        List<Edge> inheritEdges = edgeMaps.allEdgesBetweenVertices(new ArrayList<>(comps.values()), lblChild);
        int inheritEdgesCount = inheritEdges.size();
        supercycle.property(GraphBuilder.PROPERTY_NUM_INHERIT_EDGES, inheritEdgesCount);
        double relInheritEdgesCount = (double) inheritEdgesCount / size;
        supercycle.property(GraphBuilder.PROPERTY_REL_NUM_INHERIT_EDGES, relInheritEdgesCount);
        supercycle.property(GraphBuilder.PROPERTY_NUM_SUBCYCLES, 0);
    }

    private void assignSubCycles(Vertex supercycle, Set<Vertex> comps)
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CDS_SUBCYCLE_CLASS_CD_DETECTION : ETLE.Event.CDS_SUBCYCLE_PACK_CD_DETECTION);
        CyclicDependencyDetector cycleDetector = new CyclicDependencyDetector(graph, edgeMaps);
        cycleDetector.detectSubCycles(comps, vertexType,depLabel);
        List<Vertex> localSubcycles = cycleDetector.getListOfCycleSmells(vertexType);
        for (Vertex subCycle : localSubcycles)
        {
            subCycle.addEdge(GraphBuilder.LABEL_SUB_OF_SUPERCYCLE, supercycle);
            GraphUtils.incrementVertexIntProperty(supercycle, GraphBuilder.PROPERTY_NUM_SUBCYCLES);
            subcycles.add(subCycle);
        }
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CDS_SUBCYCLE_CLASS_CD_DETECTION : ETLE.Event.CDS_SUBCYCLE_PACK_CD_DETECTION);
    }

}