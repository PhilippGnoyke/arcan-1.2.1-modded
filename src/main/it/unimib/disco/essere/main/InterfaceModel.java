package it.unimib.disco.essere.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import it.unimib.disco.essere.main.asengine.SuperCycleDetector;
import it.unimib.disco.essere.main.metricsengine.*;
import it.unimib.disco.essere.main.tdengine.PageRankCalculator;
import it.unimib.disco.essere.main.tdengine.TdSmellCalculator;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.asengine.cycleutils.CDFilterUtils;
import it.unimib.disco.essere.main.asengine.cycleutils.CyclePrinter;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintShapesDocker;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintToMatrix;
import it.unimib.disco.essere.main.asengine.cycleutils.PrintToTable;
import it.unimib.disco.essere.main.asengine.filters.CDShapeFilter;
import it.unimib.disco.essere.main.asengine.filters.UDRateFilter;
import it.unimib.disco.essere.main.asengine.udutils.UDPrinter;
import it.unimib.disco.essere.main.asengine.udutils.UDUtils;
import it.unimib.disco.essere.main.graphmanager.EmptyProjectException;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphReader;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.GraphWriter;
import it.unimib.disco.essere.main.graphmanager.Neo4JGraphWriter;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilder;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByFolderOfJars;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByJar;
import it.unimib.disco.essere.main.systemreconstructor.SystemBuilderByUrl;

public class InterfaceModel
{
    private static final Logger logger = LogManager.getLogger(InterfaceModel.class);
    private Graph graph = null;

    SystemBuilder _sys = null;
    private PackageMetricsCalculator _metricsCalculator = null;
    private ClassMetricsCalculator _classMetricsCalculator = null;
    private UnstableDependencyDetector _unstableDependencyDetector = null;
    private CyclicDependencyDetector _cycleDetector = null;
    private HubLikeDetector _hubLikeDetector = null;
    private SuperCycleDetector _superCycleDetector = null; // Modded
    private ProjectMetricsCalculator _projectMetricsCalculator = null; // Modded

    private boolean _jarMode = false;
    private boolean _classMode = false;
    private boolean _jarsFolderMode = false;
    private boolean _asTdEvolution = false; // Modded
    private boolean _suppressNonAsTdEvolution = false; // Modded
    private int classCount; // Modded
    private int packCount; // Modded
    private List<Vertex> classes; // Modded
    private List<Vertex> packages; // Modded
    private List<Vertex> classCds; // Modded
    private List<Vertex> packCds; // Modded
    private List<Vertex> hds; // Modded
    private List<Vertex> uds; // Modded
    private List<Vertex> classSupercycles; // Modded
    private List<Vertex> packSupercycles; // Modded

    private File _dbFolder;
    private File _projectFolder;
    private File _outDir;

    private OutputDirUtils outputDirUtils; // Modded

    private static final String FILE_HUB_LIKE = "HL.csv";

    private static final String FILE_CYCLE = "CL.csv";
    private static final String FILE_UNSTABLE_DEPENDECY = "UD.csv";
    private static final String FILE_UNSTABLE_DEPENDECY_FILTERED_30 = "UD30.csv";
    private static final String FILE_PACKAGE_METRICS = "PM.csv";
    private static final String FILE_CLASS_METRICS = "CM.csv";


    public InterfaceModel(OutputDirUtils outputDirUtils)
    {
        this.outputDirUtils = outputDirUtils;
    }

    public void setProjectFolder(File projectFolder)
    {
        _projectFolder = projectFolder;
    }

    public final void setDbFolder(File dbFolder)
    {
        _dbFolder = dbFolder;
    }

    public final void setOutputFolder(File outDir)
    {
        _outDir = outDir;
    }

    public void set_jarMode(boolean jarMode)
    {
        _jarMode = jarMode;
    }

    public void set_classMode(boolean classMode)
    {
        _classMode = classMode;
    }

    public void set_jarsFolderMode(boolean jarsFolderMode)
    {
        _jarsFolderMode = jarsFolderMode;
    }

    public void set_asTdEvolution(boolean _asTdEvolution)
    {
        this._asTdEvolution = _asTdEvolution;
    }

    public void set_suppressNonAsTdEvolution(boolean _suppressNonAsTdEvolution)
    {
        this._suppressNonAsTdEvolution = _suppressNonAsTdEvolution;
    }

