package it.unimib.disco.essere.main.asengine.cycleutils;

import java.io.File;
import java.util.List;
import java.util.Stack;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public interface CyclePrinter {
    
   
    
    /**
     * Set the name of the file to print the results into.
     * @param path TODO
     * @param vertexType TODO
     */
    void initializePrint(File path, String vertexType);
    
    /**
     * Print the given cycle into a csv file
     * 
     * @param cycle
     */
    void printCycles(Stack<Vertex> cycle);
    
    void printCyclesFromGraph(Graph graph, List<Vertex> smellVertices);
    
    
    void closePrint();
}
