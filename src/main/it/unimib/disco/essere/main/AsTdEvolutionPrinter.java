package it.unimib.disco.essere.main;

import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.SuperCycleDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.metricsengine.ProjectMetricsCalculator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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

    public static final String ID = "id";
    public static final String AFFECTED_COMPS = "affectedComponents";
    public static final String MAIN_COMP = "mainComponent";
    public static final String LESS_STABLE_PACKS = "lessStableDependedOnPackages";
    public static final String AFFERENT_CLASSES = "afferentClasses";
    public static final String EFFERENT_CLASSES = "efferentClasses";

    public static final String DELIMITER = ",";

    private OutputDirUtils outputDirUtils;
    private ProjectMetricsCalculator projectMetricsCalculator;
    private List<Vertex> classSupercycles;
    private List<Vertex> packSupercycles;
    private List<Vertex> hds;
    private List<Vertex> uds;

    public AsTdEvolutionPrinter(OutputDirUtils outputDirUtils, ProjectMetricsCalculator projectMetricsCalculator,
                                List<Vertex> classSupercycles, List<Vertex> packSupercycles,
                                List<Vertex> hds, List<Vertex> uds)
    {
        this.outputDirUtils = outputDirUtils;
        this.projectMetricsCalculator = projectMetricsCalculator;
        this.hds = hds;
        this.uds = uds;
        this.classSupercycles = classSupercycles;
        this.packSupercycles = packSupercycles;
    }

    public void printAll() throws IOException, NullPointerException
    {
        printProjectMetrics();
        printClassCds();
        printPackCds();
        printHds();
        printUds();
    }

    private void printCore(String file, String[] headers, PrinterCore printerCore) throws IOException, NullPointerException
    {
        File fileCsv = outputDirUtils.getFileInOutputFolder(file);
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
        ProjectMetricsCalculator.PROPERTY_CLASS_COUNT,
        ProjectMetricsCalculator.PROPERTY_PACK_COUNT,
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
        ProjectMetricsCalculator.PROPERTY_AS_MULTI_AFFECTED_PACKS_DEGREE
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
    };

    public final static String[] classCdPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_SHAPE,
            GraphBuilder.PROPERTY_NUM_SUBCYCLES,
            GraphBuilder.PROPERTY_NUM_INHERIT_EDGES,
            GraphBuilder.PROPERTY_REL_NUM_INHERIT_EDGES
        });

    public final static String[] packCdPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_SHAPE,
            GraphBuilder.PROPERTY_NUM_SUBCYCLES,
        });

    public final static String[] hdPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_HL_FAN_IN,
            GraphBuilder.PROPERTY_HL_FAN_OUT,
            GraphBuilder.PROPERTY_HUB_RATIO
        });

    public final static String[] udPropHeaders = mergeStringArrays(
        generalSmellPropHeaders, new String[]{
            GraphBuilder.PROPERTY_DOUD,
            GraphBuilder.PROPERTY_INSTABILITY_GAP_1ST_QUARTILE,
            GraphBuilder.PROPERTY_INSTABILITY_GAP_2ND_QUARTILE,
            GraphBuilder.PROPERTY_INSTABILITY_GAP_3RD_QUARTILE
        });

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
        printCore(FILE_PROJECT, projectMetricsHeaders, new ProjectMetricsPrinter());
    }

    public void printClassCds() throws IOException, NullPointerException
    {
        printCore(FILE_CLASS_CDS_PROPS, classCdPropHeaders, new CdPropsPrinter(GraphBuilder.CLASS));
        printCore(FILE_CLASS_CDS_COMPS, cdCompHeaders, new CdCompsPrinter(GraphBuilder.CLASS));
    }

    public void printPackCds() throws IOException, NullPointerException
    {
        printCore(FILE_PACK_CDS_PROPS, packCdPropHeaders, new CdPropsPrinter(GraphBuilder.PACKAGE));
        printCore(FILE_PACK_CDS_COMPS, cdCompHeaders, new CdCompsPrinter(GraphBuilder.PACKAGE));
    }

    public void printHds() throws IOException, NullPointerException
    {
        printCore(FILE_HDS_PROPS, hdPropHeaders, new HdPropsPrinter());
        printCore(FILE_HDS_COMPS, hdCompHeaders, new HdCompsPrinter());
    }

    public void printUds() throws IOException, NullPointerException
    {
        printCore(FILE_UDS_PROPS, udPropHeaders, new UdPropsPrinter());
        printCore(FILE_UDS_COMPS, udCompHeaders, new UdCompsPrinter());
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
        private String level;

        public CdCompsPrinter(String level)
        {
            this.level = level;
        }

        public void print(String[] headers, CSVPrinter printer) throws IOException
        {
            cdCompPrinterCore(printer,  level.equals(GraphBuilder.CLASS) ? classSupercycles : packSupercycles);
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
}
