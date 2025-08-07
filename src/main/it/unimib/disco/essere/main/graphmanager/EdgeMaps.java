package it.unimib.disco.essere.main.graphmanager;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import java.util.*;

//Modded
public class EdgeMaps
{
    private Map<String, List<Edge>> edgeMapLabel;
    private Map<EdgeMapsHashKey, List<Edge>> edgeMapOutVertexNLabel;
    private Map<EdgeMapsHashKey, List<Edge>> edgeMapInVertexNLabel;
    private Map<EdgeMapsHashKey, Edge> edgeMapOutNInVertexNLabel;
    private Map<Vertex,Map<Vertex,List<Edge>>> edgeMapSupercycleOutVertex;
    private Map<Vertex,List<Edge>> edgeMapSupercycle;

    public EdgeMaps()
    {
        edgeMapLabel = new HashMap<>();
        edgeMapOutVertexNLabel = new HashMap<>();
        edgeMapInVertexNLabel = new HashMap<>();
        edgeMapOutNInVertexNLabel = new HashMap<>();
        edgeMapSupercycleOutVertex = new HashMap<>();
        edgeMapSupercycle  = new HashMap<>();
    }

    // Only use for backwards-compatibility outside of runtime environments
    public EdgeMaps(Graph graph)
    {
        this();
        for (Iterator<Edge> edges = graph.edges(); edges.hasNext(); ) {
            Edge edge = edges.next();
            Vertex outVertex = edge.outVertex();
            Vertex inVertex = edge.inVertex();
            String label = edge.label();
            addEdgeToEdgeMaps(edge,outVertex,inVertex,label);
        }
    }



    public void addEdgeToEdgeMaps(Edge edge, Vertex outVertex, Vertex inVertex, String label)
    {
        addToEdgeMapLabel(edge, label);
        addToEdgeMapVertexNLabel(edge, outVertex, label, edgeMapOutVertexNLabel);
        addToEdgeMapVertexNLabel(edge, inVertex, label, edgeMapInVertexNLabel);
        addToEdgeMapOutNInVertexNLabel(edge, outVertex, inVertex, label);
    }

    private void addToEdgeMapOutNInVertexNLabel(Edge edge, Vertex outVertex, Vertex inVertex, String label)
    {
        EdgeMapsHashKey key3 = hashOutNInVertexNLabel(outVertex, inVertex, label);
        edgeMapOutNInVertexNLabel.put(key3, edge);
    }

    private void addToEdgeMapVertexNLabel(Edge edge, Vertex vertex, String label, Map<EdgeMapsHashKey, List<Edge>> edgeMapVertexNLabel)
    {
        EdgeMapsHashKey key = hashVertexNLabel(vertex, label);
        if (!edgeMapVertexNLabel.containsKey(key))
        {
            edgeMapVertexNLabel.put(key, new ArrayList<>());
        }
        edgeMapVertexNLabel.get(key).add(edge);
    }

    private void addToEdgeMapLabel(Edge edge, String label)
    {
        if (!edgeMapLabel.containsKey(label))
        {
            edgeMapLabel.put(label, new ArrayList<>());
        }
        edgeMapLabel.get(label).add(edge);
    }

    public static EdgeMapsHashKey hashVertexNLabel(Vertex outVertex, String label)
    {
        return new EdgeMapsHashKey(outVertex.id().toString(),outVertex.id().toString(), label);
    }

    public static EdgeMapsHashKey hashOutNInVertexNLabel(Vertex outVertex, Vertex inVertex, String label)
    {
        return new EdgeMapsHashKey(outVertex.id().toString(),inVertex.id().toString(), label);
    }

    public List<Edge> findEdgesByLabel(String label)
    {
        return edgeMapLabel.get(label);
    }

    public Edge existEdge(String label, Vertex outVertex, Vertex inVertex)
    {
        return edgeMapOutNInVertexNLabel.get(hashOutNInVertexNLabel(outVertex, inVertex, label));

    }

    public List<Edge> getEdgesByOutVertex(String label, Vertex vertex)
    {
        return edgeMapOutVertexNLabel.get(hashVertexNLabel(vertex, label));
    }

    public List<Edge> getEdgesByInVertex(String label, Vertex vertex)
    {
        return edgeMapInVertexNLabel.get(hashVertexNLabel(vertex, label));
    }

    public List<Edge> allEdgesBetweenVertices(List<Vertex> vertices, String label)
    {
        List<Edge> edges = new ArrayList<>();
        for (Vertex outVertex : vertices)
        {
            for (Vertex inVertex : vertices)
            {
                if (outVertex != inVertex)
                {
                    Edge edge = edgeMapOutNInVertexNLabel.get(hashOutNInVertexNLabel(outVertex, inVertex, label));
                    if (edge != null)
                    {
                        edges.add(edge);
                    }
                }
            }
        }
        return edges;
    }

    public void initSupercycle(Vertex supercycle, List<Vertex> comps, String depLabel)
    {
        Map<Vertex,List<Edge>> localMap = new HashMap<>();
        List<Edge> edges = new ArrayList<>();

        for (Vertex outVertex : comps)
        {
            for (Vertex inVertex : comps)
            {
                if (outVertex != inVertex)
                {
                    Edge edge = edgeMapOutNInVertexNLabel.get(hashOutNInVertexNLabel(outVertex, inVertex, depLabel));
                    if (edge != null)
                    {
                        if (!localMap.containsKey(outVertex))
                        {
                            localMap.put(outVertex, new ArrayList<>());
                        }
                        localMap.get(outVertex).add(edge);
                        edges.add(edge);
                    }
                }
            }
        }
        edgeMapSupercycleOutVertex.put(supercycle,localMap);
        edgeMapSupercycle.put(supercycle,edges);
    }

    public List<Edge> getSupercycleEdgesByOutVertex(Vertex supercycle, Vertex outVertex)
    {
        return edgeMapSupercycleOutVertex.get(supercycle).get(outVertex);
    }

    public Map<Vertex,List<Edge>> getSupercycleEdgesByOutVertexMap(Vertex supercycle)
    {
        return edgeMapSupercycleOutVertex.get(supercycle);
    }

    public List<Edge> getSuperCycleEdges(Vertex supercycle)
    {
        return edgeMapSupercycle.get(supercycle);
    }

}

