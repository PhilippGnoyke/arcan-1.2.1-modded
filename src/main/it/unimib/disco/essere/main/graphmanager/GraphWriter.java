package it.unimib.disco.essere.main.graphmanager;

import org.apache.tinkerpop.gremlin.structure.Graph;

public interface GraphWriter {   
    
    /**
     * Makes the setup of the database at the specified path.
     * 
     * @param url
     */
    public void setup(String url);
    
    /**
     * Return a new instance of the graph
     * 
     * @return the new graph
     */
    public Graph init();
    
    /**
     * Write graph on a graphML file and in the database
     * 
     * @param graph
     * @param closeAfterWrite
     */
    public void write(Graph graph, boolean closeAfterWrite);
    

}