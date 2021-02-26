package it.unimib.disco.essere.main.asengine.cycleutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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

public class CDFilterUtils {

	private static final Logger logger = LogManager.getLogger(CDFilterUtils.class);

	public static Vertex createShapeVertex(Graph graph, String shapeType, String vertexType) {
		return graph.addVertex(T.label, GraphBuilder.CYCLE_SHAPE, GraphBuilder.PROPERTY_SHAPE, shapeType,
				GraphBuilder.PROPERTY_VERTEX_TYPE, vertexType);
	}

	public static Stack<Vertex> getCycleVertices(Graph graph, Vertex smellVertex) {
		List<Edge> cycleEdges = GraphUtils.findEdgesByLabel(graph, GraphBuilder.LABEL_CYCLE_AFFECTED);
		Stack<Vertex> cycleVertices = new Stack<>();
		for (Edge e : cycleEdges) {
			if (e.outVertex().equals(smellVertex)) {
				cycleVertices.push(e.inVertex());
			}
		}
		cycleVertices.push(smellVertex.edges(Direction.OUT, GraphBuilder.LABEL_START_CYCLE).next().inVertex());

		return cycleVertices;
	}

	public static List<Vertex> getAllOutVertices(Vertex smell, String... edgeLabels) {
		List<Vertex> cycleVertices = new ArrayList<>();
		Iterator<Edge> edges = smell.edges(Direction.OUT, edgeLabels);
		Edge e;
		while (edges.hasNext()) {
			e = edges.next();
			cycleVertices.add(e.inVertex());
		}
		return cycleVertices;
	}

	public static long getNumberOfEdges(Graph graph, Vertex smell, String label) {
		List<Vertex> vertices = getAllOutVertices(smell, GraphBuilder.LABEL_CYCLE_AFFECTED);
		int count = 0;

		List<Long> ids = new ArrayList<>();
		for (Vertex v : vertices) {
			ids.add((Long) v.id());
		}

		Iterator<Edge> i = graph.traversal().V(ids).toE(Direction.OUT, label);
		while (i.hasNext()) {
			Edge e = i.next();
			if (vertices.contains(e.inVertex()) && vertices.contains(e.outVertex())) {
				// logger.debug("id edge: " + e.id());
				count += 1;
			}
		}
		// logger.debug("Number of edges: " + count);
		return count;
	}

	public static List<Edge> getAllEdgesOfCycle(Graph graph, Vertex smell, String label) {
		List<Vertex> vertices = getAllOutVertices(smell, GraphBuilder.LABEL_CYCLE_AFFECTED);
		List<Edge> edges = new ArrayList<>();

		List<Long> ids = new ArrayList<>();
		for (Vertex v : vertices) {
			ids.add((Long) v.id());
		}

		Iterator<Edge> i = graph.traversal().V(ids).toE(Direction.OUT, label);
		while (i.hasNext()) {
			Edge e = i.next();
			if (vertices.contains(e.inVertex()) && vertices.contains(e.outVertex())) {
				edges.add(e);
			}
		}
		return edges;
	}

	public static int getNumOfCycleVertices(Graph graph, Vertex shape) {
		List<Vertex> smellVertices = getAllOutVertices(shape, GraphBuilder.LABEL_IS_PART_OF_STAR,
				GraphBuilder.LABEL_IS_CIRCLE_SHAPED, GraphBuilder.LABEL_IS_CLIQUE_SHAPED,
				GraphBuilder.LABEL_IS_PART_OF_CHAIN);
		if (shape.value(GraphBuilder.PROPERTY_SHAPE).toString().equals(GraphBuilder.STAR)
				|| shape.value(GraphBuilder.PROPERTY_SHAPE).toString().equals(GraphBuilder.CHAIN)) {
			return (smellVertices.size() + 1);
		} else {
			int numVertices = 0;
			for (Vertex v : smellVertices) {
				numVertices += Integer.parseInt(v.value(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES).toString());
			}

			return numVertices;
		}

	}

