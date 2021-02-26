package it.unimib.disco.essere.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphReader;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;

public class CompareResults {
    private static final Logger logger = LogManager.getLogger(CompareResults.class);
    BufferedReader br = null;
    private FileWriter writer = null;
    private CSVFormat formatter = null;
    private CSVPrinter printer = null;
    private int cycleCounter = 0;
    private Graph graph = null;
    private List<Vertex> vertices = null;
    private List<Edge> edges = null;

    @Before
    public void readGraph() {
        GraphReader reader = new GraphReader(Paths.get("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/JeditDB"));
        // GraphReader reader = new
        // GraphReader(Paths.get("C:/Users/ricca/Documents/Neo4j/default.graphdb"));
        graph = reader.getGraph();
    }

    @Test
    public void run() {
        List<Vertex> internalPackages = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);
        vertices = GraphUtils.filterProperty(internalPackages, GraphBuilder.PROPERTY_PACKAGE_TYPE,
                GraphBuilder.SYSTEM_PACKAGE);
        csvToTable(Paths.get("C:", "Users", "Ilaria", "Desktop", "checkFiles", "JeditCaiMo.csv").toString(),
                Paths.get("C:", "Users", "Ilaria", "Desktop", "checkFiles", "Analyzed Projects",
                        "JeditCaiMoPackageCyclicDependency.csv").toString());
    }

    public void csvToTable(String fileToRead, String fileToWrite) {
        try {
            writer = new FileWriter(Paths.get(fileToWrite).toAbsolutePath().toFile());

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
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String line = "";
        String p1 = "";
        String p2 = "";

        try {
            br = new BufferedReader(new FileReader(fileToRead));
            int cycleCounter = 0;

            while ((line = br.readLine()) != null) {
                if (line.equals("<PackageCycle>")) {
                    printer.print("Cycle" + cycleCounter);

                    p1 = br.readLine();
                    p2 = br.readLine();
                    for (Vertex clazz : vertices) {
                        
                        if (p1.substring(p1.indexOf("org"), p1.length() - 1)
                                .equals(clazz.value(GraphBuilder.PROPERTY_NAME).toString())
                                || p2.substring(p2.indexOf("org"), p2.length() - 1)
                                        .equals(clazz.value(GraphBuilder.PROPERTY_NAME).toString())) {
                            printer.print(1);
                        } else {
                            printer.print(0);
                        }
                    }
                }
                ++cycleCounter;
                printer.println();
            }
            br.close();
            printer.close();
            writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    

    @After
    public void closeGraph() {
        try {
            graph.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
