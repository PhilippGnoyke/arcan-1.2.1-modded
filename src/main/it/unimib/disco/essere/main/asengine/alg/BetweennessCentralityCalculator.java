package it.unimib.disco.essere.main.asengine.alg;

// Modded from here

// Original implementation (modified to fit with Tinkerpop/Gremlin):
// http://www.javased.com/index.php?source_dir=gs-algo/src/org/graphstream/algorithm/BetweennessCentrality.java


/*
 * Copyright 2006 - 2012
 *      Stefan Balev       <stefan.balev@graphstream-project.org>
 *      Julien Baudry <julien.baudry@graphstream-project.org>
 *      Antoine Dutot <antoine.dutot@graphstream-project.org>
 *      Yoann Pigné <yoann.pigne@graphstream-project.org>
 *      Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 *
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 *
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */

import it.unimib.disco.essere.main.ETLE;
import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.graphmanager.EdgeMaps;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.neo4j.cypher.internal.compiler.v1_9.commands.Has;

import java.util.*;


/**
 * Compute the "betweenness" centrality of each vertex of a given graph.
 *
 * <p>
 * The betweenness centrality counts how many shortest paths between each
 * pair of vertices of the graph pass by a vertex. It does it for all vertices of
 * the graph.
 * </p>
 *
 * <h2>Usage</h2>
 *
 * <p>
 * This algorithm, by default, stores the centrality values for each vertex inside
 * the "Cb" attribute. You can change this attribute name at construction time.
 * </p>
 *
 * <p>
 * <b>This algorithm does not accept multi-graphs (p-graphs with p>1) yet.</b>
 * </p>
 *
 * <p>
 * The result of the computation is stored on each vertex inside the btw-centrality
 * attribute.
 * </p>
 *
 *
 * <h2>Complexity</h2>
 *
 * <p>
 * By default the algorithm performs on a graph considered as not weighted with
 * complexity O(nm).
 * </p>
 *
 *
 * <h2>Reference</h2>
 *
 * <p>
 * This is based on the algorithm described in "A Faster Algorithm for
 * Betweenness Centrality", Ulrik Brandes, Journal of Mathematical Sociology,
 * 2001, and in
 * "On variants of shortest-path betweenness centrality and their generic computation",
 * of the same author, 2008.
 * </p>
 *
 * @reference A Faster Algorithm for Betweenness Centrality, Ulrik Brandes,
 * Journal of Mathematical Sociology, 2001, 25:2, pp. 163 - 177",
 * "DOI: 10.1080/0022250X.2001.9990249"
 * @reference On variants of shortest-path betweenness centrality and their generic computation,
 * Ulrik Brandes, Social Networks, vol 30:2", pp. 136 - 145, 2008,
 * issn 0378-8733, "DOI: 10.1016/j.socnet.2007.11.001".
 */
public class BetweennessCentralityCalculator
{
    private final static double INFINITY = 1000000.0;

    private Collection<Vertex> comps;
    private Map<Vertex, List<Edge>> edges;
    private ExTimeLogger exTimeLogger;
    private String vertexType;
    private Map<Vertex, Double> sigmas;
    private Map<Vertex, Double> deltas;
    private Map<Vertex, Double> distances;
    private Map<Vertex, Double> centralities;
    private Map<Vertex, Set<Vertex>> preds;

    public BetweennessCentralityCalculator(Collection<Vertex> comps, Map<Vertex, List<Edge>> edges, ExTimeLogger exTimeLogger, String vertexType)
    {
        this.comps = comps;
        this.edges = edges;
        this.exTimeLogger = exTimeLogger;
        this.vertexType = vertexType;
        int mapSize = comps.size() * 2;
        sigmas = new HashMap<>(mapSize);
        deltas = new HashMap<>(mapSize);
        distances = new HashMap<>(mapSize);
        centralities = new HashMap<>(mapSize);
        preds = new HashMap<>(mapSize);
    }


