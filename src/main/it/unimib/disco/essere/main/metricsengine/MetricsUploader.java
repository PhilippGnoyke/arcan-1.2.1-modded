package it.unimib.disco.essere.main.metricsengine;

import java.util.List;
import java.util.Map;

import it.unimib.disco.essere.main.graphmanager.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


public class MetricsUploader {
    private static final Logger logger = LogManager.getLogger(MetricsUploader.class);
    Graph graph;
    PackageMetricsCalculator mCalc;
    ClassMetricsCalculator cCalc;
    private Map<String,Vertex> classes; // Modded
    private Map<String,Vertex> packages; // Modded
    private EdgeMaps edgeMaps; //Modded

    //Neo4JGraphWriter graphW;

    // Modded
    public MetricsUploader(Graph graph, Map<String,Vertex> classes, Map<String,Vertex> packages, EdgeMaps edgeMaps) {
        this.graph = graph;
        this.mCalc = new PackageMetricsCalculator(graph,classes,packages);
        this.cCalc = new ClassMetricsCalculator(graph,classes, edgeMaps);
        //this.graphW = new Neo4JGraphWriter();
        this.classes = classes;
        this.packages = packages;
        this.edgeMaps = edgeMaps;
    }

    public MetricsUploader(Graph graph) {
        //Modded
        List<Vertex> classList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        List<Vertex> packageList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);
        for (Vertex vertex : classList) {
            classes.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
        for (Vertex vertex : packageList) {
            packages.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
        this.graph = graph;
        this.mCalc = new PackageMetricsCalculator(graph,classes,packages);
        this.cCalc = new ClassMetricsCalculator(graph,classes,edgeMaps);
        //this.graphW = new Neo4JGraphWriter();
    }

    public void updateInstability() throws TypeVertexException {
        for(Vertex p : packages.values()){
            double instability = mCalc.calculateInstability(p);
            p.property(GraphBuilder.PROPERTY_INSTABILITY, instability);
        } 
    }


    public void calculateAbstractness() {
        // TODO Auto-generated method stub

    }

    
    public void calculateDistanceFromTheMainSequence() {
        // TODO Auto-generated method stub

    }

}
