package it.unimib.disco.essere.main.asengine.cycleutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;

public class PrintShapesDocker extends PrintShapes {

	private static final Logger logger = LogManager.getLogger(PrintShapesDocker.class);
	private FileWriter writer = null;
	private CSVFormat formatter = null;
	private CSVPrinter printer = null;

	private String fileCsvName = "CyclesShapeTable.csv";

	public PrintShapesDocker() {

	}

	public PrintShapesDocker(String fileCsvName) {
		if (fileCsvName != null) {
			this.fileCsvName = fileCsvName;
		}
	}
	
	@Override
	public void initializePrint(File path, String vertexType) {
		initializePrint(path,vertexType,new String[]{
				"IdCycle",
				"CycleType",
				"MinWeight",
				"MaxWeight",
				"numVertices",
				"ElementList"});
	}
	
	public void initializePrint(File path, String vertexType,String [] header) {
		try {
			writer = new FileWriter(
					Paths.get(path.getAbsolutePath(), vertexType + fileCsvName).toAbsolutePath().toFile());
			formatter = CSVFormat.EXCEL.withHeader(header);
			printer = new CSVPrinter(writer, formatter);
		} catch (IOException e) {
			logger.debug(e.getMessage());
		}
	}

	@Override
	public void printCycles(Stack<Vertex> cycle) {
		try {
			String cycleList = "";
			boolean first = true;

			for (Vertex v : cycle) {
				if (first) {
					cycleList += v.value(GraphBuilder.PROPERTY_NAME);
					first = false;
				} else {
					cycleList += "," + v.value(GraphBuilder.PROPERTY_NAME);
				}
			}
			cycleList += "";

			printer.print(cycleList);
//			printer.println();
		} catch (NoSuchElementException | IOException e) {
			logger.debug(e.getMessage());
		}

	}

	@Override
	public void printCyclesFromGraph(Graph graph, List<Vertex> shapeVertices) {
		logger.debug("Smell vertices" + shapeVertices);
		if (shapeVertices != null) {
			for (Vertex v : shapeVertices) {
				String shapeType = v.value(GraphBuilder.PROPERTY_SHAPE);
				logger.debug("stampa " + v.keys().size());
				logger.debug("stampa " + shapeType);
				// iterate on the keys of the smell node attribute
				Iterator<String> m = v.keys().iterator();
				int weightmin = 0;
				int weightmax = 0;
				int numVertices = CDFilterUtils.getNumOfCycleVertices(graph, v);
				while (m.hasNext()) {
					String s = m.next();
					logger.debug("stampa " + s);
					if (GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MIN.equals(s)) {
						weightmin = v.value(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MIN);
					}
					if (GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MAX.equals(s)) {
						weightmax = v.value(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MAX);
					}
				}

				// int numVertices =
				// Integer.parseInt(v.value(GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES).toString());

				// String weightmin =
				// v.value(GraphBuilder.PROPERTY_DEPEDENCY_WEIGHT_MIN);
				try {
					printer.print(v.id());
					printer.print(shapeType);
					printer.print(weightmin);
					printer.print(weightmax);
					printer.print(numVertices);

					if (GraphBuilder.CHAIN.equals(shapeType) || GraphBuilder.STAR.equals(shapeType)) {
						Iterator<Edge> i = v.edges(Direction.OUT, GraphBuilder.LABEL_IS_PART_OF_CHAIN,
								GraphBuilder.LABEL_IS_PART_OF_STAR);

						Set<Vertex> cycleToPrint = new HashSet<>();
						while (i.hasNext()) {
							Edge e = i.next();
							cycleToPrint.addAll(CDFilterUtils.getCycleVertices(graph, e.inVertex()));
						}

						Stack<Vertex> appoggio = new Stack<>();
						appoggio.addAll(cycleToPrint);
						printCycles(appoggio);
					} else if (GraphBuilder.CIRCLE.equals(shapeType) || GraphBuilder.CLIQUE.equals(shapeType)) {

						Iterator<Edge> e = v.edges(Direction.OUT, GraphBuilder.LABEL_IS_CIRCLE_SHAPED,
								GraphBuilder.LABEL_IS_CLIQUE_SHAPED);
						if (e.hasNext()) {
							Vertex v2 = e.next().inVertex();
							printCycles(CDFilterUtils.getCycleVertices(graph, v2));
						}
					}
					if(GraphBuilder.PACKAGE.equals(v.value(GraphBuilder.PROPERTY_VERTEX_TYPE))){
						Set<Vertex> cl = CDFilterUtils.getClassInvolved(graph,v);
						logger.debug("classe involved");
						logger.debug(v+",\tsmell package: "+v.value(GraphBuilder.PROPERTY_SHAPE));
						String cycleClassList = "";
						boolean first = true;
						for(Vertex clazzOfTheCycle : cl){
//							System.out.println(clazzOfTheCycle+",\tclass name: "+clazzOfTheCycle.value(GraphBuilder.PROPERTY_NAME));
							if (first) {
								cycleClassList += clazzOfTheCycle.value(GraphBuilder.PROPERTY_NAME);
								first = false;
							} else {
								cycleClassList += "," + clazzOfTheCycle.value(GraphBuilder.PROPERTY_NAME);
							}
						}
						cycleClassList += "";
						logger.debug(cycleClassList);
						printer.print(cycleClassList);
						printer.println();
					}else{
						printer.println();
					}
				} catch (IOException e) {
					logger.debug(e.getMessage());
				}
			}
		} else {
			logger.info("***No shape identified in cyclic dependency smells, nothing to print***");
		}
	}

	@Override
	public void closePrint() {
		try {
			printer.close();
			writer.close();
		} catch (IOException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
	}
}
