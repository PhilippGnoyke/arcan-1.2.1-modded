package it.unimib.disco.essere.main.graphmanager;

import java.util.*;

import it.unimib.disco.essere.main.metricsengine.ProjectMetricsCalculator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author Ilaria
 *
 */
public class GraphUtils {
    public static final Logger logger = LogManager.getLogger(GraphUtils.class);

    // /**
    // * Finds a node of the graph given its name if exists.
    // *
    // * @param graph
    // * @param vertexName
    // * @return the requested Vertex or null if it doesn't exists.
    // */
    // public static Vertex findVertex(Graph graph, String vertexName) {
    // try {
    // Vertex v = graph.traversal().V().has(GraphBuilder.PROPERTY_NAME,
    // vertexName).next();
    // return v;
    // } catch (NoSuchElementException e) {
    // return null;
    // }
    //
    // }
    /**
     * @param graph
     * @param vertexName
     * @param info
     * @return
     */
    public static Vertex createUDSmellVertex(Graph graph, String vertexName, int[] info) {
        logger.debug("Vertex UD smell created");
        return graph.addVertex(T.label, GraphBuilder.SMELL, GraphBuilder.PROPERTY_SMELL_TYPE,
                GraphBuilder.UNSTABLE_DEPENDENCY, GraphBuilder.PROPERTY_NAME, vertexName,
                GraphBuilder.PROPERTY_NUM_BAD_DEPENDENCIES, info[0], GraphBuilder.PROPERTY_DOUD, info[1]);
    }
    
    /**
     * @param graph
     * @return
     */
    public static Vertex createCycleSmellVertex(Graph graph){
        logger.debug("Vertex cycle smell created");
        return graph.addVertex(T.label, GraphBuilder.SMELL, GraphBuilder.PROPERTY_SMELL_TYPE, GraphBuilder.CYCLIC_DEPENDENCY);
    }

    // Modded
    public static Vertex createSuperCycleSmellVertex(Graph graph){
        logger.debug("Vertex super cycle smell created");
        return graph.addVertex(T.label, GraphBuilder.SMELL, GraphBuilder.PROPERTY_SMELL_TYPE, GraphBuilder.SUPER_CYCLIC_DEPENDENCY);
    }

    
    /**
     * @param graph
     * @return
     */
    public static Vertex createHLSmellVertex(Graph graph, String vertexName, int fanin, int fanout, int totDependency){
        logger.debug("Vertex hub-like smell created");
        return graph.addVertex(T.label, GraphBuilder.SMELL, GraphBuilder.PROPERTY_SMELL_TYPE, GraphBuilder.HUBLIKE_DEPENDENCY,
        		GraphBuilder.PROPERTY_NAME,vertexName,
        		GraphBuilder.PROPERTY_HL_FAN_IN,fanin,
        		GraphBuilder.PROPERTY_HL_FAN_OUT,fanout,
        		GraphBuilder.PROPERTY_HL_TOTAL_DEPENDENCY,totDependency);
    }
    
    /**
     * Create an instance of implicit cross package dependency smell in the graph
     * @param graph
     * @param ratioIn 
     * @param ratioOut 
     * @param i 
     * @param o 
     * @return
     */
    public static Vertex createImplicitCrossPackageDependencySmellVertex(Graph graph, String vertexName, int o, int i, double ratioOut, double ratioIn){
        logger.debug("Vertex implicit cross package dependency smell created");
        return graph.addVertex(T.label, GraphBuilder.SMELL,
        		GraphBuilder.PROPERTY_SMELL_TYPE, GraphBuilder.IMPLICIT_CROSS_PACKAGE_DEPENDENCY, 
        		GraphBuilder.PROPERTY_NAME,vertexName,
        		GraphBuilder.PROPERTY_IXPD_RATIO_OUT,ratioOut,
        		GraphBuilder.PROPERTY_IXPD_RATIO_IN,ratioIn,
        		GraphBuilder.PROPERTY_IXPD_LINK_OUT,o,
        		GraphBuilder.PROPERTY_IXPD_LINK_IN,i);
    }
    

    /**
     * Finds a node of the graph given its name if exists.
     * 
     * @param graph
     * @param vertexName
     * @return the requested Vertex or null if it doesn't exists.
     */
    public static Vertex findVertex(Graph graph, String vertexName, String label) {
        try {
            Vertex v = graph.traversal().V().hasLabel(label).has(GraphBuilder.PROPERTY_NAME, vertexName).next();
            return v;
        } catch (NoSuchElementException e) {
            return null;
        }

    }


    /**
     * Finds all nodes marked with a label.
     * 
     * @param graph
     * @param label
     * @return the list of Vertex with the specified label.
     */
    public static List<Vertex> findVerticesByLabel(Graph graph, String label) {
        Map<Object, Object> vertexMap = graph.traversal().V().hasLabel(label).group().by(T.label).next();
        return (List<Vertex>) vertexMap.get(label);
    }

    // TODO aggiungere caso integer e value
    /**
     * Finds all nodes marked with a certain property value.
     * 
     * @param graph
     * @param label
     * @param property
     * @param propertyValue
     * @return
     */
    public static List<Vertex> findVerticesByProperty(Graph graph, String label, String property, String propertyValue) {
        Map<Object, Object> vertexMap = graph.traversal().V().hasLabel(label).has(property, propertyValue).group().by(property).next();
        return (List<Vertex>) vertexMap.get(propertyValue);
    }
    