    /**
     * Compute the betweenness centrality on the given graph for each vertex and
     * eventually edges.
     */
    public void betweennessCentrality()
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.BC_CLASS_INIT_VERTICES : ETLE.Event.BC_PACK_INIT_VERTICES);
        initAllVertices();
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.BC_CLASS_INIT_VERTICES : ETLE.Event.BC_PACK_INIT_VERTICES);

        for (Vertex s : comps)
        {
            PriorityQueue<Vertex> queue = simpleExplore(s);

            // The really new things in the Brandes algorithm are here:
            // Accumulation phase:
            exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
                ETLE.Event.BC_CLASS_EMPTY_QUEUE : ETLE.Event.BC_PACK_EMPTY_QUEUE);
            while (!queue.isEmpty())
            {
                Vertex w = queue.poll();

                for (Vertex v : predecessorsOf(w))
                {
                    double c = ((sigma(v) / sigma(w)) * (1.0 + delta(w)));
                    setDelta(v, delta(v) + c);
                }
                if (w != s)
                {
                    setCentrality(w, centrality(w) + delta(w));
                }
            }
            exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
                ETLE.Event.BC_CLASS_EMPTY_QUEUE : ETLE.Event.BC_PACK_EMPTY_QUEUE);
        }
    }

    /**
     * Compute single-source multiple-targets shortest paths on an unweighted
     * graph.
     *
     * @param source The source vertex.
     * @return A priority queue of explored vertices with sigma values usable to
     * compute the centrality.
     */
    private PriorityQueue<Vertex> simpleExplore(Vertex source)
    {
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.BC_CLASS_SE_INIT_QUEUES : ETLE.Event.BC_PACK_SE_INIT_QUEUES);
        Queue<Vertex> queue = new ArrayDeque<>();
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(comps.size(), new BrandesvertexComparatorLargerFirst());
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.BC_CLASS_SE_INIT_QUEUES : ETLE.Event.BC_PACK_SE_INIT_QUEUES);
        exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.BC_CLASS_SE_SETUP_VERTICES : ETLE.Event.BC_PACK_SE_SETUP_VERTICES);
        setupAllVertices();
        queue.add(source);
        setSigma(source, 1.0);
        setDistance(source, 0.0);
        exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
            ETLE.Event.BC_CLASS_SE_SETUP_VERTICES : ETLE.Event.BC_PACK_SE_SETUP_VERTICES);

        while (!queue.isEmpty())
        {
            exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
                ETLE.Event.BC_CLASS_SE_PROCESS_QUEUE : ETLE.Event.BC_PACK_SE_PROCESS_QUEUE);
            Vertex v = queue.remove();
            priorityQueue.add(v);
            exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
                ETLE.Event.BC_CLASS_SE_PROCESS_QUEUE : ETLE.Event.BC_PACK_SE_PROCESS_QUEUE);
            exTimeLogger.logEventStart(vertexType.equals(GraphBuilder.CLASS) ?
                ETLE.Event.BC_CLASS_SE_PROCESS_EDGES : ETLE.Event.BC_PACK_SE_PROCESS_EDGES);
            List<Edge> outEdges = edges.get(v);
            for (Edge edge : outEdges)
            {
                Vertex w = edge.inVertex();

                if (distance(w) == INFINITY)
                {
                    setDistance(w, distance(v) + 1);
                    queue.add(w);
                }

                if (distance(w) == (distance(v) + 1.0))
                {
                    setSigma(w, sigma(w) + sigma(v));
                    addToPredecessorsOf(w, v);
                }
            }
            exTimeLogger.logEventEnd(vertexType.equals(GraphBuilder.CLASS) ?
                ETLE.Event.BC_CLASS_SE_PROCESS_EDGES : ETLE.Event.BC_PACK_SE_PROCESS_EDGES);
        }
        return priorityQueue;
    }

    /**
     * The sigma value of the given vertex.
     *
     * @param vertex Extract the sigma value of this vertex.
     * @return The sigma value.
     */
    private double sigma(Vertex vertex)
    {
        return sigmas.get(vertex);
    }

    /**
     * The distance value of the given vertex.
     *
     * @param vertex Extract the distance value of this vertex.
     * @return The distance value.
     */
    private double distance(Vertex vertex)
    {
        return distances.get(vertex);
    }

    /**
     * The delta value of the given vertex.
     *
     * @param vertex Extract the delta value of this vertex.
     * @return The delta value.
     */
    private double delta(Vertex vertex)
    {
        return deltas.get(vertex);
    }

    /**
     * The centrality value of the given vertex.
     *
     * @param vertex Extract the centrality of this vertex.
     * @return The centrality value.
     */
    private double centrality(Vertex vertex)
    {
        return centralities.get(vertex);
    }

    /**
     * List of predecessors of the given vertex.
     *
     * @param vertex Extract the predecessors of this vertex.
     * @return The list of predecessors.
     */
    @SuppressWarnings("all")
    private Set<Vertex> predecessorsOf(Vertex vertex)
    {
        return preds.get(vertex);
    }

    /**
     * Set the sigma value of the given vertex.
     *
     * @param vertex The vertex to modify.
     * @param sigma  The sigma value to store on the vertex.
     */
    private void setSigma(Vertex vertex, double sigma)
    {
        sigmas.put(vertex, sigma);
    }

    /**
     * Set the distance value of the given vertex.
     *
     * @param vertex   The vertex to modify.
     * @param distance The delta value to store on the vertex.
     */
    private void setDistance(Vertex vertex, double distance)
    {
        distances.put(vertex, distance);
    }

    /**
     * Set the delta value of the given vertex.
     *
     * @param vertex The vertex to modify.
     * @param delta  The delta value to store on the vertex.
     */
    private void setDelta(Vertex vertex, double delta)
    {
        deltas.put(vertex, delta);
    }

    /**
     * Set the centrality of the given vertex.
     *
     * @param vertex     The vertex to modify.
     * @param centrality The centrality to store on the vertex.
     */
    private void setCentrality(Vertex vertex, double centrality)
    {
        centralities.put(vertex, centrality);
    }


    /**
     * Add a vertex to the predecessors of another.
     *
     * @param vertex      Modify the predecessors of this vertex.
     * @param predecessor The predecessor to add.
     */
    @SuppressWarnings("all")
    private void addToPredecessorsOf(Vertex vertex, Vertex predecessor)
    {
        preds.get(vertex).add(predecessor);
    }

    /**
     * Remove all predecessors of the given vertex.
     *
     * @param vertex Remove all predecessors of this vertex.
     */
    private void initPredecessorsOf(Vertex vertex)
    {
        HashSet<Vertex> set = new HashSet<>();
        preds.put(vertex, set);
    }

    /**
     * Remove all predecessors of the given vertex.
     *
     * @param vertex Remove all predecessors of this vertex.
     */
    private void clearPredecessorsOf(Vertex vertex)
    {
        preds.get(vertex).clear();
    }


    /**
     * Set a default centrality of 0 to all vertices.
     */
    private void initAllVertices()
    {
        for (Vertex vertex : comps)
        {
            setCentrality(vertex, 0.0);
            initPredecessorsOf(vertex);
        }
    }

    /**
     * Add a default value for attributes used during computation.
     */
    private void setupAllVertices()
    {
        for (Vertex vertex : comps)
        {
            clearPredecessorsOf(vertex);
            setSigma(vertex, 0.0);
            setDistance(vertex, INFINITY);
            setDelta(vertex, 0.0);
        }
    }

    /**
     * Increasing comparator used for priority queues.
     */
    private class BrandesvertexComparatorLargerFirst implements
        Comparator<Vertex>
    {
        public int compare(Vertex x, Vertex y)
        {
            // return (int) ( (distance(y)*1000.0) - (distance(x)*1000.0) );
            double yy = distance(y);
            double xx = distance(x);

            if (xx > yy)
            { return -1; }
            else if (xx < yy)
            { return 1; }

            return 0;
        }
    }

    public List<Double> getBtwCentrVals()
    {
        List<Double> btwCentrVals = new ArrayList<>();
        for (Vertex vertex : comps)
        {
            btwCentrVals.add(centralities.get(vertex));
        }
        return btwCentrVals;
    }


}

