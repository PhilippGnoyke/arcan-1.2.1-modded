package it.unimib.disco.essere.main.asengine.cycleutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;

public class PrintToMatrix implements CyclePrinter {

    private static final Logger logger = LogManager.getLogger(PrintToMatrix.class);

    private List<Vertex> vertices = null;
    private FileWriter writer = null;
    private CSVFormat formatter = null;
    private CSVPrinter printer = null;

    private Map<String, List<Integer>> map = null;

    public PrintToMatrix(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    @Override
    public void initializePrint(File path, String vertexType) {

        try {
            writer = new FileWriter(Paths.get(path.getAbsolutePath(), vertexType + "CyclicDependencyMatrix.csv")
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

            map = new HashMap<>();

            for (Vertex v : vertices) {
                List<Integer> value = new ArrayList<Integer>(vertices.size());
                for (int i = 0; i < vertices.size(); ++i) {
                    value.add(0);
                }
                logger.debug("value name: " + v.value(GraphBuilder.PROPERTY_NAME).toString());
                map.put(v.value(GraphBuilder.PROPERTY_NAME).toString(), value);
            }

        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void printCycles(Stack<Vertex> cycle) {
        int c = 1;
        logger.debug("cycle size: " + cycle.size());
        logger.debug("CONTENT OF CYCLE: " + cycle);
        for (Vertex v : cycle) {
            List<Integer> temp = map.get(v.value(GraphBuilder.PROPERTY_NAME).toString());
            logger.debug("head: " + v.value(GraphBuilder.PROPERTY_NAME).toString());
            for (int i = c; i < cycle.size(); ++i) {
                int index = vertices.indexOf(cycle.get(i));
                logger.debug("Nome classe/package da trovre: " + cycle.get(i));
                logger.debug("index: " + index);
                logger.debug("tail: " + vertices.get(index).value(GraphBuilder.PROPERTY_NAME).toString());

                Integer temp2 = temp.get(index);

                if (temp2 != null) {
                    temp2 += 1;
                    temp.set(index, temp2);
                } else {
                    temp.set(index, 1);
                }
            }
            ++c;
            map.put(v.value(GraphBuilder.PROPERTY_NAME).toString(), temp);

        }
        logger.debug("fine ciclo");

    }

    public void closePrint() {
        for (String s : map.keySet()) {
            try {
                printer.print(s);
                for (Integer i : map.get(s)) {
                    if (i != null) {
                        printer.print(i);
                    } else {
                        printer.print(0);
                    }
                }
                printer.println();
            } catch (IOException e) {
                logger.debug(e.getMessage());
                e.printStackTrace();
            }
        }

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
