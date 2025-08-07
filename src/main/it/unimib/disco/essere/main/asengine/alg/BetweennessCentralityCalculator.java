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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;


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

    /**
     * Store the centrality value in this attribute on vertices and edges.
     */
    public final static String PROPERTY_BETWEENNESS_CENTRALITY = "betweennessCentrality";

    /**
     * The predecessors.
     */
    private final static String PRED_ATTRIBUTE_NAME = "brandes.P";

    /**
     * The sigma value.
     */
    private final static String SIGMA_ATTRIBUTE_NAME = "brandes.sigma";

    /**
     * The distance value.
     */
    private final static String DIST_ATTRIBUTE_NAME = "brandes.d";

    /**
     * The delta value.
     */
    private final static String DELTA_ATTRIBUTE_NAME = "brandes.delta";

    /**
     * Compute the betweenness centrality on the given graph for each vertex and
     * eventually edges.
     */
    public void betweennessCentrality(Graph graph, int order)
    {
        initAllVertices(graph);

        Iterator<Vertex> vertices = graph.vertices();
        while (vertices.hasNext())
        {
            Vertex s = vertices.next();
            PriorityQueue<Vertex> S = simpleExplore(s, graph, order);

            // The really new things in the Brandes algorithm are here:
            // Accumulation phase:

            while (!S.isEmpty())
            {
                Vertex w = S.poll();

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
        }
    }

    /**
     * Compute single-source multiple-targets shortest paths on an unweighted
     * graph.
     *
     * @param source The source vertex.
     * @param graph  The graph.
     * @return A priority queue of explored vertices with sigma values usable to
     * compute the centrality.
     */
    private PriorityQueue<Vertex> simpleExplore(Vertex source, Graph graph, int order)
    {
        LinkedList<Vertex> Q = new LinkedList<>();
        PriorityQueue<Vertex> S = new PriorityQueue<>(order, new BrandesvertexComparatorLargerFirst());

        setupAllVertices(graph);
        Q.add(source);
        setSigma(source, 1.0);
        setDistance(source, 0.0);

        while (!Q.isEmpty())
        {
            Vertex v = Q.removeFirst();

            S.add(v);
            Iterator<? extends Edge> ww = v.edges(Direction.OUT);
            while (ww.hasNext())
            {
                Edge l = ww.next();
                Vertex w = l.inVertex();

                if (distance(w) == INFINITY)
                {
                    setDistance(w, distance(v) + 1);
                    Q.add(w);
                }

                if (distance(w) == (distance(v) + 1.0))
                {
                    setSigma(w, sigma(w) + sigma(v));
                    addToPredecessorsOf(w, v);
                }
            }
        }

        return S;
    }

    /**
     * The sigma value of the given vertex.
     *
     * @param vertex Extract the sigma value of this vertex.
     * @return The sigma value.
     */
    private double sigma(Vertex vertex)
    {
        return vertex.value(SIGMA_ATTRIBUTE_NAME);
    }

    /**
     * The distance value of the given vertex.
     *
     * @param vertex Extract the distance value of this vertex.
     * @return The distance value.
     */
    private double distance(Vertex vertex)
    {
        return vertex.value(DIST_ATTRIBUTE_NAME);
    }

    /**
     * The delta value of the given vertex.
     *
     * @param vertex Extract the delta value of this vertex.
     * @return The delta value.
     */
    private double delta(Vertex vertex)
    {
        return vertex.value(DELTA_ATTRIBUTE_NAME);
    }

    /**
     * The centrality value of the given vertex.
     *
     * @param vertex Extract the centrality of this vertex.
     * @return The centrality value.
     */
    private double centrality(Vertex vertex)
    {
        return vertex.value(PROPERTY_BETWEENNESS_CENTRALITY);
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
        return (HashSet<Vertex>) vertex.value(PRED_ATTRIBUTE_NAME);
    }

    /**
     * Set the sigma value of the given vertex.
     *
     * @param vertex The vertex to modify.
     * @param sigma  The sigma value to store on the vertex.
     */
    private void setSigma(Vertex vertex, double sigma)
    {
        vertex.property(SIGMA_ATTRIBUTE_NAME, sigma);
    }

    /**
     * Set the distance value of the given vertex.
     *
     * @param vertex   The vertex to modify.
     * @param distance The delta value to store on the vertex.
     */
    private void setDistance(Vertex vertex, double distance)
    {
        vertex.property(DIST_ATTRIBUTE_NAME, distance);
    }

    /**
     * Set the delta value of the given vertex.
     *
     * @param vertex The vertex to modify.
     * @param delta  The delta value to store on the vertex.
     */
    private void setDelta(Vertex vertex, double delta)
    {
        vertex.property(DELTA_ATTRIBUTE_NAME, delta);
    }

    /**
     * Set the centrality of the given vertex.
     *
     * @param vertex     The vertex to modify.
     * @param centrality The centrality to store on the vertex.
     */
    private void setCentrality(Vertex vertex, double centrality)
    {
        vertex.property(PROPERTY_BETWEENNESS_CENTRALITY, centrality);
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
        HashSet<Vertex> preds = (HashSet<Vertex>) vertex.value(PRED_ATTRIBUTE_NAME);
        preds.add(predecessor);
    }

    /**
     * Remove all predecessors of the given vertex.
     *
     * @param vertex Remove all predecessors of this vertex.
     */
    private void clearPredecessorsOf(Vertex vertex)
    {
        HashSet<Vertex> set = new HashSet<>();
        vertex.property(PRED_ATTRIBUTE_NAME, set);
    }

    /**
     * Set a default centrality of 0 to all vertices.
     *
     * @param graph The graph to modify.
     */
    private void initAllVertices(Graph graph)
    {
        Iterator<Vertex> vertices = graph.vertices();
        while (vertices.hasNext())
        {
            Vertex vertex = vertices.next();
            setCentrality(vertex, 0.0);
        }
    }

    /**
     * Add a default value for attributes used during computation.
     *
     * @param graph The graph to modify.
     */
    private void setupAllVertices(Graph graph)
    {
        Iterator<Vertex> vertices = graph.vertices();
        while (vertices.hasNext())
        {
            Vertex vertex = vertices.next();
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
}

