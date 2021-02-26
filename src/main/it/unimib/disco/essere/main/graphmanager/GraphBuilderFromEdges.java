package it.unimib.disco.essere.main.graphmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class GraphBuilderFromEdges extends GraphBuilder {
	private static final Logger logger = LogManager.getLogger(GraphBuilderFromEdges.class);

	final File _edges;

	public GraphBuilderFromEdges(File edges) throws IOException {
		super(null, null);
		// readEdges(edges);
		_edges = edges;
	}

	public GraphBuilderFromEdges(HashMap<String, JavaClass> classes, HashMap<String, String> packages) {
		super(classes, packages);
		_edges = null;
	}

	private void readEdges(File edges) throws IOException {
		logger.info("***CSV initializated*** -exist " + edges.exists() + " file: " + edges.getName());
		// String[] header = { "out", "label", "in" };
		// CSVFormat formatter = CSVFormat.EXCEL.withHeader();
		CSVParser parser = CSVFormat.EXCEL.withHeader().parse(new InputStreamReader(new FileInputStream(edges)));
		// CSVParser parser = CSVParser.parse(edges.getAbsolutePath(),
		// formatter);
		// List<CSVRecord> i = parser.getRecords();
		// for (CSVRecord r:i) {
		// logger.info("record: "+r);
		// String out = r.get("out");
		// logger.info("out: "+out);
		// String e = r.get("label");
		// logger.info("label: "+e);
		// String in = r.get("in");
		// logger.info("in: "+in);
		// logger.info(out+" - "+e+" -> "+in);
		// }
		Iterator<CSVRecord> ir = parser.iterator();
		while (ir.hasNext()) {
			CSVRecord r = ir.next();
			logger.info("record: " + r);
			String out = r.get("out");
			logger.info("out: " + out);
			String e = r.get("label");
			logger.info("label: " + e);
			String in = r.get("in");
			logger.info("in: " + in);
			logger.info(out + " - " + e + " -> " + in);
		}
		parser.close();
		parser = null;
		logger.info("***CSV readed***");
	}

	private void readEdgesAndBuildGraph(Graph graph, File edges) throws IOException {
		logger.info("***CSV initializated*** -exist " + edges.exists() + " file: " + edges.getName());
		CSVParser parser = CSVFormat.EXCEL.withHeader().parse(new InputStreamReader(new FileInputStream(edges)));
		Iterator<CSVRecord> ir = parser.iterator();
		while (ir.hasNext()) {
			CSVRecord r = ir.next();
			logger.info("record: " + r);
			String out = r.get("out");
			logger.info("out: " + out);
			String e = r.get("label");
			logger.info("label: " + e);
			String in = r.get("in");
			logger.info("in: " + in);
			logger.info(out + " - " + e + " -> " + in);
			Vertex vertex = GraphUtils.existNode(graph, out, CLASS);
			if (vertex == null) {
				vertex = createNode(graph, out, CLASS);
			}
			if (PropertyEdge.LABEL_CLASS_DEPENDENCY.toString().equals(e)) {
				Vertex referencedclassVertex = GraphUtils.existNode(graph, in, CLASS);
				if (referencedclassVertex == null) {
					referencedclassVertex = createNode(graph, in, CLASS);
					if (referencedclassVertex != null
							&& !in.equals(vertex.property(PROPERTY_NAME).value().toString())) {
						Edge dependency = GraphUtils.existEdge(PropertyEdge.LABEL_CLASS_DEPENDENCY.toString(), vertex,
								referencedclassVertex, Direction.OUT);
						if (dependency == null) {
							vertex.addEdge(PropertyEdge.LABEL_CLASS_DEPENDENCY.toString(), referencedclassVertex,
									PROPERTY_DEPEDENCY_WEIGHT.toString(), DEFAULT_NUM_DEPENDENCES);
						} else {
							int weight = dependency.value(PROPERTY_DEPEDENCY_WEIGHT);
							++weight;
							dependency.property(PROPERTY_DEPEDENCY_WEIGHT, weight);
						}
					}

				}
			}
			if (PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString().equals(e)) {
				createPackageDependency(graph, in, vertex);
			}
		}
		parser.close();
		parser = null;
		logger.info("***CSV readed***");
	}
	/**
	 * Creates a dependency between a class node and a package node when the
	 * class belongs to the package.
	 * 
	 * @param graph
	 * @param packageName
	 * @param callerVertex
	 */
	void createPackageDependency(Graph graph, String packageName, Vertex callerVertex) {

		if (packageName != null) {
			if ("".equals(packageName)) {
				packageName = DEFAULT_PACKAGE;
			}
			Vertex packageVertex = GraphUtils.existNode(graph, packageName, PACKAGE);
			if (packageVertex == null) {
				packageVertex = createNode(graph, packageName, PACKAGE);
				packageVertex.property(PROPERTY_NUM_TOTAL_DEPENDENCIES, 0);
			}
			callerVertex.addEdge(PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString(), packageVertex);
			
			Iterator<Edge> e = callerVertex.edges(Direction.IN, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
			while(e.hasNext()){
				Edge d =e.next();
				Vertex v = d.outVertex();
				if (GraphUtils.existEdge(LABEL_AFFERENCE, v, packageVertex, Direction.OUT) == null &&
						GraphUtils.existEdge(PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString(), v, packageVertex, Direction.OUT)==null) {
					v.addEdge(LABEL_AFFERENCE, packageVertex);
				}
			}
			
		}
	}

	public void createGraph(Graph graph) {
		logger.debug("***Init***");
		try {
			readEdgesAndBuildGraph(graph, _edges);
		} catch (IOException e) {
			logger.error(e);
		}
		logger.debug("***Calculate afferent coupling btw packages***");
		calculateAfferentCouplingBetweenPackages(graph);
		calculateEfferentCoupling(graph);
	}

	/**
	 * Create a new node and return it.
	 * 
	 * @param graph
	 * @param nodeType
	 * @param clazz
	 * @return the new node
	 */

	Vertex createNode(Graph graph, String name, String nodeType) {

		String classType = RETRIEVED_CLASS;
		String packageType = RETRIEVED_PACKAGE;
		String classModifier = NO_MODIFIER;
		if (CLASS.equals(nodeType)) {
			// if(f!=null){
			classType = SYSTEM_CLASS;
			// if (f.isAbstract()) {
			// classModifier = ABSTRACT_CLASS;
			// }
			// if (f.isInterface()) {
			// classModifier = INTERFACE;
			// }
			// }
			Vertex vertex = graph.addVertex(T.label, nodeType, PROPERTY_NAME, name, PROPERTY_CLASS_TYPE, classType,
					PROPERTY_CLASS_MODIFIER, classModifier);

			return vertex;
		} else {
			// if (_packages.get(name)!=null) {
			packageType = SYSTEM_PACKAGE;
			// }
			Vertex vertex = graph.addVertex(T.label, nodeType, PROPERTY_NAME, name, PROPERTY_PACKAGE_TYPE, packageType);
			return vertex;
		}

	}

	/**
	 * Creates a dependency between two package vertex when one is afferent of
	 * the other
	 * 
	 * @param graph
	 */
	void calculateEfferentCoupling(Graph graph) {
		List<Edge> affEdges = GraphUtils.findEdgesByLabel(graph, GraphBuilder.LABEL_AFFERENCE);
		if(affEdges!=null){
			for (Edge e : affEdges) {
				Iterator<Edge> ie = e.outVertex().edges(Direction.OUT, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
				Edge packageEdge = e.outVertex().edges(Direction.OUT, PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString()).next();
				while(ie.hasNext()){
					Edge d =ie.next();
					Vertex v = d.outVertex();
					if (packageEdge!=null && GraphUtils.existEdge(PropertyEdge.LABEL_EFFERENCE.toString(), v, packageEdge.inVertex(), Direction.OUT) == null) {
						v.addEdge(PropertyEdge.LABEL_EFFERENCE.toString(), packageEdge.inVertex());
					}
				}
			}
		}
	}
	
	/**
	 * Creates a dependency between two package vertex when one is afferent of
	 * the other
	 * 
	 * @param graph
	 */
	void calculateEfferentCouplingBetweenPackages(Graph graph) {
		List<Edge> affEdges = GraphUtils.findEdgesByLabel(graph, GraphBuilder.LABEL_AFFERENCE);
		if(affEdges!=null){
			for (Edge e : affEdges) {
				Edge belongToEdge = graph.traversal().V(e.outVertex().id()).bothE(PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString())
						.next();
				if (GraphUtils.existEdge(LABEL_PACKAGE_AFFERENCE, belongToEdge.inVertex(), e.inVertex(), Direction.OUT) == null) {
					belongToEdge.inVertex().addEdge(LABEL_PACKAGE_AFFERENCE, e.inVertex());

					Integer numTotalDep = belongToEdge.inVertex().value(PROPERTY_NUM_TOTAL_DEPENDENCIES);
					numTotalDep += 1;
					belongToEdge.inVertex().property(PROPERTY_NUM_TOTAL_DEPENDENCIES, numTotalDep);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		File f = new File(Paths.get("src", "resources", "edges.csv").toAbsolutePath().toString());
		File dbFolder = Paths.get("Neo4j-from-edges", "default.graphdb").toAbsolutePath().toFile();
		GraphBuilderFromEdges ed = new GraphBuilderFromEdges(f);
		logger.info("***Start Writing Neo4j***");
		logger.info("***Start Writing Neo4j*** - " + dbFolder.toPath());
		GraphWriter graphW = new Neo4JGraphWriter();
		graphW.setup(dbFolder.toPath().toAbsolutePath().toString());
		Graph graph = graphW.init();
		logger.info("***Graph initializated***");
		logger.info("***Graph initializated*** - graph:   " + graph);
		logger.info("***Graph initializated*** - builder: " + ed);
		ed.createGraph(graph);
		logger.info("***Graph readed from edges file***");
		graphW.write(graph, false);
		logger.info("***End of graph building***");
		graph.close();
	}
}