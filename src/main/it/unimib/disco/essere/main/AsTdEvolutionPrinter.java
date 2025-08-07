package it.unimib.disco.essere.main;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.metricsengine.ProjectMetricsCalculator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class AsTdEvolutionPrinter
{
    public static final String FILE_PROJECT = "ProjectMetrics.csv";
    public static final String FILE_CLASS_CDS_PROPS = "ClassCDsProperties.csv";
    public static final String FILE_PACK_CDS_PROPS = "PackageCDsProperties.csv";
    public static final String FILE_HDS_PROPS = "HDsProperties.csv";
    public static final String FILE_UDS_PROPS = "UDsProperties.csv";
    public static final String FILE_CLASS_CDS_COMPS = "ClassCDsComponents.csv";
    public static final String FILE_PACK_CDS_COMPS = "PackageCDsComponents.csv";
    public static final String FILE_HDS_COMPS = "HDsComponents.csv";
    public static final String FILE_UDS_COMPS = "UDsComponents.csv";
    public static final String FOLDER_INTRA_VERSION_CLASS_CD_EDGES = "classCDEdges";
    public static final String FOLDER_INTRA_VERSION_PACK_CD_EDGES = "packageCDEdges";
    public static final String FILE_INTRA_VERSION_CLASS_CD_MEFS = "ClassCDmEFS.csv";
    public static final String FILE_INTRA_VERSION_PACK_CD_MEFS = "PackageCDmEFS.csv";
    public static final String FILE_INTRA_VERSION_CLASS_CD_MEFS_WO_TINYS = "ClassCDmEFSWOTinys.csv";
    public static final String FILE_INTRA_VERSION_PACK_CD_MEFS_WO_TINYS = "PackageCDmEFSWOTinys.csv";
    public static final String FILE_EX_TIME_LOGS = "ExTimeLogs.csv";

    public static final String ID = "id";
    public static final String AFFECTED_COMPS = "affectedComponents";
    public static final String MAIN_COMP = "mainComponent";
    public static final String LESS_STABLE_PACKS = "lessStableDependedOnPackages";
    public static final String AFFERENT_CLASSES = "afferentClasses";
    public static final String EFFERENT_CLASSES = "efferentClasses";
    public static final String PARENT_ID = "parentId";
    public static final String DURATION = "cumulatedDuration";
    public static final String EVENT_COUNT = "eventCount";
    public static final String EVENT = "eventDescription";


    public static final String DEP_EDGE_OUT = "dependency edge outgoing from";
    public static final String DEP_EDGE_IN = "dependency edge incoming to...";

    public static final String EDGES = "edges";

    public static final String DELIMITER = ",";

    private OutputDirUtils outputDirUtils;
    private ProjectMetricsCalculator projectMetricsCalculator;
    private List<Vertex> classSupercycles;
    private List<Vertex> packSupercycles;
    private List<Vertex> hds;
    private List<Vertex> uds;
    private ExTimeLogger exTimeLogger;

    public AsTdEvolutionPrinter(OutputDirUtils outputDirUtils, ProjectMetricsCalculator projectMetricsCalculator,
                                List<Vertex> classSupercycles, List<Vertex> packSupercycles,
                                List<Vertex> hds, List<Vertex> uds, ExTimeLogger exTimeLogger)
    {
        this.outputDirUtils = outputDirUtils;
        this.projectMetricsCalculator = projectMetricsCalculator;
        this.hds = hds;
        this.uds = uds;
        this.classSupercycles = classSupercycles;
        this.packSupercycles = packSupercycles;
        this.exTimeLogger = exTimeLogger;
    }

    public void printAll() throws IOException, NullPointerException
    {
        printProjectMetrics();
        printClassCds();
        printPackCds();
        printHds();
        printUds();
        exTimeLogger.logEventEnd(ETLE.Event.ARCAN_PRINTING);
        printExTimeLogs();
    }

    private void printCore(String file, String[] headers, PrinterCore printerCore) throws IOException, NullPointerException
    {
        File fileCsv = outputDirUtils.getFileInOutputFolder(file);
        printCore(fileCsv, headers, printerCore);
    }

    private void printCore(File fileCsv, String[] headers, PrinterCore printerCore) throws IOException, NullPointerException
    {
        CSVFormat formatter = CSVFormat.EXCEL.withHeader(headers);
        FileWriter writer = new FileWriter(fileCsv);
        CSVPrinter printer = new CSVPrinter(writer, formatter);
        printerCore.print(headers, printer);
        printer.close();
        writer.close();
    }

    private static String[] mergeStringArrays(String[]... arrays)
    {
        return Stream.of(arrays).flatMap(Stream::of).toArray(String[]::new);
    }

    public final static String[] projectMetricsHeaders = new String[]{
        ProjectMetricsCalculator.PROPERTY_LOC,
        ProjectMetricsCalculator.PROPERTY_TOTAL_CLASS_COUNT,
        ProjectMetricsCalculator.PROPERTY_TOTAL_PACK_COUNT,
        ProjectMetricsCalculator.PROPERTY_INT_CLASS_COUNT,
        ProjectMetricsCalculator.PROPERTY_INT_PACK_COUNT,
        ProjectMetricsCalculator.PROPERTY_EXT_CLASS_COUNT,
        ProjectMetricsCalculator.PROPERTY_EXT_PACK_COUNT,
        ProjectMetricsCalculator.PROPERTY_TOTAL_CLASS_DEP_COUNT,
        ProjectMetricsCalculator.PROPERTY_TOTAL_PACK_DEP_COUNT,
        ProjectMetricsCalculator.PROPERTY_INT_CLASS_DEP_COUNT,
        ProjectMetricsCalculator.PROPERTY_INT_PACK_DEP_COUNT,
        ProjectMetricsCalculator.PROPERTY_AS_COUNT,
        ProjectMetricsCalculator.PROPERTY_ASS_PER_LOC,
        ProjectMetricsCalculator.PROPERTY_ASS_PER_CLASS,
        ProjectMetricsCalculator.PROPERTY_ASS_PER_PACK,
        ProjectMetricsCalculator.PROPERTY_SUPERCYCLE_CLASS_CD_COUNT,
        ProjectMetricsCalculator.PROPERTY_CLASS_CDS_PER_LOC,
        ProjectMetricsCalculator.PROPERTY_CLASS_CDS_PER_CLASS,
        ProjectMetricsCalculator.PROPERTY_SUPERCYCLE_PACK_CD_COUNT,
        ProjectMetricsCalculator.PROPERTY_PACK_CDS_PER_LOC,
        ProjectMetricsCalculator.PROPERTY_PACK_CDS_PER_PACK,
        ProjectMetricsCalculator.PROPERTY_HD_COUNT,
        ProjectMetricsCalculator.PROPERTY_HDS_PER_LOC,
        ProjectMetricsCalculator.PROPERTY_HDS_PER_CLASS,
        ProjectMetricsCalculator.PROPERTY_UD_COUNT,
        ProjectMetricsCalculator.PROPERTY_UDS_PER_LOC,
        ProjectMetricsCalculator.PROPERTY_UDS_PER_PACK,
        ProjectMetricsCalculator.PROPERTY_TD_AMOUNT,
        ProjectMetricsCalculator.PROPERTY_TD_PER_LOC,
        ProjectMetricsCalculator.PROPERTY_TD_PER_CLASS,
        ProjectMetricsCalculator.PROPERTY_TD_PER_PACK,
        ProjectMetricsCalculator.PROPERTY_CLASS_CD_SHARE_ON_TD,
        ProjectMetricsCalculator.PROPERTY_PACK_CD_SHARE_ON_TD,
        ProjectMetricsCalculator.PROPERTY_HD_SHARE_ON_TD,
        ProjectMetricsCalculator.PROPERTY_UD_SHARE_ON_TD,
        ProjectMetricsCalculator.PROPERTY_AS_AFFECTED_CLASSES_DEGREE,
        ProjectMetricsCalculator.PROPERTY_AS_AFFECTED_PACKS_DEGREE,
        ProjectMetricsCalculator.PROPERTY_AS_MULTI_AFFECTED_CLASSES_DEGREE,
        ProjectMetricsCalculator.PROPERTY_AS_MULTI_AFFECTED_PACKS_DEGREE,
        ProjectMetricsCalculator.PROPERTY_TOTAL_ORDER_CLASS_CDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_ORDER_PACK_CDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_ORDER_HDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_ORDER_UDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_ORDER_OVERALL,
        ProjectMetricsCalculator.PROPERTY_TOTAL_SIZE_CLASS_CDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_SIZE_PACK_CDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_SIZE_HDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_SIZE_UDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_SIZE_OVERALL,
        ProjectMetricsCalculator.PROPERTY_TOTAL_NUM_SUBCYCLES_CLASS_CDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_NUM_SUBCYCLES_PACK_CDS,
        ProjectMetricsCalculator.PROPERTY_TOTAL_NUM_SUBCYCLES_OVERALL,
        ProjectMetricsCalculator.PROPERTY_CLASS_SHARE_LARGEST_CLASS_CD,
        ProjectMetricsCalculator.PROPERTY_PACK_SHARE_LARGEST_PACK_CD
    };

    public final static String[] exTimeLogsHeaders = new String[]{
        ID,
        PARENT_ID,
        DURATION,
        EVENT_COUNT,
        EVENT
    };


    private final static String[] generalSmellPropHeaders = new String[]{
        ID,
        GraphBuilder.PROPERTY_ORDER,
        GraphBuilder.PROPERTY_SIZE,
        GraphBuilder.PROPERTY_SIZE_OVERCOMPLEYITY,
        GraphBuilder.PROPERTY_SEVERITY_SCORE,
        GraphBuilder.PROPERTY_CENTRALITY,
        GraphBuilder.PROPERTY_TDI,
        GraphBuilder.PROPERTY_OVERLAP_RATIO,
        GraphBuilder.PROPERTY_BACKREF_SHARE,
        GraphBuilder.PROPERTY_SHARE_PACKAGES,
    };

    public final static String[] classCdPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_SHAPE,
            GraphBuilder.PROPERTY_NUM_SUBCYCLES,
            GraphBuilder.PROPERTY_NUM_INHERIT_EDGES,
            GraphBuilder.PROPERTY_REL_NUM_INHERIT_EDGES,
            GraphBuilder.PROPERTY_MEFS_SIZE,
            GraphBuilder.PROPERTY_REL_MEFS_SIZE,
            GraphBuilder.PROPERTY_MEFS_SIZE_WO_TINYS,
            GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS,
            GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS_REDUCTION,
            GraphBuilder.PROPERTY_NUM_PACKAGES,
            GraphBuilder.PROPERTY_SHARE_CLASSES,
            GraphBuilder.PROPERTY_DENSITY
        });

    public final static String[] packCdPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_SHAPE,
            GraphBuilder.PROPERTY_NUM_SUBCYCLES,
            GraphBuilder.PROPERTY_MEFS_SIZE,
            GraphBuilder.PROPERTY_REL_MEFS_SIZE,
            GraphBuilder.PROPERTY_MEFS_SIZE_WO_TINYS,
            GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS,
            GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS_REDUCTION,
            GraphBuilder.PROPERTY_NUM_CLASS_SUPERCYCLES,
            GraphBuilder.PROPERTY_NUM_STRONG_PACK_SUPERCYCLES,
            GraphBuilder.PROPERTY_DENSITY
        });

    public final static String[] hdPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_HL_FAN_IN,
            GraphBuilder.PROPERTY_HL_FAN_OUT,
            GraphBuilder.PROPERTY_HUB_RATIO,
            GraphBuilder.PROPERTY_NUM_PACKAGES,
            GraphBuilder.PROPERTY_SHARE_CLASSES,
        });

    public final static String[] udPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_DOUD,
            GraphBuilder.PROPERTY_INSTABILITY_GAP_1ST_QUARTILE,
            GraphBuilder.PROPERTY_INSTABILITY_GAP_2ND_QUARTILE,
            GraphBuilder.PROPERTY_INSTABILITY_GAP_3RD_QUARTILE,
        });

    public final static String[] cdMEFSHeaders = new String[]{
        ID,
        EDGES
    };

    public final static String[] cdEdgeHeaders = new String[]{
        DEP_EDGE_OUT,
        DEP_EDGE_IN
    };

    public final static String[] cdCompHeaders = new String[]{
        ID,
        AFFECTED_COMPS
    };

    public final static String[] hdCompHeaders = new String[]{
        ID,
        MAIN_COMP,
        AFFERENT_CLASSES,
        EFFERENT_CLASSES
    };

    public final static String[] udCompHeaders = new String[]{
        ID,
        MAIN_COMP,
        LESS_STABLE_PACKS
    };

    public void printProjectMetrics() throws IOException, NullPointerException
    {
        exTimeLogger.logEventStart(ETLE.Event.PRT_PROJECT_METRICS);
        printCore(FILE_PROJECT, projectMetricsHeaders, new ProjectMetricsPrinter());
        exTimeLogger.logEventEnd(ETLE.Event.PRT_PROJECT_METRICS);
    }

    public void printExTimeLogs() throws IOException, NullPointerException
    {
        printCore(FILE_EX_TIME_LOGS, exTimeLogsHeaders, new ExTimeLogsPrinter());
    }

    public void printClassCds() throws IOException, NullPointerException
    {
        exTimeLogger.logEventStart(ETLE.Event.PRT_CLASS_CDS_PROPS);
        printCore(FILE_CLASS_CDS_PROPS, classCdPropHeaders, new CdPropsPrinter(GraphBuilder.CLASS));
        exTimeLogger.logEventEnd(ETLE.Event.PRT_CLASS_CDS_PROPS);
        exTimeLogger.logEventStart(ETLE.Event.PRT_CLASS_CDS_COMPS);
        printCore(FILE_CLASS_CDS_COMPS, cdCompHeaders, new CdCompsPrinter(GraphBuilder.CLASS));
        exTimeLogger.logEventEnd(ETLE.Event.PRT_CLASS_CDS_COMPS);
        exTimeLogger.logEventStart(ETLE.Event.PRT_CLASS_CDS_EDGES);
        outputDirUtils.createSubDirFullPath(outputDirUtils.getFolderInOutputFolder(FOLDER_INTRA_VERSION_CLASS_CD_EDGES));
        for (Vertex smell : classSupercycles)
        {
            File file = outputDirUtils.getFileInSubOutputFolder(FOLDER_INTRA_VERSION_CLASS_CD_EDGES, smell.id().toString() + ".csv");
            printCore(file, cdEdgeHeaders, new CdEdgesPrinter(GraphBuilder.CLASS, smell));
        }
        exTimeLogger.logEventEnd(ETLE.Event.PRT_CLASS_CDS_EDGES);
        exTimeLogger.logEventStart(ETLE.Event.PRT_CLASS_CDS_MEFS);
        printCore(FILE_INTRA_VERSION_CLASS_CD_MEFS, cdMEFSHeaders, new MEFSPrinter(GraphBuilder.CLASS,false));
        printCore(FILE_INTRA_VERSION_CLASS_CD_MEFS_WO_TINYS, cdMEFSHeaders, new MEFSPrinter(GraphBuilder.CLASS,true));
        exTimeLogger.logEventEnd(ETLE.Event.PRT_CLASS_CDS_MEFS);

    }

    public void printPackCds() throws IOException, NullPointerException
    {
        exTimeLogger.logEventStart(ETLE.Event.PRT_PACK_CDS_PROPS);
        printCore(FILE_PACK_CDS_PROPS, packCdPropHeaders, new CdPropsPrinter(GraphBuilder.PACKAGE));
        exTimeLogger.logEventEnd(ETLE.Event.PRT_PACK_CDS_PROPS);
        exTimeLogger.logEventStart(ETLE.Event.PRT_PACK_CDS_COMPS);
        printCore(FILE_PACK_CDS_COMPS, cdCompHeaders, new CdCompsPrinter(GraphBuilder.PACKAGE));
        exTimeLogger.logEventEnd(ETLE.Event.PRT_PACK_CDS_COMPS);
        exTimeLogger.logEventStart(ETLE.Event.PRT_PACK_CDS_EDGES);
        outputDirUtils.createSubDirFullPath(outputDirUtils.getFolderInOutputFolder(FOLDER_INTRA_VERSION_PACK_CD_EDGES));
        for (Vertex smell : packSupercycles)
        {
            File file = outputDirUtils.getFileInSubOutputFolder(FOLDER_INTRA_VERSION_PACK_CD_EDGES, smell.id().toString() + ".csv");
            printCore(file, cdEdgeHeaders, new CdEdgesPrinter(GraphBuilder.PACKAGE, smell));
        }
        exTimeLogger.logEventEnd(ETLE.Event.PRT_PACK_CDS_EDGES);
        exTimeLogger.logEventStart(ETLE.Event.PRT_PACK_CDS_MEFS);
        printCore(FILE_INTRA_VERSION_PACK_CD_MEFS, cdMEFSHeaders, new MEFSPrinter(GraphBuilder.PACKAGE,false));
        printCore(FILE_INTRA_VERSION_PACK_CD_MEFS_WO_TINYS, cdMEFSHeaders, new MEFSPrinter(GraphBuilder.PACKAGE,true));
        exTimeLogger.logEventEnd(ETLE.Event.PRT_PACK_CDS_MEFS);
    }

    public void printHds() throws IOException, NullPointerException
    {
        exTimeLogger.logEventStart(ETLE.Event.PRT_HDS_PROPS);
        printCore(FILE_HDS_PROPS, hdPropHeaders, new HdPropsPrinter());
        exTimeLogger.logEventEnd(ETLE.Event.PRT_HDS_PROPS);
        exTimeLogger.logEventStart(ETLE.Event.PRT_HDS_COMPS);
        printCore(FILE_HDS_COMPS, hdCompHeaders, new HdCompsPrinter());
        exTimeLogger.logEventEnd(ETLE.Event.PRT_HDS_COMPS);
    }

    public void printUds() throws IOException, NullPointerException
    {
        exTimeLogger.logEventStart(ETLE.Event.PRT_UDS_PROPS);
        printCore(FILE_UDS_PROPS, udPropHeaders, new UdPropsPrinter());
        exTimeLogger.logEventEnd(ETLE.Event.PRT_UDS_PROPS);
        exTimeLogger.logEventStart(ETLE.Event.PRT_UDS_COMPS);
        printCore(FILE_UDS_COMPS, udCompHeaders, new UdCompsPrinter());
        exTimeLogger.logEventEnd(ETLE.Event.PRT_UDS_COMPS);
    }

    private interface PrinterCore
    {
        void print(String[] headers, CSVPrinter printer) throws IOException;
    }

    private class ProjectMetricsPrinter implements PrinterCore
    {
        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            for (String header : headers)
            {
                printer.print(projectMetricsCalculator.get(header));
            }
            printer.println();
        }
    }

    private class ExTimeLogsPrinter implements PrinterCore
    {
        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            for (ExTimeLogger.ExTimeLoggerEvent event : exTimeLogger.getEvents())
            {
                printer.print(event.getEventId());
                printer.print(event.getParentId());
                printer.print(event.getDurationInMilliSecs());
                printer.print(event.getEventCount());
                printer.print(event.getEventDescription());
                printer.println();
            }
        }
    }


    private class CdPropsPrinter implements PrinterCore
    {
        private String level;

        public CdPropsPrinter(String level)
        {
            this.level = level;
        }

        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            smellPropPrinterCore(headers, printer, level.equals(GraphBuilder.CLASS) ? classSupercycles : packSupercycles);
        }
    }

    private class HdPropsPrinter implements PrinterCore
    {
        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            smellPropPrinterCore(headers, printer, hds);
        }
    }

    private class UdPropsPrinter implements PrinterCore
    {
        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            smellPropPrinterCore(headers, printer, uds);
        }
    }

    private class CdCompsPrinter implements PrinterCore
    {
        private final String level;

        public CdCompsPrinter(String level) { this.level = level; }

        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            cdCompPrinterCore(printer, level.equals(GraphBuilder.CLASS) ? classSupercycles : packSupercycles);
        }
    }

    private class MEFSPrinter implements PrinterCore
    {
        private final String level;
        private final boolean woTinys;

        public MEFSPrinter(String level, boolean woTinys)
        {
            this.level = level;
            this.woTinys = woTinys;
        }

        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            printmEFSCore(printer, level.equals(GraphBuilder.CLASS) ? classSupercycles : packSupercycles,woTinys);
        }
    }


    private class CdEdgesPrinter implements PrinterCore
    {
        private final String level;
        private final Vertex smell;

        public CdEdgesPrinter(String level, Vertex smell)
        {
            this.level = level;
            this.smell = smell;
        }

        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            String depLabel = level.equals(GraphBuilder.CLASS) ? GraphBuilder.LBL_CLASS_DEP : GraphBuilder.LBL_PACK_DEP;
            printCdEdgesCore(printer, smell, depLabel);
        }
    }


    private class HdCompsPrinter implements PrinterCore
    {
        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            hdCompPrinterCore(printer, hds);
        }
    }

    private class UdCompsPrinter implements PrinterCore
    {
        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            udCompPrinterCore(printer, uds);
        }
    }

    private static void smellPropPrinterCore(String[] headers, CSVPrinter printer, List<Vertex> smells) throws IOException
    {
        final int HEADERS_OFFSET = 1;
        for (Vertex smell : smells)
        {
            printer.print(smell.id());
            for (int i = HEADERS_OFFSET; i < headers.length; i++)
            {
                printer.print(smell.value(headers[i]));
            }
            printer.println();
        }
    }

    private static void cdCompPrinterCore(CSVPrinter printer, List<Vertex> smells) throws IOException
    {
        for (Vertex smell : smells)
        {
            printer.print(smell.id());
            printCompNamesCore(printer, smell, GraphBuilder.LABEL_SUPERCYCLE_AFFECTED);
            printer.println();
        }
    }

    private static void hdCompPrinterCore(CSVPrinter printer, List<Vertex> smells) throws IOException
    {
        for (Vertex smell : smells)
        {
            printer.print(smell.id());
            Vertex mainComp = smell.edges(Direction.OUT, GraphBuilder.LABEL_AFFECTED_CLASS).next().inVertex();
            printer.print(mainComp.value(GraphBuilder.PROPERTY_NAME));
            printCompNamesCore(printer, smell, GraphBuilder.LABEL_IS_HL_IN);
            printCompNamesCore(printer, smell, GraphBuilder.LABEL_IS_HL_OUT);
            printer.println();
        }
    }

    private static void udCompPrinterCore(CSVPrinter printer, List<Vertex> smells) throws IOException
    {
        for (Vertex smell : smells)
        {
            printer.print(smell.id());
            Vertex mainComp = smell.edges(Direction.OUT, GraphBuilder.LABEL_AFFECTED_PACKAGE).next().inVertex();
            printer.print(mainComp.value(GraphBuilder.PROPERTY_NAME));
            printCompNamesCore(printer, smell, GraphBuilder.LABEL_BAD_DEPENDENCY);
            printer.println();
        }
    }

    private static void printCompNamesCore(CSVPrinter printer, Vertex smell, String edgeLabel) throws IOException
    {
        StringBuilder compNames = new StringBuilder();
        Iterator<Edge> edges = smell.edges(Direction.OUT, edgeLabel);
        while (edges.hasNext())
        {
            String comp = edges.next().inVertex().value(GraphBuilder.PROPERTY_NAME);
            compNames.append(comp);
            compNames.append(DELIMITER);
        }
        compNames.deleteCharAt(compNames.length() - 1);
        printer.print(compNames);
    }

    private static void printCdEdgesCore(CSVPrinter printer, Vertex supercycle, String depLabel) throws IOException
    {
        Iterator<Edge> compEdges = supercycle.edges(Direction.OUT, GraphBuilder.LABEL_SUPERCYCLE_AFFECTED);

        List<Vertex> affected = new ArrayList<>();
        Map<Vertex, Integer> indexMap = new HashMap<>();
        int size = 0;

        while (compEdges.hasNext())
        {
            Vertex comp = compEdges.next().inVertex();
            affected.add(comp);
            indexMap.put(comp, size);
            size++;
        }

        List<Edge> depEdges = GraphUtils.allEdgesBetweenVertices(new HashSet<>(affected), depLabel);
        boolean[][] edgeMatrix = new boolean[size][size];

        for (Edge edge : depEdges)
        {
            int in = indexMap.get(edge.inVertex());
            int out = indexMap.get(edge.outVertex());
            edgeMatrix[out][in] = true;
        }

        for (int i = 0; i < size; i++)
        {
            printer.print(affected.get(i).value(GraphBuilder.PROPERTY_NAME));
        }
        printer.println();
        for (int i = 0; i < size; i++)
        {
            for (int j = 0; j < size; j++)
            {
                if (j == 0) { printer.print(affected.get(i).value(GraphBuilder.PROPERTY_NAME)); }
                printer.print(edgeMatrix[i][j] ? 1 : 0);
            }
            printer.println();
        }
    }

    private static void printmEFSCore(CSVPrinter printer, List<Vertex> smells, boolean woTinys) throws IOException
    {
        for (Vertex smell : smells)
        {
            Set<Edge> edges = (Set<Edge>) smell.value(woTinys? GraphBuilder.PROPERTY_MEFS_WO_TINYS : GraphBuilder.PROPERTY_MEFS);
            if (edges.size()>0)
            {
                printer.print(smell.id());
                for (Edge edge : edges)
                {
                    String outVertexName = edge.outVertex().value(GraphBuilder.PROPERTY_NAME);
                    String inVertexName = edge.inVertex().value(GraphBuilder.PROPERTY_NAME);

                    printer.print(outVertexName + "->" + inVertexName);
                }
                printer.println();
            }
        }
    }
}