	public static void getMinMaxOfWeight(Graph graph, Vertex shape, String edgeLabel) {
		Iterator<Edge> partOfShape = shape.edges(Direction.OUT, edgeLabel);

		// List<Edge> le = getAllEdgesOfCycle(graph, smell,
		// GraphBuilder.LABEL_CLASS_DEPENDENCY);
		int globalMin = 0;
		int globalMax = 0;
		boolean first = true;
		logger.debug("Shape: " + shape.value(GraphBuilder.PROPERTY_SHAPE) + " PART OF SHAPE.HASNEXT: " + partOfShape.hasNext());
		while (partOfShape.hasNext()) {
			Edge inVpartOfShape = partOfShape.next();
			List<Edge> le = getAllEdgesOfCycle(graph, inVpartOfShape.inVertex(),
					PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
			//            HashSet<Vertex> vertexClassOfTheCycle = new HashSet<Vertex>();
			//            for (Edge e : le) {
			//                vertexClassOfTheCycle.add(e.outVertex());
			//            }
			int localMin = 0;
			int localMax = 0;
			boolean localfirst = true;
			for(Edge e : le){
				int weight = e.value(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT);
				if (localfirst) {
					localfirst = false;
					localMin = weight;
					localMax = weight;
				} else {
					if (weight < localMin) {
						localMin = weight;
					}
					if (weight > localMax) {
						localMax = weight;
					}
				}
			}
			//            for (Vertex v : vertexClassOfTheCycle) {
			//                Iterator<Edge> i = v.edges(Direction.OUT, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
			//                while (i.hasNext()) {
			//                    int weight = i.next().value(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT);
			//                    if (localfirst) {
			//                        localfirst = false;
			//                        localMin = weight;
			//                        localMax = weight;
			//                    } else {
			//                        if (weight < localMin) {
			//                            localMin = weight;
			//                        }
			//                        if (weight > localMax) {
			//                            localMax = weight;
			//                        }
			//                    }
			//                }
			//            }
			logger.debug("min local weight = " + localMin + " max local weight = " + localMax);
			inVpartOfShape.inVertex().property(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MIN, localMin);
			inVpartOfShape.inVertex().property(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MAX, localMax);
			if (first) {
				first = false;
				globalMin = localMin;
				globalMax = localMax;
			} else {
				if (localMin < globalMin) {
					globalMin = localMin;
				}
				if (localMax > globalMax) {
					globalMax = localMax;
				}

			}
		}
		logger.debug("min global weight = " + globalMin + " max global weight = " + globalMax);
		shape.property(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MIN, globalMin);
		shape.property(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MAX, globalMax);
		// graph.addVertex(smell);
	}

	//partOfTheCycle
	public static Map<Vertex,Set<Vertex>> getClassInvolved(Graph graph){
		Map<Vertex,Set<Vertex>> results = new HashMap<Vertex,Set<Vertex>>();
		List<Vertex> cycleShape =GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.PACKAGE);
		logger.debug("cycleShape"+cycleShape+" "+cycleShape.iterator().next().label());
		//    	Iterator<Vertex> belongsTo = graph.traversal().V().hasLabel(GraphBuilder.CYCLE_SHAPE).toE(Direction.OUT).inV();//.toE(Direction.IN, GraphBuilder.LABEL_CYCLE_AFFECTED);
		for(Vertex v :cycleShape){
			Set<Vertex> classes = getClassInvolved(graph,v);
			results.put(v, classes);
		}
		return results;
	}