    public Graph getGraph()
    {
        return graph;
    }

    public boolean buildProjectNeo4J() throws EmptyProjectException
    {
        logger.info("***Start graph building***");

        GraphBuilder graphB = null;
        GraphWriter graphW = null;

        if (!_jarsFolderMode && !_jarMode && _classMode)
        {
            _sys = new SystemBuilderByUrl();
        }
        if (_jarMode)
        {
            _sys = new SystemBuilderByJar();
        }
        if (_jarsFolderMode)
        {
            _sys = new SystemBuilderByFolderOfJars();
        }
        logger.debug("***Start graph building*** - " + _projectFolder.toString());
        _sys.readClass(_projectFolder.toString());
        if (_sys.getClassesHashMap().isEmpty())
        {
            throw new EmptyProjectException("No files to read founded");
        }
        graphB = new GraphBuilder(_sys.getClassesHashMap(), _sys.getPackagesHashMap(), _asTdEvolution);
        graphW = new Neo4JGraphWriter();
        logger.info("***Start Writing Neo4j***");
        logger.debug("***Start Writing Neo4j*** - " + _dbFolder.toPath());
        graphW.setup(_dbFolder.toPath().toAbsolutePath().toString());
        graph = graphW.init();
        logger.info("***Graph initializated***");
        logger.debug("***Graph initializated*** - graph:   " + graph);
        logger.debug("***Graph initializated*** - builder: " + graphB);
        graphB.createGraph(graph);
        logger.info("***Graph readed from compiled file***");
        initClassAndPackVars(graphB); // Modded
        computeAndStoreClassesMetrics();
        computeAndStorePackageMetrics();
        graphW.write(graph, false);

        // data._arcanSubfolder = projectFolder.getAbsolutePath() +
        // ARCAN_OUTPUT_URL;
        // createOutputDir(data._arcanSubfolder);
        // createOutputDir(data._arcanSubfolder + FILTERED_URL);
//		if (_outDir != null) {
//			OutputDirUtils.createDirFullPath(_outDir, _jarMode);
//		} else {
//			OutputDirUtils.createDir(_projectFolder, _jarMode);
//		}
        logger.info("***End of graph building***");
        return true;

    }

    public void buildGraphTinkerpop()
    {
        logger.info("***Start graph building*** - " + _projectFolder.toString());
        GraphBuilder graphB = null;
        if (!_jarsFolderMode && !_jarMode && _classMode)
        {
            _sys = new SystemBuilderByUrl();
        }
        if (_jarMode)
        {
            _sys = new SystemBuilderByJar();
        }
        if (_jarsFolderMode)
        {
            _sys = new SystemBuilderByFolderOfJars();
        }
        logger.info("***Start graph building*** - " + _sys);
        _sys.readClass(_projectFolder.toString());
        graphB = new GraphBuilder(_sys.getClassesHashMap(), _sys.getPackagesHashMap(),_asTdEvolution);
        // _sys = null;
        logger.info("***Start Opening Tinkerpop***");
        graph = TinkerGraph.open();
        logger.info("***Graph initializated*** - graph:   " + graph);
        logger.info("***Graph initializated*** - builder: " + graphB);
        graphB.createGraph(graph);
        logger.info("***Graph created from compiled file*** - graph:" + graph);
        initClassAndPackVars(graphB); // Modded
        computeAndStoreClassesMetrics();
        computeAndStorePackageMetrics();
        logger.info("***End of graph building***");
    }

    // Modded
    private void initClassAndPackVars(GraphBuilder graphB)
    {
        classes = graphB.getClasses();
        packages = graphB.getPackages();
        classCount = _sys.getClasses().size();
        packCount = 0;
        for (Vertex pack : packages)
        {
            if (GraphBuilder.SYSTEM_PACKAGE.equals(pack.value(GraphBuilder.PROPERTY_PACKAGE_TYPE)))
            {
                packCount++;
            }
        }
    }

    public void readGraph()
    {
        logger.info("***Start Reading Neo4j***");
        logger.info("***Start Reading Neo4j*** - " + _dbFolder.toPath());
        GraphReader reader = new GraphReader(_dbFolder.toPath());
        logger.info("***Created Reader Neo4j***");
        graph = reader.getGraph();
        logger.info("***Readed Neo4j*** - " + graph);
        logger.info("End Reading Neo4j");
    }

