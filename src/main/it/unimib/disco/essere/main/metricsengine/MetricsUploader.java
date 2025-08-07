package it.unimib.disco.essere.main.metricsengine;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;


import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;



public class MetricsUploader {
    private static final Logger logger = LogManager.getLogger(MetricsUploader.class);
    Graph graph;
    PackageMetricsCalculator mCalc;
    ClassMetricsCalculator cCalc;
    private List<Vertex> classes; // Modded
    private List<Vertex> packages; // Modded

    //Neo4JGraphWriter graphW;

    // Modded
    public MetricsUploader(Graph graph,List<Vertex> classes, List<Vertex> packages) {
        this.graph = graph;
        this.mCalc = new PackageMetricsCalculator(graph,classes,packages);
        this.cCalc = new ClassMetricsCalculator(graph);
        //this.graphW = new Neo4JGraphWriter();
        this.classes = classes;
        this.packages = packages;
    }

    public MetricsUploader(Graph graph) {
        this.classes = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS); // Modded
        this.packages = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE); // Modded
        this.graph = graph;
        this.mCalc = new PackageMetricsCalculator(graph,classes,packages);
        this.cCalc = new ClassMetricsCalculator(graph);
        //this.graphW = new Neo4JGraphWriter();
    }

    public void updateInstability() throws TypeVertexException {
        for(Vertex p : packages){
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
