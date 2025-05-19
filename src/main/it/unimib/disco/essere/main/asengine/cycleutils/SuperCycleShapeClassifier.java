package it.unimib.disco.essere.main.asengine.cycleutils;

import it.unimib.disco.essere.main.ETLE;
import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.asengine.alg.BetweennessCentralityCalculator;
import it.unimib.disco.essere.main.graphmanager.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

// From Al-Mutawa: On The Shape of Circular Dependencies in Java Programs
public class SuperCycleShapeClassifier
{
    private static final int CHAIN_FRIENDS = 2;
    private static final int ORDER_TINY = 2;
    //Empirically, we found in the jSCOUTS corpus that no shapes other than MultiHub exist above 100 vertices
    //Even with a generous safety factor, we can achieve a considerable speed-up for systems with very large CDs
    //That is because the betweenness centrality is the only algorithm in Arcan that scales non-linearly with O(E*V)
    private static final int ORDER_ALWAYS_MULTIHUB = 500;
    private static final double RATIO_CYCLE = 0.75;
    private static final double BACKREF_CLIQUE = 0.75;
    private static final double DENSE_CLIQUE = 0.75;
    private static final double CHAIN_CHAIN = 0.75;
    private static final double STAR_STAR = 0.75;
    private static final double HUB_MULTIHUB = 0.5;
    private static final double DENSE_SEMICLIQUE = 0.45;

    private Vertex supercycle;
    private Map<String, Vertex> comps;
    private List<Edge> edges;
    private int order;
    private int size;
    private double backref;
    private String vertexType;
    private String edgesIn;
    private String edgesOut;
    private String depLabel;
    private EdgeMaps edgeMaps;
    private ExTimeLogger exTimeLogger;

    private Map<Vertex, Set<Vertex>> friends;


    public SuperCycleShapeClassifier(Vertex supercycle, Map<String, Vertex> comps, List<Edge> edges,
                                     int order, int size, String vertexType, EdgeMaps edgeMaps, ExTimeLogger exTimeLogger)
    {
        this.supercycle = supercycle;
        this.comps = comps;
        this.edges = edges;
        this.order = order;
        this.size = size;
        this.vertexType = vertexType;
        friends = new HashMap<>();
        this.edgeMaps = edgeMaps;
        this.exTimeLogger = exTimeLogger;
        this.backref = backref();

        switch (vertexType)
        {
            case GraphBuilder.CLASS:
            default:
                edgesIn = GraphBuilder.PROPERTY_FANIN;
                edgesOut = GraphBuilder.PROPERTY_FANOUT;
                depLabel = GraphBuilder.LBL_CLASS_DEP;
                break;
            case GraphBuilder.PACKAGE:
                edgesIn = GraphBuilder.PROPERTY_CA;
                edgesOut = GraphBuilder.PROPERTY_NUM_TOTAL_DEPENDENCIES;
                depLabel = GraphBuilder.LBL_PACK_DEP;
                break;
        }
    }

    public String classifyShape()
    {
        if (order == ORDER_TINY)
        {
            return GraphBuilder.TINY;
        }
        if (order >= ORDER_ALWAYS_MULTIHUB)
        {
            return GraphBuilder.MULTI_HUB;
        }
        if (ratio() >= RATIO_CYCLE)
        {
            return GraphBuilder.CIRCLE;
        }
        if (backref >= BACKREF_CLIQUE)
        {
            if (dense() >= DENSE_CLIQUE)
            {
                return GraphBuilder.CLIQUE;
            }
            if (chain() >= CHAIN_CHAIN)
            {
                return GraphBuilder.CHAIN;
            }
            if (star() >= STAR_STAR)
            {
                return GraphBuilder.STAR;
            }
        }
        if (hub() >= HUB_MULTIHUB)
        {
            return GraphBuilder.MULTI_HUB;
        }
        if (dense() >= DENSE_SEMICLIQUE)
        {
            return GraphBuilder.SEMI_CLIQUE;
        }
        return GraphBuilder.UNKNOWN;
    }

    private double ratio()
    {
        return (double) order / size;
    }