    public boolean runUnstableDependencies() throws TypeVertexException, FileNotFoundException, IOException
    {
        logger.info("***Start Unstable dependencies detection***" + graph);
        _metricsCalculator = new PackageMetricsCalculator(graph,classes,packages);
        _unstableDependencyDetector = new UnstableDependencyDetector(graph, _metricsCalculator);

        UDUtils.cleanUDDetection(graph);
        MetricsUploader m = new MetricsUploader(graph,classes,packages);

        try
        {
            m.updateInstability();
            if (graph instanceof Neo4jGraph)
            {
                Neo4JGraphWriter graphW = new Neo4JGraphWriter();
                graphW.write(graph, false);
            }
        }
        catch (TypeVertexException e)
        {
            logger.debug(e.getMessage());
        }

        boolean ud = _unstableDependencyDetector.newDetect();

        // update the graph if it is a Neo4J graph
        if (ud == true)
        {
            if (graph instanceof Neo4jGraph)
            {
                Neo4JGraphWriter graphW = new Neo4JGraphWriter();
                graphW.write(graph, false);
            }

            // Modded (additional if condition)
            if(!_suppressNonAsTdEvolution)
            {
                Map<String, List<String>> smellMap = _unstableDependencyDetector.getSmellMap();
                logger.debug("Obtained smell map of UD");
                UDPrinter p = new UDPrinter(outputDirUtils.getOutputFolder(), _unstableDependencyDetector,
                        FILE_UNSTABLE_DEPENDECY);
                logger.debug("Created csv printer of UD");
                p.print(smellMap);
                logger.debug("UD csv printer");
                p.closeAll();
                logger.debug("Closed csv printer of UD");
            }
        }
        logger.info("***End of Unstable dependencies detection***" + graph);
        return ud;
    }

    public boolean runUnstableDependencyFilter() throws IOException
    {
        File f = outputDirUtils.getOutputFolder();
        logger.debug("Obtained output folder: " + f);
        runUnstableDependencyFilter(f, FILE_UNSTABLE_DEPENDECY_FILTERED_30);
        return true;
    }

    private boolean runUnstableDependencyFilter(File outputFilePath, String csvfile) throws IOException
    {
        logger.info("***Start unstable dependency filtering***" + graph);
        UDRateFilter filter = new UDRateFilter(graph);
        logger.debug("***Finished unstable dependency filtering***" + graph);
        logger.debug("***Start writing csv of unstable dependency filtering***" + graph);
        UDPrinter p2 = new UDPrinter(outputFilePath, _unstableDependencyDetector, csvfile);
        p2.print(filter.filter(30));
        logger.debug("UD csv printer");
        p2.closeAll();
        logger.debug("***End of writing csv of unstable dependency filtering***" + graph);
        logger.info("***End of Unstable dependencies filtering***" + graph);
        return true;
    }

    public void runHubLikeDependencies() throws TypeVertexException, IOException
    {
        logger.info("***Start of Hub-Like dependencies detection***" + graph);
        runHubLikeDependencies(outputDirUtils.getFileInOutputFolder(FILE_HUB_LIKE));
        logger.info("***End of Hub-Like dependencies detection***" + graph);
    }

    private boolean runHubLikeDependencies(File outputFilePath)
            throws TypeVertexException, IOException
    {
        File outputFileCSVPath = outputFilePath;
        _classMetricsCalculator = new ClassMetricsCalculator(graph);
        _hubLikeDetector = new HubLikeDetector(graph, _classMetricsCalculator);
        Map<String, List<Integer>> hubLikeClasses = _hubLikeDetector.detect();

        // update metrics in graph

        if (graph instanceof Neo4jGraph)
        {
            Neo4JGraphWriter graphW = new Neo4JGraphWriter();
            graphW.write(graph, false);
        }

        if (hubLikeClasses != null && !hubLikeClasses.isEmpty())
        {
            // Modded (additional if condition)
            if(!_suppressNonAsTdEvolution)
            {
                CSVFormat formatter = CSVFormat.EXCEL.withHeader("Class", "FanIn", "FanOut", "Total Dependences");
                FileWriter writer = new FileWriter(outputFileCSVPath);
                CSVPrinter printer = new CSVPrinter(writer, formatter);
                for (Entry<String, List<Integer>> e : hubLikeClasses.entrySet())
                {
                    printer.print(e.getKey());
                    printer.print(e.getValue().get(1));
                    printer.print(e.getValue().get(2));
                    printer.print(e.getValue().get(0));
                    printer.println();
                }
                printer.close();
                writer.close();
            }

        }
        else
        {
            logger.info("***No Hub like Dependency smell detected, nothing to print***");
            return false;
        }

        return true;
    }

