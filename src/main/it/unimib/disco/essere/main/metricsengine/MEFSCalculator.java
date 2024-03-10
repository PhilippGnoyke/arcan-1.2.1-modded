package it.unimib.disco.essere.main.metricsengine;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

// Calculates mEFS from:
// Eades, P., Lin, X., & Smyth, W. F. (1993). A fast and effective heuristic for the feedback arc set problem. Information Processing Letters, 47(6), 319–323.
public class MEFSCalculator
{
    private Set<Vertex> remainingComps;
    private final List<Edge> edges;
    private List<Vertex> sequence1;
    private List<Vertex> sequence2;
    private Set<Edge> edgeFeedbackSet;
    private Set<Edge> edgeFeedbackSetWOTinys;
    private int maxMEFSSize;

    private Multimap<Vertex, Vertex> incoming;
    private Multimap<Vertex, Vertex> outgoing;
    private Multimap<Vertex, Vertex> outgoing2;

    public MEFSCalculator(Set<Vertex> comps, List<Edge> edges)
    {
        this.remainingComps = new HashSet<>(comps);
        this.edges = edges;
        this.sequence1 = new ArrayList<>();
        this.sequence2 = new ArrayList<>();
        this.maxMEFSSize = edges.size() - comps.size() + 1;
        this.edgeFeedbackSet = new HashSet<>(maxMEFSSize);
        this.edgeFeedbackSetWOTinys = new HashSet<>(maxMEFSSize);
        initMaps();
        calcMFES();
    }

    public Set<Edge> getEdgeFeedbackSet()
    {
        return edgeFeedbackSet;
    }

    public Set<Edge> getEdgeFeedbackSetWOTinys()
    {
        return edgeFeedbackSetWOTinys;
    }

    public int getMEFSSize()
    {
        return edgeFeedbackSet.size();
    }

    public double getRelativeMEFSSize()
    {
        return (double) edgeFeedbackSet.size() / edges.size();
    }

    public int getMEFSSizeWOTinys()
    {
        return edgeFeedbackSetWOTinys.size();
    }

    public double getRelativeMEFSSizeWOTinys()
    {
        return (double) edgeFeedbackSetWOTinys.size() / edges.size();
    }

    public double getMEFSSizeWOTinysReduction()
    {
        return (double) (getMEFSSize() - getMEFSSizeWOTinys()) / getMEFSSize();
    }

    private void initMaps()
    {
        incoming = HashMultimap.create();
        outgoing = HashMultimap.create();
        for (Edge edge : edges)
        {
            Vertex inVertex = edge.inVertex();
            Vertex outVertex = edge.outVertex();
            incoming.put(inVertex, outVertex);
            outgoing.put(outVertex, inVertex);
        }
        outgoing2 = HashMultimap.create(outgoing);
    }

    private void calcMFES()
    {
        while (remainingComps.size() > 0)
        {
            checkForSink();
            checkForSource();
            removeMaxDelta();
        }
        determineBackwardEdges();
    }

    private void determineBackwardEdges()
    {
        Map<Vertex, Integer> vertexIndexes = buildVertexIndexes();
        HashMultimap<Vertex,Edge> incomingLeftEdges = HashMultimap.create();
        HashMultimap<Vertex,Edge> outgoingLeftEdges = HashMultimap.create();
        for (Edge edge : edges)
        {
            Vertex outVertex = edge.outVertex();
            Vertex inVertex = edge.inVertex();
            boolean isBackwardsEdge = vertexIndexes.get(outVertex) > vertexIndexes.get(inVertex);
            if (isBackwardsEdge)
            {
                edgeFeedbackSet.add(edge);
                updateEFSWOTinysData(incomingLeftEdges, outgoingLeftEdges, edge, outVertex, inVertex);
            }
        }
        updateSetWOTinysWithUnavoidableBackrefs(vertexIndexes, incomingLeftEdges, outgoingLeftEdges);
    }

