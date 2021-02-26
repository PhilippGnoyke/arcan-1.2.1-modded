package it.unimib.disco.essere.main.asengine.filters;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.asengine.udutils.UDUtils;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;

//TODO put "rate" as global attribute and create its setter, to allow the chioce of its value
public class UDRateFilter implements FilterInterface{
    private static final Logger logger = LogManager.getLogger(UDRateFilter.class);
    public Graph _graph;
    
    
    public UDRateFilter(Graph _graph) {
        this._graph = _graph;
    }

    public Map<String, List<String>> filter(int rate){
        Map<String, List<String>> filteredSmellMap = new HashMap<>();
        Iterator<Vertex> i = _graph.traversal().V().has(GraphBuilder.PROPERTY_DOUD, P.gt(rate));
        Direction d = Direction.OUT;
        while(i.hasNext()){
            Vertex v = i.next();
            logger.debug("filtered Vertex: " + v + ", rate: " + v.value(GraphBuilder.PROPERTY_DOUD));
            UDUtils.createMap(filteredSmellMap, d, v);
        }
        return filteredSmellMap;
    }

}
