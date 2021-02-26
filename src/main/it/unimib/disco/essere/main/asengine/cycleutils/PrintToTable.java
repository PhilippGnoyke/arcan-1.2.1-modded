package it.unimib.disco.essere.main.asengine.cycleutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;

public class PrintToTable implements CyclePrinter {
    private static final Logger logger = LogManager.getLogger(PrintToTable.class);

    private List<Vertex> vertices = null;
    private FileWriter writer = null;
    private CSVFormat formatter = null;
    private CSVPrinter printer = null;

    private int cycleCounter = 0;

    public PrintToTable(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    @Override
    public void initializePrint(File path, String vertexType) {
        try {

            writer = new FileWriter(Paths.get(path.getAbsolutePath(), vertexType + "CyclicDependencyTable.csv")
                    .toAbsolutePath().toFile());
            String[] header = new String[vertices.size() + 1];

            for (int i = 0; i < header.length; ++i) {
                if (i == 0) {
                    header[i] = "Cycle";
                } else {
                    header[i] = vertices.get(i - 1).value(GraphBuilder.PROPERTY_NAME).toString();
                }
            }
            formatter = CSVFormat.EXCEL.withHeader(header);
            printer = new CSVPrinter(writer, formatter);

        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
        

    }

    @Override
    public void printCycles(Stack<Vertex> cycle) {

        logger.debug("***inside CSV method***");
        List<String> verticesInCycle = new ArrayList<>();
        int i = 0;
        for (Vertex vx : cycle) {
            verticesInCycle.add(vx.value(GraphBuilder.PROPERTY_NAME).toString());
            ++i;
        }
        logger.debug("***Collected vertex of the cycle " + i + "***");
        i = 0;
        try {
            logger.debug("printer " + printer);
            printer.print("Cycle" + cycleCounter);
            for (Vertex clazz : vertices) {
                if (verticesInCycle.contains(clazz.value(GraphBuilder.PROPERTY_NAME).toString())) {
                    printer.print(1);
                    i++;
                } else {
                    printer.print(0);
                }

            }
            printer.println();
            logger.debug("***Printed " + i + " vertex of the cycle " + cycleCounter + "***");
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
        logger.debug("***End of CSV method***");
        ++cycleCounter;

    }

    public void closePrint() {
        try {
            printer.close();
            writer.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void printCyclesFromGraph(Graph graph, List<Vertex> smellVertices) {
        if (smellVertices != null) {
            for (Vertex v : smellVertices) {
                printCycles(CDFilterUtils.getCycleVertices(graph, v));
            }
        } else {
            logger.info("***No cyclic dependency smell detected, nothing to print***");
        }
    }

}
