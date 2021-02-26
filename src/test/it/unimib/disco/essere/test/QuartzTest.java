package it.unimib.disco.essere.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.metricsengine.PackageMetricsCalculator;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByJar;

@RunWith(JUnit4.class)
public class QuartzTest {
    private static final Logger logger = LogManager.getLogger(QuartzTest.class);
    private static SystemBuilder sys = null;
    private static GraphBuilder graphB = null;
    private static GraphWriter graphW = null;
    private static Graph graph = null;
    private static PackageMetricsCalculator calc = null;
    private static UnstableDependencyDetector detector = null;
    private static String file = "C:/Users/Ilaria/Desktop/checkFiles/packagesInstability2.csv";

    @BeforeClass
    public static void setupGraph() {
        /*
         * sys = new SystemBuilderByUrl(
         * "C:/Users/Ilaria/Desktop/myprog/LabIngSoft/ToySystem/target/classes/it/unimib/disco/essere/toysystem"
         * );
         */
        sys = new SystemBuilderByJar();
        sys.readClass("C:/Users/Ilaria/Downloads/quartz-1.8.6/quartz-all-1.8.6.jar");
        graphB = new GraphBuilder(sys.getClassesHashMap(),sys.getPackagesHashMap());
        graphW = new Neo4JGraphWriter();
        graphW.setup("C:/Users/Ilaria/Desktop/myprog/LabIngSoft/quartzNeo4jDB");
        graph = graphW.init();
        graphB.createGraph(graph);
        graphW.write(graph, false);
        calc = new PackageMetricsCalculator(graph);
        detector = new UnstableDependencyDetector(graph, calc);
    }

  /*  @Test
    public void unstableDependencyDetectorTest() {
        try {
            Map<String, Double> instabilityMap = calc.calculatePackagesInstability();
            PrintWriter pw;
            pw = new PrintWriter(new File(file));
            Map<String, List<String>> smellMap = detector.detect();

            for (Entry<String, List<String>> entry : smellMap.entrySet()) {
                for (String interestedPackage : entry.getValue()) {
                    String r = "Package affected by unstable dependency smell:" + "\t" + entry.getKey() + "\t"
                            + "Instability:" + "\t" + instabilityMap.get(entry.getKey()) + "\t" + "Correlated package"
                            + "\t" + interestedPackage + "\t" + "Instability:" + "\t"
                            + instabilityMap.get(interestedPackage);
                    pw.write(r + "\n");
                }

            }
            pw.close();

        } catch (TypeVertexException | FileNotFoundException e) {
            logger.error(e.getMessage());
        }
    }*/

    @Test
    public void runTest() {
        int counterClass = 0;
        int counterPackage = 0;
        Iterator<Vertex> i = graph.vertices();
        while (i.hasNext()) {
            Vertex v = i.next();

            if (v.label().equals("class")) {
                ++counterClass;
            }

            if (v.label().equals("package")) {
                ++counterPackage;
            }
        }

        int counterChildren = 0;
        int counterDependences = 0;
        int counterPackageDependences = 0;
        int counterAfferenceDependences = 0;
        int counterEfferenceDependences = 0;

        Iterator<Edge> iter = graph.edges();
        while (iter.hasNext()) {
            Edge e = iter.next();
            String label = e.label();
            switch (label) {
            case "isChildOf":
                ++counterChildren;
                break;
            case "dependsOn":
                ++counterDependences;
                break;
            case "belongsTo":
                ++counterPackageDependences;
                break;
            case "isAfferentOf":
                ++counterAfferenceDependences;
                break;
            case "isEfferentOf":
                ++counterEfferenceDependences;
                break;
            }
        }

        System.out.println("counterClass: " + counterClass);
        System.out.println("counterPackage: " + counterPackage);
        System.out.println("counterChildren: " + counterChildren);
        System.out.println("counterDependences: " + counterDependences);
        System.out.println("counterPackageDependences: " + counterPackageDependences);
        System.out.println("counterAfferenceDependences: " + counterAfferenceDependences);
        System.out.println("counterEfferenceDependences: " + counterEfferenceDependences);

        /*
         * assertEquals(16, counterClass); assertEquals(5, counterPackage);
         * assertEquals(5, counterChildren); assertEquals(28,
         * counterDependences); assertEquals(16, counterPackageDependences);
         * assertEquals(11, counterAfferenceDependences); assertEquals(11,
         * counterEfferenceDependences);
         */
    }

    @Test
    public void metricsTest() {
        try {           
            Map<String, List<String>> smellMap = detector.detect();
            Set<String> packagesNames = smellMap.keySet();
            List<Vertex> packages = new ArrayList<>();
            
            for(String name : packagesNames){
                packages.add(GraphUtils.findVertex(graph, name,GraphBuilder.PACKAGE));
            }
            
            
            double a = 0.0;
            double af = 0.0;
            double d = 0.0;
            double e = 0.0;
            for (Vertex pkg : packages) {

                //a = calc.calculateAbstractness(pkg);
                //af = calc.calculateAfferentClasses(pkg);
                //d = calc.calculateDistanceFromTheMainSequence(pkg);
                e = calc.calculateEfferentClasses(pkg);
                
                System.out.println("package " + pkg.property("name").value().toString() + " " + e);
            }

        } catch (TypeVertexException e) {
            logger.error(e.getMessage());
        }
    }

    @AfterClass
    public static void closeGraph() {
        try {
            graph.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
