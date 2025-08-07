package it.unimib.disco.essere.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.asengine.cycleutils.CDFilterUtils;
import it.unimib.disco.essere.main.asengine.cycleutils.CyclePrinter;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintShapes;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintToMatrix;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintToTable;
import it.unimib.disco.essere.main.asengine.filters.CDShapeFilter;
import it.unimib.disco.essere.main.asengine.filters.UDRateFilter;
import it.unimib.disco.essere.main.asengine.udutils.UDPrinter;
import it.unimib.disco.essere.main.asengine.udutils.UDUtils;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphReader;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.ClassMetricsCalculator;
import it.unimib.disco.essere.main.metricsengine.MetricsUploader;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByFolderOfJars;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByJar;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;
import it.unimib.disco.essere.utils.XMLReader;

@RunWith(JUnit4.class)
public class QualitasCorpusTest {
    private static final Logger logger = LogManager.getLogger(QualitasCorpusTest.class);
    private static XMLReader reader = new XMLReader();
    private static SystemBuilder sys = null;
    private static GraphBuilder graphB = null;
    private static GraphWriter graphW = null;
    private static Graph graph = null;
    private static PackageMetricsCalculator packageCalc = null;
    private static ClassMetricsCalculator classCalc = null;
    private static UnstableDependencyDetector UDDetector = null;
    private static HubLikeDetector HBDetector = null;
    private static CyclicDependencyDetector CDDetector = null;
    // private static String file = "C:/Users/Ilaria/Desktop/checkFiles/Analyzed
    // Projects/Apache-Maven results/";
    private static String file = "C:/Users/ipiga/Desktop/Hadoop Arcan/ArcanOutput3/";

    
    public static void setupGraph() {
        // sys = new SystemBuilderByUrl(
        // "C:/gittest/ant-1.8.2/bin");

        sys = new SystemBuilderByFolderOfJars();
        // sys.readClass("C:/Users/Ilaria/Downloads/qualitasCorpus/quartz-1.8.6/quartz-all-1.8.6.jar");
        sys.readClass("D:/LaboratorioESSeRE/ProjectsToAnalyze/hadoop-1.1.2/jars");
        // sys.readClass("C:/Users/Ilaria/Downloads/ProjectsToAnalyze/apache-maven-3.0.5/lib");
        // sys.readClass("C:/User/Ilaria/Downloads/ProjectsToAnalyze/struts-2.2.1/bin");

        graphB = new GraphBuilder(sys.getClassesHashMap(), sys.getPackagesHashMap());

        graphW = new Neo4JGraphWriter();
        graphW.setup("C:/Users/ipiga/Desktop/Hadoop Arcan/Hadoop3DB");
        // graphW.setup("C:/Users/ricca/Documents/Neo4j/default.graphdb");
        graph = graphW.init();
        logger.debug("Graph initializated");

        graphB.createGraph(graph);
        logger.debug("End of graph building");

        graphW.write(graph, false);
        logger.debug("Graph written into DB");

        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HBDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("C:/Users/ipiga/Desktop/Hadoop Arcan/ArcanOutput3").toFile());

        logger.debug("End setup");