    public boolean runCycleDetector()
    {
        logger.info("***Start cycles detection***" + graph);
        _cycleDetector = new CyclicDependencyDetector(graph, outputDirUtils.getOutputFolder(),
            _suppressNonAsTdEvolution,classes,packages);

        CDFilterUtils.cleanCDDetection(graph);
        _cycleDetector.detect();
        if (graph instanceof Neo4jGraph)
        {
            Neo4JGraphWriter graphW = new Neo4JGraphWriter();
            graphW.write(graph, false);
        }

        // Modded (additional condition)
        if(!_suppressNonAsTdEvolution)
        {
            CyclePrinter printer = new PrintToMatrix(classes);
            CyclePrinter printer2 = new PrintToTable(classes);

            printer.initializePrint(outputDirUtils.getOutputFolder(), GraphBuilder.CLASS);
            printer2.initializePrint(outputDirUtils.getOutputFolder(), GraphBuilder.CLASS);

            printer.printCyclesFromGraph(graph, _cycleDetector.getListOfCycleSmells(GraphBuilder.CLASS));
            printer2.printCyclesFromGraph(graph, _cycleDetector.getListOfCycleSmells(GraphBuilder.CLASS));

            printer.closePrint();
            printer2.closePrint();

            CyclePrinter printer3 = new PrintToMatrix(packages);
            CyclePrinter printer4 = new PrintToTable(packages);

            printer3.initializePrint(outputDirUtils.getOutputFolder(), GraphBuilder.PACKAGE);
            printer4.initializePrint(outputDirUtils.getOutputFolder(), GraphBuilder.PACKAGE);

            printer3.printCyclesFromGraph(graph, _cycleDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));
            printer4.printCyclesFromGraph(graph, _cycleDetector.getListOfCycleSmells(GraphBuilder.PACKAGE));

            printer3.closePrint();
            printer4.closePrint();
        }