    private double backref()
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_BACKREF : ETLE.Event.PACK_CD_SHAPES_BACKREF);
        int backrefCount = 0;
        for (Edge edge : edges)
        {
            Vertex inVertex1 = edge.inVertex();
            Vertex outVertex1 = edge.outVertex();
            if (edgeMaps.existEdge(edge.label(), inVertex1, outVertex1) != null)
            {
                backrefCount++;
                addToFriends(inVertex1, outVertex1);
            }
        }
        double backrefShare = (double) backrefCount * 2 / size;
        supercycle.property(GraphBuilder.PROPERTY_BACKREF_SHARE, backrefShare);
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_BACKREF : ETLE.Event.PACK_CD_SHAPES_BACKREF);
        return backrefShare;
    }

    private void addToFriends(Vertex vertex1, Vertex vertex2)
    {
        addToFriendsCore(vertex1, vertex2);
        addToFriendsCore(vertex2, vertex1);
    }

    private void addToFriendsCore(Vertex vertexA, Vertex vertexB)
    {
        Set<Vertex> friendSet;
        boolean setInit = friends.containsKey(vertexA);
        friendSet = setInit ? friends.get(vertexA) : new HashSet<>();
        friendSet.add(vertexB);
        friends.put(vertexA, friendSet);
    }

    private double dense()
    {
        return (double) (size - order) / (order * order - (order + order));
    }

    private double chain()
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_CHAIN : ETLE.Event.PACK_CD_SHAPES_CHAIN);
        double result = (double) Math.min(friendCount(), order - CHAIN_FRIENDS) / (order - CHAIN_FRIENDS);
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_CHAIN : ETLE.Event.PACK_CD_SHAPES_CHAIN);
        return result;
    }

    private int friendCount()
    {
        int friendCount = 0;
        for (Vertex vertex : friends.keySet())
        {
            int localFriends = friends.get(vertex).size();
            if (localFriends == CHAIN_FRIENDS)
            {
                friendCount++;
            }
        }
        return friendCount;
    }

    private double star()
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_STAR : ETLE.Event.PACK_CD_SHAPES_STAR);
        int depsMax = 0;
        for (Vertex vertex : comps.values())
        {
            int deps = (int) vertex.value(edgesIn) + (int) vertex.value(edgesOut);
            if (deps > depsMax)
            {
                depsMax = deps;
            }
        }
        double result = (double) depsMax / size;
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_STAR : ETLE.Event.PACK_CD_SHAPES_STAR);
        return result;
    }

    private double hub()
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY : ETLE.Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY);
        BetweennessCentralityCalculator btwCentrCalc = new BetweennessCentralityCalculator(comps.values(), edgeMaps.getSupercycleEdgesByOutVertexMap(supercycle), exTimeLogger, vertexType);
        btwCentrCalc.betweennessCentrality();
        List<Double> btwCentrVals = btwCentrCalc.getBtwCentrVals();

        //BetweennessCentralityCalculatorKadabra btwCentrCalc = new BetweennessCentralityCalculatorKadabra(new ArrayList<>(comps.values()),edgeMaps.getSupercycleEdgesByOutVertexMap(supercycle));
        //Collection<Double> btwCentrVals = btwCentrCalc.getAllCentralities().values();

        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY : ETLE.Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY);
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_HUB_GINI : ETLE.Event.PACK_CD_SHAPES_HUB_GINI);
        double result = gini(btwCentrVals);
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.CLASS_CD_SHAPES_HUB_GINI : ETLE.Event.PACK_CD_SHAPES_HUB_GINI);
        return result;
    }

    // From https://stackoverflow.com/a/60538128 (modified to prevent division by zero).
    private double gini(Collection<Double> values)
    {
        double sumOfDifference = values.stream()
            .flatMapToDouble(v1 -> values.stream().mapToDouble(v2 -> Math.abs(v1 - v2))).sum();
        if (sumOfDifference == 0)
        {
            return 0;
        }
        double mean = values.stream().mapToDouble(v -> v).average().getAsDouble();
        return sumOfDifference / (2 * values.size() * values.size() * mean);
    }


}
