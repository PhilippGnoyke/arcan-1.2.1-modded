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

public class PrintShapes implements CyclePrinter {
    private static final Logger logger = LogManager.getLogger(PrintShapes.class);
    private FileWriter writer = null;
    private CSVFormat formatter = null;
    private CSVPrinter printer = null;

    private String fileCsvName = "CyclesShapeTable.csv";
    
    public PrintShapes() {

    }
    public PrintShapes(String fileCsvName) {
    	if(fileCsvName!=null){
    		this.fileCsvName = fileCsvName;
    	}
    }

    @Override
    public void initializePrint(File path, String vertexType) {
        try {
            writer = new FileWriter(
                    Paths.get(path.getAbsolutePath(), vertexType + fileCsvName).toAbsolutePath().toFile());
            String[] header = new String[3];
            header[0] = "IdCycle";
            header[1] = "CycleType";
            header[2] = "ElementList";

            formatter = CSVFormat.EXCEL.withHeader(header);
            printer = new CSVPrinter(writer, formatter);
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

    @Override
    public void printCycles(Stack<Vertex> cycle) {
        try {
            for (Vertex v : cycle) {
                printer.print(v.value(GraphBuilder.PROPERTY_NAME));
            }
            printer.println();
        } catch (NoSuchElementException | IOException e) {
            logger.debug(e.getMessage());
        }

    }

    @Override
    public void printCyclesFromGraph(Graph graph, List<Vertex> smellVertices) {
        logger.debug("Smell vertices" + smellVertices);
        if (smellVertices != null) {
            for (Vertex v : smellVertices) {
                String shapeType = v.value(GraphBuilder.PROPERTY_SHAPE);
                try {
                    printer.print(v.id());
                    printer.print(shapeType);

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
