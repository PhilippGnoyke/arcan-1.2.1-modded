package it.unimib.disco.essere.main.metricsengine;

import it.unimib.disco.essere.main.graphmanager.EdgeMaps;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

public class MEFSCalculator
{
    private final Set<Vertex> remainingComps;
    private final List<Edge> edges;

    private final List<Vertex> sequence1 = new ArrayList<>();
    private final List<Vertex> sequence2 = new ArrayList<>();

    private final Set<Edge> edgeFeedbackSet;
    private final Set<Edge> edgeFeedbackSetWOTinys;

    private Multimap<Vertex, Vertex> incoming;
    private Multimap<Vertex, Vertex> outgoing;
    private Multimap<Vertex, Vertex> outgoing2;

    private final Map<Vertex, Integer> indegrees = new HashMap<>();
    private final Map<Vertex, Integer> outdegrees = new HashMap<>();

    private final PriorityQueue<Vertex> deltaQueue;

    public MEFSCalculator(Set<Vertex> comps, List<Edge> edges)
    {
        this.remainingComps = new HashSet<>(comps);
        this.edges = edges;

        this.edgeFeedbackSet = new HashSet<>();
        this.edgeFeedbackSetWOTinys = new HashSet<>();

        this.deltaQueue = new PriorityQueue<>(Comparator.<Vertex>comparingInt(this::getDelta).reversed());

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
            Vertex out = edge.outVertex();
            Vertex in = edge.inVertex();

            if (!remainingComps.contains(out) || !remainingComps.contains(in)) { continue; }

            incoming.put(in, out);
            outgoing.put(out, in);

            indegrees.put(in, indegrees.getOrDefault(in, 0) + 1);
            outdegrees.put(out, outdegrees.getOrDefault(out, 0) + 1);
        }

        outgoing2 = HashMultimap.create(outgoing);
        deltaQueue.addAll(remainingComps);
    }

    private int getDelta(Vertex v)
    {
        return outdegrees.getOrDefault(v, 0) - indegrees.getOrDefault(v, 0);
    }

    private void calcMFES()
    {
        while (!remainingComps.isEmpty())
        {
            boolean changed = true;

            while (changed)
            {
                changed = checkForSink() || checkForSource();
            }

            if (!remainingComps.isEmpty())
            {
                removeMaxDelta();
            }
        }

        determineBackwardEdges();
    }

    private boolean checkForSink()
    {
        for (Vertex vertex : remainingComps)
        {
            if (!outgoing.containsKey(vertex) && incoming.containsKey(vertex))
            {
                sequence2.add(vertex);
                removeVertex(vertex);
                return true;
            }
        }
        return false;
    }

    private boolean checkForSource()
    {
        for (Vertex vertex : remainingComps)
        {
            if (!incoming.containsKey(vertex) && outgoing.containsKey(vertex))
            {
                sequence1.add(vertex);
                removeVertex(vertex);
                return true;
            }
        }
        return false;
    }

    private void removeMaxDelta()
    {
        Vertex maxVertex = null;
        int maxDelta = Integer.MIN_VALUE;

        for (Vertex vertex : remainingComps)
        {
            int delta = getDelta(vertex);
            if (delta > maxDelta)
            {
                maxDelta = delta;
                maxVertex = vertex;
            }
        }

        if (maxVertex != null)
        {
            sequence1.add(maxVertex);
            removeVertex(maxVertex);
        }
    }

    private void removeVertex(Vertex vertex)
    {
        remainingComps.remove(vertex);
        deltaQueue.remove(vertex);

        for (Vertex neighbor : new HashSet<>(incoming.get(vertex)))
        {
            outgoing.remove(neighbor, vertex);
            outdegrees.put(neighbor, outdegrees.get(neighbor) - 1);
        }

        for (Vertex neighbor : new HashSet<>(outgoing.get(vertex)))
        {
            incoming.remove(neighbor, vertex);
            indegrees.put(neighbor, indegrees.get(neighbor) - 1);
        }

        incoming.removeAll(vertex);
        outgoing.removeAll(vertex);
    }

    private void determineBackwardEdges()
    {
        Map<Vertex, Integer> vertexIndexes = buildVertexIndexes();
        Multimap<Vertex, Edge> incomingLeftEdges = HashMultimap.create();
        Multimap<Vertex, Edge> outgoingLeftEdges = HashMultimap.create();

        for (Edge edge : edges)
        {
            Vertex out = edge.outVertex();
            Vertex in = edge.inVertex();

            if (vertexIndexes.get(out) > vertexIndexes.get(in))
            {
                edgeFeedbackSet.add(edge);

                if (!outgoing2.containsEntry(in, out))
                {
                    edgeFeedbackSetWOTinys.add(edge);
                }
                else
                {
                    incomingLeftEdges.put(in, edge);
                    outgoingLeftEdges.put(out, edge);
                }
            }
        }

        updateSetWOTinysWithUnavoidableBackrefs(vertexIndexes, incomingLeftEdges, outgoingLeftEdges);
    }

    private void updateSetWOTinysWithUnavoidableBackrefs(Map<Vertex, Integer> vertexIndexes,
                                                         Multimap<Vertex, Edge> incomingLeftEdges,
                                                         Multimap<Vertex, Edge> outgoingLeftEdges)
    {
        for (Vertex vertex : vertexIndexes.keySet())
        {
            Set<Edge> in = new HashSet<>(incomingLeftEdges.get(vertex));
            Set<Edge> out = new HashSet<>(outgoingLeftEdges.get(vertex));

            while (in.size() + out.size() > 1)
            {
                Iterator<Edge> it = in.isEmpty() ? out.iterator() : in.iterator();
                Edge toRemove = it.next();
                edgeFeedbackSetWOTinys.add(toRemove);
                it.remove();
            }
        }
    }

    private Map<Vertex, Integer> buildVertexIndexes()
    {
        Map<Vertex, Integer> indexes = new HashMap<>();
        int i = 0;
        for (Vertex v : sequence1)
        {
            indexes.put(v, i++);
        }
        for (int j = sequence2.size() - 1; j >= 0; j--)
        {
            indexes.put(sequence2.get(j), i++);
        }
        return indexes;
    }
}
