package it.unimib.disco.essere.main.metricsengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.neuralnet.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.google.common.collect.ImmutableList;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;

public class PackageMetricsCalculator {
    private static final Logger logger = LogManager.getLogger(PackageMetricsCalculator.class);
    private Graph graph;
    private Map<String,Vertex> packages; // Modded
    private Map<String,Vertex> classes; // Modded

    //Modded
    public PackageMetricsCalculator(Graph graph, Map<String,Vertex> classes, Map<String,Vertex> packages) {
        this.graph = graph;
        this.classes = classes;
        this.packages = packages;
    }

    //Modded
    public PackageMetricsCalculator(Graph graph) {
        this.graph = graph;
        List<Vertex> classList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        List<Vertex> packageList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);
        for (Vertex vertex : classList) {
            classes.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
        for (Vertex vertex : packageList) {
            packages.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
    }


    public int calculateNumberOfInterfaces() {
        int numberOfInterfaces = 0;
        for (Vertex v : classes.values()) {
            if (v.property(GraphBuilder.PROPERTY_CLASS_MODIFIER).value().toString().equals(GraphBuilder.INTERFACE)) {
                ++numberOfInterfaces;
            }
        }

        return numberOfInterfaces;
    }

    /**
     * @param packageVertex
     * @return the number of the classes which are afferent to packageVertex.
     * @throws TypeVertexException
     */
    public int calculateAfferentClasses(final Vertex packageVertex) throws TypeVertexException {
        List<Edge> afferentClasses = calculatePackageEdges(packageVertex, GraphBuilder.LABEL_AFFERENCE, Direction.IN);
        return afferentClasses.size();
    }
    
    public int calculateAfferentClasses(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        int afferent = calculateAfferentClasses(pkg);
        pkg.property(GraphBuilder.PROPERTY_CA, afferent);
        return afferent;
    }

    /**
     * @param packageVertex
     * @return the number of the classes which are efferent to packageVertex.
     * @throws TypeVertexException
     */
    public int calculateEfferentClasses(final Vertex packageVertex) throws TypeVertexException {
        List<Edge> efferentClasses = calculatePackageEdges(packageVertex, PropertyEdge.LABEL_EFFERENCE.toString(), Direction.IN);
        return efferentClasses.size();
    }
    
    public int calculateEfferentClasses(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        int efferent = calculateEfferentClasses(pkg);
        pkg.property(GraphBuilder.PROPERTY_CE, efferent);
        return efferent;
    }

    public int calculateInternalEfferentClasses(final Vertex packageVertex) throws TypeVertexException {
        List<Edge> efferentClasses = calculatePackageEdges(packageVertex, PropertyEdge.LABEL_EFFERENCE.toString(), Direction.IN);
        List<Edge> internalEfferentClasses = new ArrayList<>();
        for (Edge e : efferentClasses) {
            if (e.outVertex().property(GraphBuilder.PROPERTY_CLASS_TYPE).value().toString()
                    .equals(GraphBuilder.SYSTEM_CLASS)) {
                internalEfferentClasses.add(e);
            }
        }
        return internalEfferentClasses.size();
    }
    
    public int calculateInternalEfferentClasses(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        int efferent = calculateInternalEfferentClasses(pkg);
        pkg.property(GraphBuilder.PROPERTY_CE_INTERNAL, efferent);
        return calculateInternalEfferentClasses(pkg);
    }

    /**
     * @param packageVertex
     * @return the instability of packageVertex.
     * @throws TypeVertexException
     */
    public double calculateInstability(final Vertex packageVertex) throws TypeVertexException {
        double Ca = calculateAfferentClasses(packageVertex);
        double Ce = calculateEfferentClasses(packageVertex);
        if (Ca + Ce > 0) {
            double instability = Ce / (Ca + Ce);
            return instability;
        } else {
            return 0.0;
        }
    }
    
    public double calculateInstability(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        double instability = calculateInstability(pkg);
        pkg.property(GraphBuilder.PROPERTY_INSTABILITY, instability);
        return instability;
    }

    public double calculateInternalInstability(final Vertex packageVertex) throws TypeVertexException {

        double Ca = calculateAfferentClasses(packageVertex);
        double Ce = calculateInternalEfferentClasses(packageVertex);
        if (Ca + Ce > 0) {
            double instability = Ce / (Ca + Ce);
            return instability;
        } else {
            return 0.0;
        }
    }
    
    public double calculateInternalInstability(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        double instability = calculateInternalInstability(pkg);
        pkg.property(GraphBuilder.PROPERTY_INSTABILITY_INTERNAL, instability);
        return instability;
    }

    /**
     * @param packageVertex
     * @return the abstractness of packageVertex.
     * @throws TypeVertexException
     */
    public double calculateAbstractness(final Vertex packageVertex) throws TypeVertexException {
        if (GraphBuilder.CLASS.equals(packageVertex.label())) {
            throw new TypeVertexException("Wrong Vertex type. Expected type package");
        }
        double numberOfAbstractClasses = 0.0;
        double numberOfClassesInPackage = 0.0;

        List<Edge> packageDependences = calculatePackageEdges(packageVertex, PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString(),
                Direction.IN);
        for (Edge e : packageDependences) {
            if (e.outVertex().property(GraphBuilder.PROPERTY_CLASS_MODIFIER).value().equals(GraphBuilder.ABSTRACT_CLASS)
                    || e.outVertex().property(GraphBuilder.PROPERTY_CLASS_MODIFIER).value()
                            .equals(GraphBuilder.INTERFACE)) {
                numberOfAbstractClasses += 1;
            }

        }
        numberOfClassesInPackage = packageDependences.size();
        if(numberOfClassesInPackage != 0 && numberOfAbstractClasses != 0){
            return numberOfAbstractClasses / numberOfClassesInPackage;
        }else{
           return 0; 
        }
    }
    
    public double calculateAbstractness(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        double rma = calculateAbstractness(pkg);
        pkg.property(GraphBuilder.PROPERTY_RMA, rma);
        return rma;
    }

    /**
     * @param packageVertex
     * @return the distance from the main sequence of packageVertex.
     * @throws TypeVertexException
     */
    public double calculateDistanceFromTheMainSequence(final Vertex packageVertex) throws TypeVertexException {

        double abstractness = calculateAbstractness(packageVertex);
        double instability = calculateInstability(packageVertex);
        double distance = Math.abs(abstractness + instability - 1);
        return distance;
    }
    
    public double calculateDistanceFromTheMainSequence(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        double rmd = calculateDistanceFromTheMainSequence(pkg);
        pkg.property(GraphBuilder.PROPERTY_RMD, rmd);
        return rmd;
    }

    /**
     * @return the instability of all packages in the graph.
     * @throws TypeVertexException
     */
    public Map<String, Double> calculatePackagesInstability() throws TypeVertexException {
        Map<String, Double> instabilityMap = new HashMap<>();

        for (Vertex v : packages.values()) {
            instabilityMap.put(v.property(GraphBuilder.PROPERTY_NAME).value().toString(), calculateInstability(v));
        }
        return instabilityMap;

    }

    /**
     * @param packageVertex
     * @param label
     * @param dir
     * @return the ingoing or outgoing edges of the given package. 
     * @throws TypeVertexException
     */
    public List<Edge> calculatePackageEdges(Vertex packageVertex, String label, Direction dir)
            throws TypeVertexException {
        if (GraphBuilder.CLASS.equals(packageVertex.label())) {
            throw new TypeVertexException("Wrong Vertex type. Expected type package");
        }
        return ImmutableList.copyOf(packageVertex.edges(dir, label));
    }


    /**
     * @param packageVertex
     * @return all Martin metrics related to the given package.
     * @throws TypeVertexException
     */
    public double[] calculatePackageMetrics(Vertex packageVertex) throws TypeVertexException {
        double[] metrics = new double[5];
        metrics[0] = calculateAfferentClasses(packageVertex);
        metrics[1] = calculateEfferentClasses(packageVertex);
        metrics[2] = calculateInstability(packageVertex);
        metrics[3] = calculateAbstractness(packageVertex);
        metrics[4] = calculateDistanceFromTheMainSequence(packageVertex);

        return metrics;
    }
    
    public double[] calculatePackageMetrics(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        return calculatePackageMetrics(pkg);
    }

    /**
     * @param packageVertex
     * @return all Martin metrics related to the given package excluding from
     *         computation all elements which don't belong to the analyzed
     *         project.
     * @throws TypeVertexException
     */
    public double[] calculateInternalPackageMetrics(Vertex packageVertex) throws TypeVertexException {
        double[] metrics = new double[5];
        metrics[0] = calculateAfferentClasses(packageVertex);
        metrics[1] = calculateInternalEfferentClasses(packageVertex);
        metrics[2] = calculateInternalInstability(packageVertex);
        metrics[3] = calculateAbstractness(packageVertex);
        metrics[4] = calculateDistanceFromTheMainSequence(packageVertex);

        return metrics;
    }
    
    public double[] calculateInternalPackageMetrics(String packageVertex) throws TypeVertexException{
        Vertex pkg = packages.get(packageVertex); // modded
        //Vertex pkg = GraphUtils.findVertex(graph, packageVertex,GraphBuilder.PACKAGE);
        return calculateInternalPackageMetrics(pkg);
    }

}
