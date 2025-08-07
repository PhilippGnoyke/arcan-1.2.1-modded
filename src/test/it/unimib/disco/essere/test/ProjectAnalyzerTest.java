package it.unimib.disco.essere.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import groovy.json.internal.Value;
import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphReader;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.ClassMetricsCalculator;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByFolderOfJars;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByJar;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

public class ProjectAnalyzerTest {
    private static final Logger logger = LogManager.getLogger(ProjectAnalyzerTest.class);
    private SystemBuilder sys = null;
    private static GraphBuilder graphB = null;
    private static GraphWriter graphW = null;
    private static Graph graph = null;
    private static PackageMetricsCalculator packageCalc = null;
    private static ClassMetricsCalculator classCalc = null;
    private static UnstableDependencyDetector UDDetector = null;
    private static HubLikeDetector HLDetector = null;
    private static CyclicDependencyDetector CDDetector = null;

    public void setupGraph(SystemBuilder sys, String url) {
        // sys = new SystemBuilderByUrl(
        // "C:/gittest/ant-1.8.2/bin");

        sys.readClass(url);
        graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap());        

        graphW = new Neo4JGraphWriter();

        int lastIndex = url.lastIndexOf('\\');
        url = url.substring(44, lastIndex);
        graphW.setup("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/" + url + "DB");
        // graphW.setup("C:/Users/ricca/Documents/Neo4j/default.graphdb");
        graph = graphW.init();
        logger.info("***Graph initializated***");

        graphB.createGraph(graph);
        logger.info("***End of graph building***");

