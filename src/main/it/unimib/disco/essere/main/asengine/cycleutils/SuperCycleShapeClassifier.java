package it.unimib.disco.essere.main.asengine.cycleutils;

import it.unimib.disco.essere.main.asengine.alg.BetweennessCentralityCalculator;
import it.unimib.disco.essere.main.graphmanager.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import java.util.*;

// From Al-Mutawa: On The Shape of Circular Dependencies in Java Programs
public class SuperCycleShapeClassifier
{
    private static final int CHAIN_FRIENDS = 2;
    private static final int ORDER_TINY = 2;
    private static final double RATIO_CYCLE = 0.75;
    private static final double BACKREF_CLIQUE = 0.75;
    private static final double DENSE_CLIQUE = 0.75;
    private static final double CHAIN_CHAIN = 0.75;
    private static final double STAR_STAR = 0.75;
    private static final double HUB_MULTIHUB = 0.5;
    private static final double DENSE_SEMICLIQUE = 0.45;

    private Set<Vertex> comps;
    private List<Edge> edges;
    private int order;
    private int size;
    private String vertexType;
    private String edgesIn;
    private String edgesOut;
    private String dependencyLabel;

    private Map<Vertex, Set<Vertex>> friends;

    public SuperCycleShapeClassifier(Set<Vertex> comps, List<Edge> edges, int order, int size, String vertexType)
    {
        this.comps = comps;
        this.edges = edges;
        this.order = order;
        this.size = size;
        this.vertexType = vertexType;
        friends = new HashMap<>();
        switch (vertexType)
        {
            case GraphBuilder.CLASS:
            default:
                edgesIn = GraphBuilder.PROPERTY_FANIN;
                edgesOut = GraphBuilder.PROPERTY_FANOUT;
                dependencyLabel = PropertyEdge.LABEL_CLASS_DEPENDENCY.toString();
                break;
            case GraphBuilder.PACKAGE:
                edgesIn = GraphBuilder.PROPERTY_CA;
                edgesOut = GraphBuilder.PROPERTY_NUM_TOTAL_DEPENDENCIES;
                dependencyLabel = GraphBuilder.LABEL_PACKAGE_AFFERENCE;
                break;
        }
    }

    public String classifyShape()
    {
        if (order == ORDER_TINY)
        {
            return GraphBuilder.TINY;
        }
        if (ratio() >= RATIO_CYCLE)
        {
            return GraphBuilder.CIRCLE;
        }
        if (backref() >= BACKREF_CLIQUE)
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
        int backrefCount = 0;
        for (int i = 0; i < size; i++)
        {
            Edge edge1 = edges.get(i);
            Vertex inVertex1 = edge1.inVertex();
            Vertex outVertex1 = edge1.outVertex();
            for (int j = i + 1; j < size; j++)
            {
                Edge edge2 = edges.get(j);
                Vertex inVertex2 = edge2.inVertex();
                Vertex outVertex2 = edge2.outVertex();
                if (inVertex1.id().equals(outVertex2.id()) && outVertex1.id().equals(inVertex2.id()))
                {
                    backrefCount++;
                    addToFriends(inVertex1, outVertex1);
                    break;
                }
            }
        }
        return (double) backrefCount * 2 / size;
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
        return (double) Math.min(friendCount(), order - CHAIN_FRIENDS) / (order - CHAIN_FRIENDS);
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
        int depsMax = 0;
        for (Vertex vertex : comps)
        {
            int deps = (int) vertex.value(edgesIn) + (int) vertex.value(edgesOut);
            if (deps > depsMax)
            {
                depsMax = deps;
            }
        }
        return (double) depsMax / size;
    }

    private double hub()
    {
        Graph cycleGraph = buildCycleGraph();
        List<Double> btwCentrVals = new ArrayList<>();
        BetweennessCentralityCalculator btwCentrCalc = new BetweennessCentralityCalculator();
        btwCentrCalc.betweennessCentrality(cycleGraph, order);
        Iterator<Vertex> vertices = cycleGraph.vertices();
        while (vertices.hasNext())
        {
            Vertex vertex = vertices.next();
            double btwCentr = vertex.value(BetweennessCentralityCalculator.PROPERTY_BETWEENNESS_CENTRALITY);
            btwCentrVals.add(btwCentr);
        }
        return gini(btwCentrVals);
    }

    // From https://stackoverflow.com/a/60538128 (modified to prevent division by zero).
    private double gini(List<Double> values)
    {
        double sumOfDifference = values.stream()
                .flatMapToDouble(v1 -> values.stream().mapToDouble(v2 -> Math.abs(v1 - v2))).sum();
        if(sumOfDifference==0)
        {
            return 0;
        }
        double mean = values.stream().mapToDouble(v -> v).average().getAsDouble();
        return sumOfDifference / (2 * values.size() * values.size() * mean);
    }

    private Graph buildCycleGraph()
    {
        Map<Object, Integer> outDepsCounts = new HashMap<>();
        Graph cycleGraph = TinkerGraph.open();
        for (Vertex vertex : comps)
        {
            String vertexName = vertex.value(GraphBuilder.PROPERTY_NAME);
            Vertex cycleGraphVert = cycleGraph.addVertex(T.label, vertexType, GraphBuilder.PROPERTY_NAME, vertexName);
            outDepsCounts.put(cycleGraphVert.id(), 0);
        }
        for (Edge edge : edges)
        {
            String outVertexName = edge.outVertex().value(GraphBuilder.PROPERTY_NAME);
            Vertex outVertex = GraphUtils.findVertex(cycleGraph, outVertexName, vertexType);
            String inVertexName = edge.inVertex().value(GraphBuilder.PROPERTY_NAME);
            Vertex inVertex = GraphUtils.findVertex(cycleGraph, inVertexName, vertexType);
            assert outVertex != null;
            outVertex.addEdge(dependencyLabel, inVertex);
            Object id = outVertex.id();
            outDepsCounts.put(id, outDepsCounts.get(id) + 1);
        }
        Iterator<Vertex> cycleGraphVerts = cycleGraph.vertices();
        while (cycleGraphVerts.hasNext())
        {
            Vertex vertex = cycleGraphVerts.next();
            vertex.property(edgesOut, outDepsCounts.get(vertex.id()));
        }
        return cycleGraph;
    }


}