    /**
     * Finds all edges marked with a label.
     * 
     * @param graph
     * @param label
     * @return the list of Edge with the specified label.
     */
    public static List<Edge> findEdgesByLabel(Graph graph, String label) {
        Map<Object, Object> eMap = graph.traversal().E().hasLabel(label).group().by(T.label).next();

        return (List<Edge>) eMap.get(label);

    }

    /**
     * Return true if the class to insert already has its own node in the graph.
     * 
     * @param graph
     * @param clazzName
     //* @param ids
     * @param nodeType
     * @return true if the node does not exist yet.
     */
    @Deprecated
    public static boolean notAlreadyExistNode(Graph graph, String clazzName, String nodeType) {

        Iterator<Vertex> i = graph.vertices();
        while (i.hasNext()) {
            Vertex v = i.next();

            if (v.label().equals(nodeType) && v.property(GraphBuilder.PROPERTY_NAME).value().equals(clazzName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the class to insert already has its own node in the graph.
     * 
     * @param graph
     * @param clazzName
     //* @param ids
     * @param nodeType
     * @return true if the node does not exist yet.
     */
    public static Vertex existNode(Graph graph, String clazzName, String nodeType) {
        Vertex v = findVertex(graph, clazzName, nodeType);
        return v;
    }

    /**
     * Return true if the edge to insert already has its own instance in the
     * graph.
     * 
     //* @param data.graph
     * @param label
     * @param outVertex
     * @param inVertex
     * @return true if the edge does not exist yet.
     */
    @Deprecated
    public static boolean notAlreadyExistEdge(final String label, final Vertex outVertex, final Vertex inVertex) {
        Iterator<Edge> i = outVertex.edges(Direction.BOTH, label);
        while (i.hasNext()) {
            Edge e = i.next();
            if (e.outVertex().equals(inVertex) || e.inVertex().equals(inVertex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the edge to insert already has its own instance in the
     * graph in {@link Direction .BOTH} directions.
     * 
     * @param label
     * @param outVertex
     * @param inVertex
     * 
     * @return true if the edge does not exist yet.
     */
    public static Edge existEdge(String label, Vertex outVertex, Vertex inVertex) {
        Direction d = Direction.BOTH;
        return existEdge(label, outVertex, inVertex, d);
    }

    /**
     * Return true if the edge to insert already has its own instance in the
     * graph in selected direction.
     * 
     //* @param data.graph
     * @param label
     * @param outVertex
     * @param inVertex
     * @param direction
     * @return true if the edge does not exist yet.
     */
    public static Edge existEdge(String label, Vertex outVertex, Vertex inVertex, Direction direction) {
        Iterator<Edge> i = outVertex.edges(direction, label);
        while (i.hasNext()) {
            Edge e = i.next();
            if (e.outVertex().equals(inVertex) || e.inVertex().equals(inVertex)) {
                return e;
            }
        }
        return null;
    }

    public static List<Edge> getEdgesByVertex(String label, Vertex vertex, Direction d) {
        List<Edge> edges = new ArrayList<>();
        Iterator<Edge> i = vertex.edges(d, label);
        while (i.hasNext()) {
            Edge e = i.next();
            edges.add(e);
        }
        return edges;
    }

    /**
     * Filters the given list of vertex according to the property value
     * indicated.
     * 
     * @param toFilter
     * @param property
     * @param propertyValue
     * @return the filtered list.
     */
    public static List<Vertex> filterProperty(List<Vertex> toFilter, String property, String propertyValue) {
        List<Vertex> filtered = new ArrayList<>();
        for (Vertex v : toFilter) {
            if (v.value(property).equals(propertyValue)) {
                filtered.add(v);
            }
        }
        return filtered;
    }


    /**
     * @param fullClassName
     * @return the name of the package of the given class.
     */
    public static String getPackageName(String fullClassName) {
        String packageName = GraphBuilder.DEFAULT_PACKAGE;
        int pointIndex = fullClassName.lastIndexOf('.');
        if (pointIndex >= 0) {
            packageName = fullClassName.substring(0, pointIndex);
        }
        return packageName;
    }

    // Modded
    // Returns the set of edges between a set of vertices
    public static List<Edge> allEdgesBetweenVertices(Set<Vertex> vertices, String... labels) //TODO
    {
        List<Edge> edges = new ArrayList<>();
        for(Vertex vertex : vertices)
        {
            Iterator<Edge> localEdges = vertex.edges(Direction.OUT,labels);
            while(localEdges.hasNext())
            {
                Edge localEdge = localEdges.next();
                if(vertices.contains(localEdge.inVertex()))
                {
                    edges.add(localEdge);
                }
            }
        }
        return edges;
    }

    // Modded
    public static void incrementVertexIntProperty(Vertex vertex, String property)
    {
        vertex.property(property,(int) vertex.value(property) + 1);
    }


    //Modded
    public static Vertex getPackageOfClass(Vertex classVertex)
    {
        return classVertex.edges(Direction.OUT,PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString()).next().inVertex();
    }


}