        logger.info("***End of cycles detection***" + graph);
        return true;
    }

    public void runCycleDetectorShapeFilter()
    {
        runCycleDetectorShapeFilter(outputDirUtils.getOutputFolder(), null);
    }

    private void runCycleDetectorShapeFilter(File outputFilePath, String nameFile)
    {
        logger.info("***Start cycles filtering***" + graph);

        CDFilterUtils.cleanCDShapeFilter(graph);

        CDShapeFilter filter = new CDShapeFilter(graph);

        filter.getCircleCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getCircleCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);
        logger.debug("Computed circle");

        filter.getCliqueCycles(GraphBuilder.CLASS, PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
        filter.getCliqueCycles(GraphBuilder.PACKAGE, GraphBuilder.LABEL_PACKAGE_AFFERENCE);
        logger.debug("Computed clique");

        filter.getStarAndChainCycles(GraphBuilder.CLASS);
        filter.getStarAndChainCycles(GraphBuilder.PACKAGE);
        logger.debug("Computed stars and chain");

        if (graph instanceof Neo4jGraph)
        {
            Neo4JGraphWriter graphW = new Neo4JGraphWriter();
            graphW.write(graph, false);
            logger.debug("Written Graph Neo4j");
        }

        CyclePrinter printerShape = new PrintShapesDocker(nameFile);
        printerShape.initializePrint(outputFilePath, GraphBuilder.CLASS);
        printerShape.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.CLASS));
        printerShape.closePrint();

        PrintShapesDocker printerShape2 = new PrintShapesDocker(nameFile);
        printerShape2.initializePrint(outputFilePath, GraphBuilder.PACKAGE, new String[]{
                "IdCycle",
                "CycleType",
                "MinWeight",
                "MaxWeight",
                "numVertices",
                "ElementList",
                "ClassElementList"});
        printerShape2.printCyclesFromGraph(graph, GraphUtils.findVerticesByProperty(graph, GraphBuilder.CYCLE_SHAPE,
                GraphBuilder.PROPERTY_VERTEX_TYPE, GraphBuilder.PACKAGE));
        printerShape2.closePrint();

        logger.info("***End of cycles filtering***" + graph);
    }

    public boolean createCSVClassesMetrics() throws IOException, TypeVertexException, NullPointerException
    {
        logger.info("***Start of computation of class metrics***");

        logger.debug("folder: " + outputDirUtils.getFileInOutputFolder(FILE_CLASS_METRICS));
        File fileCsv = outputDirUtils.getFileInOutputFolder(FILE_CLASS_METRICS);

        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Class", "FI", "FO", "CBO", "LCOM","PR"); // Modded
        FileWriter writer = new FileWriter(fileCsv);
        CSVPrinter printer = new CSVPrinter(writer, formatter);

        for (Vertex clazz : classes)
        {
            String className = clazz.value(GraphBuilder.PROPERTY_NAME);
            String retrieved = clazz.value(GraphBuilder.PROPERTY_CLASS_TYPE);
            if (!GraphBuilder.RETRIEVED_CLASS.equals(retrieved))
            {
                int fanIn = clazz.value(GraphBuilder.PROPERTY_FANIN);
                int fanOut = clazz.value(GraphBuilder.PROPERTY_FANOUT);
                int cbo = clazz.value(GraphBuilder.PROPERTY_CBO);
                double lcom = clazz.value(GraphBuilder.PROPERTY_LCOM);
                double pr = clazz.value(GraphBuilder.PROPERTY_CENTRALITY); // Modded
                printer.print(className);
                printer.print(fanIn);
                printer.print(fanOut);
                printer.print(cbo);
                printer.print(lcom);
                printer.print(pr); // Modded
                printer.println();
            }
        }
        printer.close();
        writer.close();
        logger.info("***End of computation of class metrics***");
        return true;

    }

    /**
     * it is deprecated because it work only whene the compiled files are available
     *
     * @return
     * @throws IOException
     * @throws TypeVertexException
     * @throws NullPointerException
     */
    @Deprecated
    public boolean createCSVClassesMetricsOld() throws IOException, TypeVertexException, NullPointerException
    {
        logger.info("***Start of computation of class metrics***");
        _classMetricsCalculator = new ClassMetricsCalculator(graph);

        logger.debug("folder: " + outputDirUtils.getFileInOutputFolder(FILE_CLASS_METRICS));
        File fileCsv = outputDirUtils.getFileInOutputFolder(FILE_CLASS_METRICS);

        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Class", "FI", "FO", "CBO", "LCOM");
        FileWriter writer = new FileWriter(fileCsv);
        CSVPrinter printer = new CSVPrinter(writer, formatter);
        logger.debug("sys: " + _sys);
        for (JavaClass clazz : _sys.getClasses())
        {
            String className = clazz.getClassName();
            int fanIn = _classMetricsCalculator.calculateFanIn(className);
            int fanOut = _classMetricsCalculator.calculateFanOut(className);
            int cbo = _classMetricsCalculator.calculateCBO(className);
            double lcom = _classMetricsCalculator.calculateLCOM(clazz);
            printer.print(className);
            printer.print(fanIn);
            printer.print(fanOut);
            printer.print(cbo);
            printer.print(lcom);
            printer.println();
        }
        printer.close();
        writer.close();
        logger.info("***End of computation of class metrics***");
        return true;

    }

    public boolean computeAndStoreClassesMetrics()
    {
        logger.info("***Start of computation of class metrics***");
        _classMetricsCalculator = new ClassMetricsCalculator(graph);

        logger.debug("sys: " + _sys);
        if (_sys != null)
        {
            for (JavaClass clazz : _sys.getClasses())
            {
                String className = clazz.getClassName();
                try
                {
                    _classMetricsCalculator.calculateFanIn(className);
                    _classMetricsCalculator.calculateFanOut(className);
                    _classMetricsCalculator.calculateCBO(className);
                    _classMetricsCalculator.calculateLCOM(clazz);
                }
                catch (TypeVertexException e)
                {
                    e.printStackTrace();
                }
            }
//			if (graph instanceof Neo4jGraph) {
//				Neo4JGraphWriter graphW = new Neo4JGraphWriter();
//				graphW.write(graph, false);
//			}
            logger.info("***End of computation of class metrics***");
            return true;
        }
        else
        {
            logger.info("***End of computation of class metrics***");
            return false;
        }

    }

    public boolean computeAndStorePackageMetrics()
    {
        logger.info("***Start of computation of package metrics***");
        _metricsCalculator = new PackageMetricsCalculator(graph,classes,packages);

        for (Vertex pkg : packages)
        {
            if (GraphBuilder.SYSTEM_PACKAGE.equals(pkg.value(GraphBuilder.PROPERTY_PACKAGE_TYPE)))
            {
                try
                {
                    String pkgname = pkg.value(GraphBuilder.PROPERTY_NAME);
//					logger.debug(pkgname);
//					_metricsCalculator.calculateInternalPackageMetrics(pkgname);
                    _metricsCalculator.calculateAfferentClasses(pkgname);
                    _metricsCalculator.calculateInternalEfferentClasses(pkgname);
                    _metricsCalculator.calculateInternalInstability(pkgname);
                    _metricsCalculator.calculateAbstractness(pkgname);
                    _metricsCalculator.calculateDistanceFromTheMainSequence(pkgname);
                }
                catch (TypeVertexException e)
                {
                    e.printStackTrace();
                }
            }
        }
        logger.info("***End of computation of package metrics***");
        return true;
    }

    public boolean createCSVPackageMetrics() throws IOException, NoSuchElementException, TypeVertexException
    {
        logger.info("***Start of computation of package metrics***");
        _metricsCalculator = new PackageMetricsCalculator(graph,classes,packages);
        // update
        MetricsUploader mu = new MetricsUploader(graph,classes,packages);
        try
        {
            mu.updateInstability();
            if (graph instanceof Neo4jGraph)
            {
                Neo4JGraphWriter graphW = new Neo4JGraphWriter();
                graphW.write(graph, false);
            }
        }
        catch (TypeVertexException e)
        {
            logger.debug(e.getMessage());
        }
        // end update

        File fileCsv = outputDirUtils.getFileInOutputFolder(FILE_PACKAGE_METRICS);

        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Package", "CA", "CE", "RMI", "RMA", "RMD","PR"); // Modded
        FileWriter writer = new FileWriter(fileCsv);
        CSVPrinter printer = new CSVPrinter(writer, formatter);

        for (Vertex pkg : packages)
        {
            if (GraphBuilder.SYSTEM_PACKAGE.equals(pkg.value(GraphBuilder.PROPERTY_PACKAGE_TYPE)))
            {
                String n = pkg.value(GraphBuilder.PROPERTY_NAME);
                if ("".equals(n))
                {
                    n = GraphBuilder.DEFAULT_PACKAGE;
                }
                printer.print(n);
                printer.print(pkg.value(GraphBuilder.PROPERTY_CA));
                printer.print(pkg.value(GraphBuilder.PROPERTY_CE_INTERNAL));
                printer.print(pkg.value(GraphBuilder.PROPERTY_INSTABILITY_INTERNAL));
                printer.print(pkg.value(GraphBuilder.PROPERTY_RMA));
                printer.print(pkg.value(GraphBuilder.PROPERTY_RMD));
                printer.print(pkg.value(GraphBuilder.PROPERTY_CENTRALITY)); // Modded

                printer.println();
            }
        }
        printer.close();
        writer.close();
        logger.info("***End of computation of package metrics***");
        return true;
    }

    @Deprecated
    public boolean createCSVPackageMetricsOld() throws IOException, NoSuchElementException, TypeVertexException
    {
        logger.info("***Start of computation of package metrics***");
        _metricsCalculator = new PackageMetricsCalculator(graph,classes,packages);
        // update
        MetricsUploader mu = new MetricsUploader(graph,classes,packages);
        try
        {
            mu.updateInstability();
            if (graph instanceof Neo4jGraph)
            {
                Neo4JGraphWriter graphW = new Neo4JGraphWriter();
                graphW.write(graph, false);
            }
        }
        catch (TypeVertexException e)
        {
            logger.debug(e.getMessage());
        }
        // end update

        File fileCsv = outputDirUtils.getFileInOutputFolder(FILE_PACKAGE_METRICS);

        CSVFormat formatter = CSVFormat.EXCEL.withHeader("Package", "CA", "CE", "RMI", "RMA", "RMD");
        FileWriter writer = new FileWriter(fileCsv);
        CSVPrinter printer = new CSVPrinter(writer, formatter);

        for (Vertex pkg : packages)
        {
            if (GraphBuilder.SYSTEM_PACKAGE.equals(pkg.value(GraphBuilder.PROPERTY_PACKAGE_TYPE)))
            {
                double[] metrics;
                metrics = _metricsCalculator.calculateInternalPackageMetrics(pkg);
                String n = pkg.value(GraphBuilder.PROPERTY_NAME);
                if ("".equals(n))
                {
                    n = GraphBuilder.DEFAULT_PACKAGE;
                }
                printer.print(n);
                for (Double m : metrics)
                {
                    if (m != null)
                    {
                        printer.print(m);
                    }
                }
                printer.println();
            }
        }
        printer.close();
        writer.close();
        logger.info("***End of computation of package metrics***");
        return true;
    }

    /*
     * create output folder for the UI
     */
    public boolean createOutPutFolder()
    {
        logger.info("***Creating Output folder***");
        if (_outDir != null)
        {
            outputDirUtils.createDirFullPath(_outDir, _jarMode);
        }
        else
        {
            outputDirUtils.createDir(_projectFolder, _jarMode);
        }
        logger.info("***Created Output Folder***");
        return true;
    }

    /*
     * create output folder for the UI
     */
    public boolean createOutPutFolderTerminal()
    {
        logger.info("***Creating Output folder***");
        if (_outDir != null)
        {
            outputDirUtils.createDirFullPath(_outDir);
        }
        else
        {
            outputDirUtils.createDirFullPath();
        }
        logger.info("***Created Output Folder***" + outputDirUtils.getOutputFolder());
        return true;
    }

    public boolean createOutPutFolderRead()
    {
        outputDirUtils.createDirFullPath(_outDir);
        logger.info("***Created Output Folder***");
        return true;
    }

    public void closeGraph()
    {
        closeGraph(graph);
    }

    public static void closeGraph(final Graph graph)
    {
        try
        {
            if (graph != null)
            {
                graph.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isGraphOpen()
    {
        if (graph != null)
        {
            return true;
        }
        else
        {
            return false;
        }

    }



    // Modded
    public boolean detectSuperCycles()
    {
        classCds = _cycleDetector.getListOfCycleSmells(GraphBuilder.CLASS);
        packCds = _cycleDetector.getListOfCycleSmells(GraphBuilder.PACKAGE);
        hds = _hubLikeDetector.getListOfHubLikeSmells();
        uds = _unstableDependencyDetector.getListOfUDSmells();
        _superCycleDetector = new SuperCycleDetector();
        _superCycleDetector.detectAndRegisterSuperCycles(graph, classes,packages,classCds,packCds);
        classSupercycles = _superCycleDetector.getListOfSuperCycleSmells(GraphBuilder.CLASS);
        packSupercycles = _superCycleDetector.getListOfSuperCycleSmells(GraphBuilder.PACKAGE);
        return true;
    }


    // Modded
    public void initProjectMetricsCalculator()
    {
        _projectMetricsCalculator = new ProjectMetricsCalculator
            (classCount,packCount,classSupercycles,packSupercycles,hds,uds);
    }


    // Modded
    public boolean calculateTdAndOverlapRatios()
    {
        boolean result;
        if(graph == null)
        {
            return true;
        }
        _projectMetricsCalculator.calcProjAsAffectedCompsAndOverlapRatios();

        // Write PageRank value into all class and package nodes
        result = PageRankCalculator.calculateAllPrVals(graph);

        TdSmellCalculator tdSmellCalculator = new TdSmellCalculator(classCds,packCds,hds,uds);
        result &= tdSmellCalculator.calculateAllTdVals(_projectMetricsCalculator);
        TdSmellCalculator.calculateSuperCdTdVals(classSupercycles);
        TdSmellCalculator.calculateSuperCdTdVals(packSupercycles);
        return result;
    }

    // Modded
    public boolean calculateMiscMetrics(int loc)
    {
        _projectMetricsCalculator.updateAsCounts(loc);
        _projectMetricsCalculator.calculateProjectTdMetrics();
        MiscSmellMetricsCalculator miscSmellMetricsCalculator = new MiscSmellMetricsCalculator(hds,uds);
        miscSmellMetricsCalculator.calculateAll();
        return true;
    }

    // Modded
    public boolean printAsTdEvolution() throws IOException
    {
        AsTdEvolutionPrinter astdEPrinter = new AsTdEvolutionPrinter(outputDirUtils,_projectMetricsCalculator,
            classSupercycles,packSupercycles,hds,uds);
        astdEPrinter.printAll();
        return true;
    }

}