    private void updateEFSWOTinysData(HashMultimap<Vertex, Edge> incomingLeftEdges, HashMultimap<Vertex,
        Edge> outgoingLeftEdges, Edge edge, Vertex outVertex, Vertex inVertex)
    {
        if (!outgoing2.containsEntry(inVertex, outVertex))
        {
            edgeFeedbackSetWOTinys.add(edge);
        }
        else
        {
            incomingLeftEdges.put(inVertex, edge);
            outgoingLeftEdges.put(outVertex, edge);
        }
    }

    // Not all backrefs can be kept to only have tiny supercycles left after removing all edges in the mEFS
    // Strategy: Every vertex can only have one left-facing edge in the mEFS
    // All additional incoming and outgoing left-facing edges per vertex lead to supercycles of higher orders
    // Thus, they must be removed as well
    private void updateSetWOTinysWithUnavoidableBackrefs(Map<Vertex, Integer> vertexIndexes, HashMultimap<Vertex, Edge> incomingLeftEdges, HashMultimap<Vertex, Edge> outgoingLeftEdges)
    {
        for (Vertex vertex : vertexIndexes.keySet())
        {
            Set<Edge> incoming = incomingLeftEdges.get(vertex);
            Set<Edge> outgoing = outgoingLeftEdges.get(vertex);
            while (incoming.size()+outgoing.size()>1)
            {
                Iterator<Edge> iterator = incoming.iterator();
                if(!iterator.hasNext()) { iterator = outgoing.iterator(); }
                edgeFeedbackSetWOTinys.add(iterator.next());
                iterator.remove();
            }
        }
    }

    private Map<Vertex, Integer> buildVertexIndexes()
    {
        Map<Vertex, Integer> vertexIndexes = new HashMap<>();
        int vertexIndex;
        for (vertexIndex = 0; vertexIndex < sequence1.size(); vertexIndex++)
        {
            vertexIndexes.put(sequence1.get(vertexIndex), vertexIndex);
        }
        for (vertexIndex = sequence1.size() + sequence2.size() - 1; vertexIndex >= sequence1.size(); vertexIndex--)
        {
            vertexIndexes.put(sequence2.get(vertexIndex - sequence1.size()), vertexIndex);
        }
        return vertexIndexes;
    }

    private void checkForSink()
    {
        Vertex sink = findSink();
        while (sink != null)
        {
            sequence2.add(sink);
            removeVertex(sink);
            sink = findSink();
        }
    }

    private void checkForSource()
    {
        Vertex source = findSource();
        while (source != null)
        {
            sequence1.add(source);
            removeVertex(source);
            source = findSource();
        }
    }

    private void removeVertex(Vertex vertex)
    {
        remainingComps.remove(vertex);
        removeVertexFromMultimap(incoming, vertex);
        removeVertexFromMultimap(outgoing, vertex);
    }


    private void removeVertexFromMultimap(Multimap<Vertex, Vertex> multimap, Vertex toBeRemoved)
    {
        Set<Vertex> keys = new HashSet<>(multimap.keySet());
        for (Vertex key : keys)
        {
            Set<Vertex> vertexSet = new HashSet<>(multimap.get(key));
            vertexSet.remove(toBeRemoved);
            multimap.replaceValues(key, vertexSet);
        }
    }

    private Vertex findSink()
    {
        for (Vertex vertex : remainingComps)
        {
            if (!outgoing.containsKey(vertex) && incoming.containsKey(vertex))
            {
                return vertex;
            }
        }
        return null;
    }

    private Vertex findSource()
    {
        for (Vertex vertex : remainingComps)
        {
            if (!incoming.containsKey(vertex) && outgoing.containsKey(vertex))
            {
                return vertex;
            }
        }
        return null;
    }

    private void removeMaxDelta()
    {
        Vertex maxDeltaVertex = getMaxDeltaVertex();
        sequence1.add(maxDeltaVertex);
        removeVertex(maxDeltaVertex);
    }

    private Vertex getMaxDeltaVertex()
    {
        Vertex maxVertex = null;
        int maxDelta = 0;
        for (Vertex vertex : remainingComps)
        {
            int delta = outgoing.get(vertex).size() - incoming.get(vertex).size();
            if (delta >= maxDelta)
            {
                maxDelta = delta;
                maxVertex = vertex;
            }
        }
        return maxVertex;
    }
}