	//partOfTheCycle
	public static Set<Vertex> getClassInvolved(final Graph graph, final Vertex v){
		final Set<Vertex> classes = new HashSet<Vertex>();
		final Set<Vertex> packages = graph.traversal().V(v).toE(Direction.OUT).inV().toE(Direction.OUT, GraphBuilder.LABEL_CYCLE_AFFECTED).inV().toSet();
		final Set<Vertex> cycldep = graph.traversal().V(v).toE(Direction.OUT).inV().toE(Direction.OUT, GraphBuilder.LABEL_CYCLE_AFFECTED).outV().toSet();
		logger.debug("packages"+packages+" "+packages.iterator().next().label());
		logger.debug("cycldep"+cycldep+" "+cycldep.iterator().next().label());
		for(Vertex cv : cycldep){
			Set<Vertex> packagessub = graph.traversal().V(cv).toE(Direction.OUT, GraphBuilder.LABEL_CYCLE_AFFECTED).inV().toSet();
			logger.debug("packagessub"+packagessub+" "+packagessub.iterator().next().label());
			logger.debug("cv "+cv.label());
			for(Vertex sp : packagessub){
				classes.addAll(getClassInvolved(graph, sp, PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString(), packages));
			}
		}
		return classes;
	}

	private static Set<Vertex> getClassInvolved(Graph graph, Vertex shape, String edgeLabel, Set<Vertex> packages) {
		graph.traversal().V(shape).toE(Direction.IN, edgeLabel).outV().toSet();
		Set<Edge> classDependency = graph.traversal().V(shape).toE(Direction.IN, edgeLabel).outV().toE(Direction.OUT,PropertyEdge.LABEL_CLASS_DEPENDENCY.toString()).toSet();
		Set<Vertex> classes = new HashSet<Vertex>();
		for(Edge e : classDependency){
			logger.debug("edge "+e);
			Vertex v = e.inVertex();
			Edge belongsTo = graph.traversal().V(v).toE(Direction.OUT, PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString()).next();
			if(packages.contains(belongsTo.inVertex())){
				logger.debug("edge "+e+" INVOLVED");
				classes.add(v); 
				classes.add(e.outVertex()); 
			}
		}
		return classes;
	}

	public static void cleanCDDetection(Graph graph) {
		Iterator<Edge> i = graph.traversal().E().hasLabel(GraphBuilder.LABEL_CYCLE_AFFECTED,
				GraphBuilder.LABEL_START_CYCLE);
		while (i.hasNext()) {
			Edge e = i.next();
			e.remove();
		}
		Iterator<Vertex> iv = graph.traversal().V().has(GraphBuilder.PROPERTY_SMELL_TYPE,
				GraphBuilder.CYCLIC_DEPENDENCY);
		while (iv.hasNext()) {
			Vertex v = iv.next();
			v.remove();
		}
	}

	public static void cleanCDShapeFilter(Graph graph) {
		Iterator<Edge> i = graph.traversal().E().hasLabel(GraphBuilder.LABEL_IS_CENTRE_OF_STAR,
				GraphBuilder.LABEL_IS_PART_OF_STAR, GraphBuilder.LABEL_IS_PART_OF_CHAIN,
				GraphBuilder.LABEL_IS_CIRCLE_SHAPED, GraphBuilder.LABEL_IS_CLIQUE_SHAPED);
		while (i.hasNext()) {
			Edge e = i.next();
			e.remove();
		}
		Iterator<Vertex> iv = graph.traversal().V().hasLabel(GraphBuilder.CYCLE_SHAPE);
		while (iv.hasNext()) {
			Vertex v = iv.next();
			v.remove();
		}
	}

	public static boolean checkIfNestedClass(Vertex motherClass, Vertex checkClass) {
		String motherName = motherClass.value(GraphBuilder.PROPERTY_NAME);
		String checkName = checkClass.value(GraphBuilder.PROPERTY_NAME);
		int index = checkName.indexOf('$');
		logger.debug("nameCheck1: " + checkName + " mother1 " + motherName);

		if (index != -1) {
			checkName = checkName.substring(0, index);
			if (motherName.equals(checkName)) {

				logger.debug("nameCheck: " + checkName + " mother " + motherName);
				return true;
			}
		}

		return false;

	}

}
