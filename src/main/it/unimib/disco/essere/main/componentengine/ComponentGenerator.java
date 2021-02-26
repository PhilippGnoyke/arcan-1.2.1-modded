package it.unimib.disco.essere.main.componentengine;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;

//THIS CLASS UPDATES THE DATABASE: NEED WRITE -line 64
public class ComponentGenerator {
    private static final Logger logger = LogManager.getLogger(ComponentGenerator.class);
    public Graph graph;

    public ComponentGenerator(Graph graph) {
        this.graph = graph;
    }

    public void createComponent(String compName, String... partsOfComponent) {
        Vertex compVertex = GraphUtils.existNode(graph, compName, GraphBuilder.COMPONENT);
        if (compVertex == null) {
            graph.addVertex(T.label, GraphBuilder.COMPONENT, GraphBuilder.PROPERTY_NAME, compName);
            for (String part : partsOfComponent) {
                searchAndLinkToComponent(part, GraphBuilder.PACKAGE, compVertex);
                searchAndLinkToComponent(part, GraphBuilder.CLASS, compVertex);
            }

        } else {
            logger.debug("A component with this name already exists.");
        }
    }

    public void createConstraint(String component1, String component2, Direction direction) {

    }

    public void linkNodeToComponent(String component, String partOfComponent) {

    }

    public boolean searchAndLinkToComponent(String partOfComponent, String nodeType, Vertex component) {
        Vertex part = GraphUtils.existNode(graph, partOfComponent, nodeType);
        if (part != null) {         
            if(GraphBuilder.PACKAGE.equals(nodeType)){
                // check if the classes belonging to the package are already part of component
                Iterator<Edge> classes = part.edges(Direction.IN, PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString());
                while(classes.hasNext()){
                    Vertex clazz = classes.next().outVertex();
                    if(!clazz.edges(Direction.OUT, GraphBuilder.IS_PART_OF_COMPONENT).hasNext()){
                        clazz.addEdge(GraphBuilder.IS_PART_OF_COMPONENT, component);
                    }
                }
            }
            Edge e = part.edges(Direction.OUT, GraphBuilder.IS_PART_OF_COMPONENT).next();
            if(e != null){
                e.remove();
            }
            part.addEdge(GraphBuilder.IS_PART_OF_COMPONENT, component);
            return true;
        } else {
            return false;
        }
    }

    /*
     * public void searchAndLinkToComponent(String part, List<String>
     * searchList, String searchListType, Vertex vertexComponent){
     * if(searchList.contains(part)){ VertexGraphUtils.findVertex(graph, part,
     * searchListType); } }
     */
}