        /*
         * OutputStream out; try { out = new
         * FileOutputStream("Hadoop-1.1.2.graphml");
         * graph.io(IoCore.graphml()).writer().normalize(true).create().
         * writeGraph(out, graph); } catch (IOException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); }
         */

    }
    


    @BeforeClass
    public static void readGraph() {
        logger.info("***Start Graph Reading***");
        GraphReader reader = new GraphReader(Paths.get("D:/Arcan/Hadoop Arcan/HadoopDB"));
        // GraphReader reader = new
        // GraphReader(Paths.get("C:/Users/ricca/Documents/Neo4j/default.graphdb"));
        graph = reader.getGraph();
        graphW = new Neo4JGraphWriter();
        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HBDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, Paths.get("D:/Arcan/Hadoop Arcan/ArcanOutput3").toFile());
        logger.debug("End setup");

    }

    public void instabilityUpdateTest() {
        MetricsUploader m = new MetricsUploader(graph);
        try {
            m.updateInstability();
            graphW.write(graph, false);
        } catch (TypeVertexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public void unstableDependencyDetectorBySmellVertexTest() {
        try {
            MetricsUploader m = new MetricsUploader(graph);
            m.updateInstability();
            graphW.write(graph, false);

            UDUtils.cleanUDDetection(graph);
            UDDetector.newDetect();
            graphW.write(graph, false);

            Map<String, List<String>> results = UDDetector.getSmellMap();

            UDPrinter p = new UDPrinter(Paths.get("C:/Users/ipiga/Desktop/Hadoop Arcan/ArcanOutput3").toFile(), UDDetector);

            p.print(results);

            p.closeAll();

            UDRateFilter filter = new UDRateFilter(graph);

            logger.debug("***filtering***");
            UDPrinter p2 = new UDPrinter(Paths.get("C:/Users/ipiga/Desktop/Hadoop Arcan/ArcanOutput3/filtered").toFile(),
                    UDDetector);
            p2.print(filter.filter(30));
            p2.closeAll();

        } catch (IOException | TypeVertexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void cyclicDependencyDetectorTest() {
        CDFilterUtils.cleanCDShapeFilter(graph);
        CDFilterUtils.cleanCDDetection(graph);
        CDDetector.detect();
        graphW.write(graph, false);

        CDShapeFilter filter = new CDShapeFilter(graph);

        filter.getCircleCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getCircleCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);

        filter.getCliqueCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getCliqueCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);

        filter.getStarAndChainCycles(GraphBuilder.CLASS);
     
        filter.getStarAndChainCycles(GraphBuilder.PACKAGE);
        
        graphW.write(graph, false);
 
        List<Vertex> classes = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        List<Vertex> packages = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);

        CyclePrinter printer = new PrintToMatrix(classes);
        CyclePrinter printer2 = new PrintToTable(classes);
        CyclePrinter printerShape = new PrintShapes();

        printer.initializePrint(Paths.get("D:/Arcan/Hadoop Arcan/ArcanOutput3").toFile(), GraphBuilder.CLASS);
        printer2.initializePrint(Paths.get("C:/Users/ipiga/Desktop/Hadoop Arcan/ArcanOutput3").toFile(), GraphBuilder.CLASS);
        printerShape.initializePrint(Paths.get("D:/Arcan/Hadoop Arcan/ArcanOutput3/filtered").toFile(), GraphBuilder.CLASS);

        printer.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.CLASS));
        printer2.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.CLASS));
        
        logger.debug("lista di shape: " + GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.CLASS));
        printerShape.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.CLASS));
        

        printer.closePrint();
        printer2.closePrint();
        printerShape.closePrint();

        CyclePrinter printer3 = new PrintToMatrix(packages);
        CyclePrinter printer4 = new PrintToTable(packages);
        CyclePrinter printerShape2 = new PrintShapes();

        printer3.initializePrint(Paths.get("D:/Arcan/Hadoop Arcan/ArcanOutput3").toFile(), GraphBuilder.PACKAGE);
        printer4.initializePrint(Paths.get("D:/Arcan/Hadoop Arcan/ArcanOutput3").toFile(), GraphBuilder.PACKAGE);
        printerShape2.initializePrint(Paths.get("D:/Arcan/Hadoop Arcan/ArcanOutput3/filtered").toFile(),
                GraphBuilder.PACKAGE);

        printer3.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));
        printer4.printCyclesFromGraph(graph, CDDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));
        printerShape2.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.PACKAGE));

        printer3.closePrint();
        printer4.closePrint();
        printerShape2.closePrint();
        
        logger.debug("***End of shape filtering and printing***");
    }

 
    public void hubLikeDetectorTest() {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Class", "FanIn", "FanOut", "Total Dependences");

        FileWriter writer;

        try {
            writer = new FileWriter(
                    Paths.get("C:/Users/ipiga/Desktop/Hadoop Arcan/ArcanOutput3/" + "HubLikeClasses.csv").toFile());
            CSVPrinter printer = new CSVPrinter(writer, formatter);

            Map<String, List<Integer>> hubLikeClasses = HBDetector.detect();

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

    public void getNumberOfDependencesTest() {
        List<Vertex> classesVertex = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        for (Vertex classVertex : classesVertex) {
            int numDependences = classCalc.calculateFanIn(classVertex) + classCalc.calculateFanOut(classVertex);

        }
    }

    public void unstableDependencyDetectorTest() {
        try {
            Map<String, Double> instabilityMap = packageCalc.calculatePackagesInstability();
            PrintWriter pw;
            pw = new PrintWriter(new File(file + "UnstableDependency.csv"));
            Map<String, List<String>> smellMap = UDDetector.detect();

            for (Entry<String, List<String>> entry : smellMap.entrySet()) {
                for (String interestedPackage : entry.getValue()) {
                    String r = "Package affected by unstable dependency smell:" + "," + entry.getKey() + ","
                            + "Instability:" + "," + instabilityMap.get(entry.getKey()) + "," + "Correlated package"
                            + "," + interestedPackage + "," + "Instability:" + ","
                            + instabilityMap.get(interestedPackage);
                    pw.write(r + "\n");
                }
            }
            pw.close();

        } catch (TypeVertexException | FileNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    public static void main(String[] args) {
        logger.info("***Start Graph Reading***");
        GraphReader reader = new GraphReader(
                Paths.get("C:/Users/ipiga/Desktop/myprog/LabIngSoft/springframework-3.0.5DB"));
        // GraphReader reader = new
        // GraphReader(Paths.get("C:/Users/ricca/Documents/Neo4j/default.graphdb"));
        graph = reader.getGraph();
        packageCalc = new PackageMetricsCalculator(graph);
        classCalc = new ClassMetricsCalculator(graph);
        UDDetector = new UnstableDependencyDetector(graph, packageCalc);
        HBDetector = new HubLikeDetector(graph, classCalc);
        CDDetector = new CyclicDependencyDetector(graph, new File("empty"));
        logger.debug("End setup");

        CDDetector.detect();
    }

    public void findNulls() {
        Iterator<Vertex> i = graph.vertices();
        while (i.hasNext()) {
            Vertex v = i.next();
            if (v == null) {
                logger.debug("JUMBO");
            }
            // logger.debug("vertex id: " + v.id() + " Type: " + v.label() + "
            // name: " + v.value(GraphBuilder.PROPERTY_NAME));
        }
        List<Vertex> vcs = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        for (Vertex v : vcs) {
            if (v == null) {
                logger.debug("JUMBO");
            }
            for (String s : v.keys()) {
                if (v.value(s) == null || "".equals(v.value(s)) || s == null) {
                    logger.debug("JUMBO " + s);
                }
                // logger.debug("" + s + ": "+v.value(s));
            }
            // logger.debug("ClassType" + ": "+v.value("ClassType"));
            // logger.debug("JUMBO " + v.value(GraphBuilder.PROPERTY_NAME));
        }
        List<Vertex> vc = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);
        for (Vertex v : vc) {
            if (v == null) {
                logger.debug("JUMBO");
            }
            for (String s : v.keys()) {
                if (v.value(s) == null || "".equals(v.value(s)) || s == null) {
                    logger.debug("JUMBO " + s);
                }
            }
            // logger.debug("JUMBO " + v.value(GraphBuilder.PROPERTY_NAME));
        }
        List<Edge> desge = GraphUtils.findEdgesByLabel(graph, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        for (Edge e : desge) {
            if (e == null) {
                logger.debug("JUMBO");
            }
            for (String s : e.keys()) {
                if (e.value(s) == null || "".equals(e.value(s)) || s == null) {
                    logger.debug("JUMBO " + s);
                }
            }
            // logger.debug(e.inVertex() + " " + e.outVertex() + "in
            // null?"+(e.inVertex()==null)+ "out null?"+(e.outVertex()==null));
            // logger.debug("JUMBO " + v.value(GraphBuilder.PROPERTY_NAME));
        }
        List<Edge> pesge = GraphUtils.findEdgesByLabel(graph, GraphBuilder.LABEL_PACKAGE_AFFERENCE);
        for (Edge e : pesge) {
            if (e == null) {
                logger.debug("JUMBO");
            }
            for (String s : e.keys()) {
                if (e.value(s) == null || "".equals(e.value(s)) || s == null) {
                    logger.debug("JUMBO " + s);
                }
            }
            logger.debug(e.inVertex() + " " + e.outVertex() + "\tin null?" + (e.inVertex() == null) + "\tout null?"
                    + (e.outVertex() == null));
            // logger.debug("JUMBO " + v.value(GraphBuilder.PROPERTY_NAME));
        }
    }

    public void runTest() throws IOException {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader("key", "CA", "QS_CA", "Comparison_CA", "CE", "QS_CE",
                "Comparison_CE", "RMI", "QS_RMI", "Comparison_RMI", "RMA", "QS_RMA", "Comparison_RMA", "RMD", "QS_RMD",
                "Comparison_RMD");

        FileWriter writer = new FileWriter(
                Paths.get("C:/Users/Ilaria/Desktop/checkFiles/QualitasCorpusComparisonPico_4.csv").toFile());
        // FileWriter writer = new FileWriter(
        // Paths.get("C:/gittest/qualitasCorpusComparison.csv").toFile());
        CSVPrinter printer = new CSVPrinter(writer, formatter);

        // List<Vertex> projectClasses = GraphUtils.findVertexByLabel(graph,
        // GraphBuilder.CLASS);
        List<Vertex> projectPackages = GraphUtils.findVerticesByLabel(graph, GraphBuilder.PACKAGE);

        Map<String, double[]> packageMetricsMap = new HashMap<>();
        // Map<String, Double> classMetricsMap = new HashMap<>();

        for (Vertex pkg : projectPackages) {
            if (pkg.property(GraphBuilder.PROPERTY_PACKAGE_TYPE).value().equals(GraphBuilder.SYSTEM_PACKAGE)) {
                try {
                    double[] metrics;
                    metrics = packageCalc.calculateInternalPackageMetrics(pkg);
                    String n = pkg.property(GraphBuilder.PROPERTY_NAME).value().toString();
                    if ("".equals(n)) {
                        n = GraphBuilder.DEFAULT_PACKAGE;
                    }
                    packageMetricsMap.put(n, metrics);
                } catch (TypeVertexException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        /*
         * for (JavaClass clazz : sys.getClasses()) { Double metric; metric =
         * (Double) calc.calculateLCOM(clazz);
         * classMetricsMap.put(clazz.getClassName(), metric); }
         */

        Map<String, Map<String, String>> QCpackageMetricsMap = getQCMetrics();
        logger.debug(packageMetricsMap.keySet());
        logger.debug(QCpackageMetricsMap.keySet());
        Iterator<String> h = QCpackageMetricsMap.keySet().iterator();
        String[] metrics = { "CA", "CE", "RMI", "RMA", "RMD" };
        while (h.hasNext()) {
            String key = h.next();
            String s = key + ",";
            int i = 0;
            for (String m : metrics) {
                double[] vd1 = packageMetricsMap.get(key);
                Map<String, String> vd2 = QCpackageMetricsMap.get(key);

                logger.debug("chiave: [" + key + "] valore [" + QCpackageMetricsMap.get(key) + "]" + " ");

                if (vd1 != null && vd2 != null) {
                    String d1 = String.valueOf(vd1[i]);
                    String d2 = Double.valueOf(vd2.get(m)).toString();
                    s += d1 + "," + d2 + "," + (d1.equals(d2) + ",");
                }
                ++i;
            }
            printer.print(s);
            printer.println();
            logger.info(s);
        }
        printer.close();
        writer.close();
    }

    public Map<String, Map<String, String>> getQCMetrics() {
        reader.setReader(
                Paths.get("C:", "Users", "Ilaria", "Downloads", "qualitas corpus", "metrics", "picocontainer-2.10.2")
                        .toString()
        // Paths.get("C:", "gittest", "metrics", "ant-1.8.2").toString()
        );// C:\gittest\metrics\ant-1.8.2

        Map<String, Map<String, String>> metricMap = new HashMap<>();
        reader.readXML("/Metrics/Metric[@id = 'CA']/Values/Value", metricMap);
        reader.readXML("/Metrics/Metric[@id = 'CE']/Values/Value", metricMap);
        reader.readXML("/Metrics/Metric[@id = 'RMI']/Values/Value", metricMap);
        reader.readXML("/Metrics/Metric[@id = 'RMA']/Values/Value", metricMap);
        reader.readXML("/Metrics/Metric[@id = 'RMD']/Values/Value", metricMap);
        return metricMap;

    }

    public void findDuplicatesTest() {
        Path path = Paths.get("C:", "Users", "Ilaria", "Downloads", "qualitas corpus", "metrics");

        Stream<Path> stream;
        try {
            stream = Files.walk(path);

            stream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    reader.setReader(filePath.toString());

                    int index = filePath.toString().lastIndexOf('\\');
                    String projectName = filePath.toString().substring(index);
                    boolean duplication = false;
                    boolean duplication1 = false;
                    boolean duplication2 = false;
                    boolean duplication3 = false;
                    boolean duplication4 = false;

                    duplication = reader.findDuplicates("/Metrics/Metric[@id = 'CA']/Values/Value", projectName);
                    duplication1 = reader.findDuplicates("/Metrics/Metric[@id = 'CE']/Values/Value", projectName);
                    duplication2 = reader.findDuplicates("/Metrics/Metric[@id = 'RMI']/Values/Value", projectName);
                    duplication3 = reader.findDuplicates("/Metrics/Metric[@id = 'RMA']/Values/Value", projectName);
                    duplication4 = reader.findDuplicates("/Metrics/Metric[@id = 'RMD']/Values/Value", projectName);

                    logger.info("project " + projectName + " "
                            + ((duplication && duplication1 && duplication2 && duplication3 && duplication4) == false));
                    logger.info("- 0 " + duplication + " - 1 " + duplication1 + " - 2 " + duplication2 + " - 3 "
                            + duplication3 + " - 4 " + duplication4);
                    logger.info("");
                }
            });
            stream.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * <Metric id = "CA" description ="Afferent Coupling"> <Metric id = "CE"
     * description ="Efferent Coupling"> <Metric id = "RMI" description
     * ="Instability"> <Metric id = "RMA" description ="Abstractness"> <Metric
     * id = "RMD" description ="Normalized Distance"> <Metric id = "LCOM"
     * description ="Lack of Cohesion of Methods"> <Metric id = "NOC"
     * description ="Number of Classes"> <Metric id = "NOI" description =
     * "Number of Interfaces"> <Metric id = "NOP" description =
     * "Number of Packages">
     */

    @AfterClass
    public static void closeGraph() {
        try {
            if(graph != null){
            graph.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