        graphW.write(graph, false);
        // logger.debug("Graph written into DB");

        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HLDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, null);
        logger.info("***End setup***");
    }

    public void readGraph(String dbName) {
        GraphReader reader = new GraphReader(Paths.get("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/" + dbName + "DB"));
        // GraphReader reader = new
        // GraphReader(Paths.get("C:/Users/ricca/Documents/Neo4j/default.graphdb"));
        graph = reader.getGraph();
        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("C:/Users/Ilaria/Desktop/JunitCycles").toFile());
        HLDetector = new HubLikeDetector(graph, classCalc);
    }

  
    public void runTestWithBuild() {
        // String path = "C:/Users/Ilaria/Downloads/qualitas
        // corpus/picocontainer-2.10.2/org/picocontainer";
        SystemBuilder jarSys = new SystemBuilderByJar();
        SystemBuilder urlSys = new SystemBuilderByUrl();
        SystemBuilder folderJarSys = new SystemBuilderByFolderOfJars();

        try {
            File file = new File("C:/Users/Ilaria/Downloads/ProjectsToAnalyze/projectsToAnalyze.txt");
            // File file = new
            // File("C:/Users/Ilaria/Downloads/ProjectsToAnalyze/folderJarsTest.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            Map<String, Map<String, Integer[]>> allStatistics = new HashMap<>();

            while ((line = bufferedReader.readLine()) != null) {
                if (line.endsWith("JR")) {
                    setupGraph(jarSys, line.substring(0, line.length() - 2));
                    sys = jarSys;
                }
                if (line.endsWith("CL")) {
                    setupGraph(urlSys, line.substring(0, line.length() - 2));
                    sys = urlSys;
                }
                if (line.endsWith("FJ")) {
                    setupGraph(folderJarSys, line.substring(0, line.length() - 2));
                    sys = folderJarSys;
                }

                int lastIndex = line.lastIndexOf('\\');
                line = line.substring(44, lastIndex);

                startAnalysis(line, allStatistics);

            }
            fileReader.close();
            printStatistics(allStatistics);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void runTestRead() {
        try {
            File file = new File("C:/Users/Ilaria/Downloads/ProjectsToAnalyze/projectsToAnalyze.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            Map<String, Map<String, Integer[]>> allStatistics = new HashMap<>();

            while ((line = bufferedReader.readLine()) != null) {

                int lastIndex = line.lastIndexOf('\\');
                line = line.substring(44, lastIndex);

                readGraph(line);

                startAnalysis(line, allStatistics);

            }

            fileReader.close();
            //printStatistics(allStatistics);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startAnalysis(String line, Map<String, Map<String, Integer[]>> allStatistics) {
        logger.info("***Start " + line + " analysis***");

        Map<String, Integer[]> stats = calculateUnstableDependencyStatistics(line);
        allStatistics.put(line, stats);
        // calculatePackageMetricsTest(line);
        // calculateClassMetricsTest(line);
        //unstableDependencyDetectorTest(line);
         //hubLikeDetectorTest(line);
         cyclicDependencyDetectorTest(line);

        logger.info("***End project analysis***");

        closeGraph();
    }

    public Map<String, Integer[]> calculateUnstableDependencyStatistics(String fileName) {
        Map<String, Integer[]> stats = new HashMap<String, Integer[]>();
        try {
            stats = UDDetector.getStatistics();
        } catch (TypeVertexException e) {
            logger.debug(e.getMessage());
        }
        return stats;
    }

    public void printStatistics(Map<String, Map<String, Integer[]>> allStatistics) {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("System", "Package", "Num Bad Dependences",
                "Total Dependences", "Bad Dep./Total Dep.");

        FileWriter writer;

        try {
            writer = new FileWriter(Paths.get("C:/Users/Ilaria/Desktop/checkFiles/UDStats/UDStats.csv").toFile());

            CSVPrinter printer = new CSVPrinter(writer, formatter);

            for (Entry<String, Map<String, Integer[]>> e : allStatistics.entrySet()) {
                for (Entry<String, Integer[]> e1 : e.getValue().entrySet()) {
                    printer.print(e.getKey());
                    printer.print(e1.getKey());
                    printer.print(e1.getValue()[0]);
                    printer.print(e1.getValue()[1]);
                    printer.print(e1.getValue()[2]);

                    printer.println();
                }
            }

            printer.close();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void calculatePackageMetricsTest(String fileName) {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Package", "CA", "CE", "RMI", "RMA", "RMD");

        FileWriter writer;
        try {
            writer = new FileWriter(
                    Paths.get("C:/Users/Ilaria/Desktop/checkFiles/Analyzed Projects/" + fileName + "PackageMetrics.csv")
                            .toFile());
            CSVPrinter printer = new CSVPrinter(writer, formatter);

            List<Vertex> projectPackages = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);

            for (Vertex pkg : projectPackages) {
                if (pkg.property(GraphBuilder.PROPERTY_PACKAGE_TYPE).value().equals(GraphBuilder.SYSTEM_PACKAGE)) {
                    double[] metrics;
                    metrics = packageCalc.calculateInternalPackageMetrics(pkg);
                    String n = pkg.value(GraphBuilder.PROPERTY_NAME);
                    if ("".equals(n)) {
                        n = GraphBuilder.DEFAULT_PACKAGE;
                    }
                    printer.print(n);
                    for (Double m : metrics) {
                        if (m != null) {
                            printer.print(m);
                        }
                    }
                    printer.println();
                }
            }
            printer.close();
            writer.close();
        } catch (IOException | TypeVertexException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }

    }

    public void calculateClassMetricsTest(String fileName) {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Class", "FI", "FO", "CBO", "LCOM");

        FileWriter writer;

        try {
            writer = new FileWriter(
                    Paths.get("C:/Users/Ilaria/Desktop/checkFiles/Analyzed Projects/" + fileName + "ClassesMetrics.csv")
                            .toFile());
            CSVPrinter printer = new CSVPrinter(writer, formatter);

            List<JavaClass> projectClasses = sys.getClasses();

            for (JavaClass clazz : projectClasses) {
                String className = clazz.getClassName();

                int fanIn = classCalc.calculateFanIn(className);
                int fanOut = classCalc.calculateFanOut(className);
                int cbo = classCalc.calculateCBO(className);
                double lcom = classCalc.calculateLCOM(clazz);

                printer.print(className);
                printer.print(fanIn);
                printer.print(fanOut);
                printer.print(cbo);
                printer.print(lcom);

                printer.println();
            }
            printer.close();
            writer.close();
        } catch (IOException | TypeVertexException e) {
            logger.debug(e.getMessage());
        }

    }

    public void unstableDependencyDetectorTest(String fileName) {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Package Affected", "Instability", "Correlated Package",
                "Correlated Instability");

        FileWriter writer;

        try {
            writer = new FileWriter(Paths
                    .get("C:/Users/Ilaria/Desktop/checkFiles/Analyzed Projects/" + fileName + "UnstableDependeces.csv")
                    .toFile());
            CSVPrinter printer = new CSVPrinter(writer, formatter);
            Map<String, Double> instabilityMap = packageCalc.calculatePackagesInstability();

            Map<String, List<String>> smellMap = UDDetector.detect();

            for (Entry<String, List<String>> entry : smellMap.entrySet()) {
                for (String interestedPackage : entry.getValue()) {
                    printer.print(entry.getKey());
                    printer.print(instabilityMap.get(entry.getKey()));
                    printer.print(interestedPackage);
                    printer.print(instabilityMap.get(interestedPackage));
                    printer.println();
                }

            }
            printer.close();
            writer.close();

        } catch (TypeVertexException | IOException e) {
            logger.debug(e.getMessage());
        }
    }

    public void hubLikeDetectorTest(String fileName) {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Class", "FanIn", "FanOut", "Total Dependences");

        FileWriter writer;

        try {
            writer = new FileWriter(
                    Paths.get("C:/Users/Ilaria/Desktop/checkFiles/Analyzed Projects/" + fileName + "HubLikeClasses.csv")
                            .toFile());
            CSVPrinter printer = new CSVPrinter(writer, formatter);

            Map<String, List<Integer>> hubLikeClasses = HLDetector.detect();

            for (Entry<String, List<Integer>> e : hubLikeClasses.entrySet()) {
                printer.print(e.getKey());
                printer.print(e.getValue().get(1));
                printer.print(e.getValue().get(2));
                printer.print(e.getValue().get(0));
                printer.println();
            }
            printer.close();
            writer.close();

        } catch (IOException | TypeVertexException e) {
            logger.debug(e.getMessage());
        }

    }

    public void cyclicDependencyDetectorTest(String fileName) {

        
        CDDetector.detect();
    }

    public void closeGraph() {
        try {
            graph.close();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }

}
