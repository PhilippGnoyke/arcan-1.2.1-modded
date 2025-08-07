package it.unimib.disco.essere.main.metricsengine;

import com.google.common.collect.Lists;
import it.unimib.disco.essere.main.ETLE;
import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.graphmanager.EdgeMaps;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

public class MiscSmellMetricsCalculator
{
    private final static int QUARTILE_1_PERCENTILE = 25;
    private final static int QUARTILE_2_PERCENTILE = 50;
    private final static int QUARTILE_3_PERCENTILE = 75;

    private List<Vertex> hds;
    private List<Vertex> uds;
    private List<Vertex> packCds;
    private long packCount;
    private long classCount;
    private EdgeMaps edgeMaps; // Modded
    private ExTimeLogger exTimeLogger; // Modded

    public MiscSmellMetricsCalculator(List<Vertex> hds, List<Vertex> uds, List<Vertex> packCds,
                                      long packCount, long classCount, EdgeMaps edgeMaps, ExTimeLogger exTimeLogger)
    {
        this.hds = hds;
        this.uds = uds;
        this.packCds = packCds;
        this.packCount = packCount;
        this.classCount = classCount;
        this.edgeMaps = edgeMaps;
        this.exTimeLogger = exTimeLogger;
    }

    public void calculateAll()
    {
        calcUdMetrics();
        calcHdMetrics();
        calcPackCdMetrics();
    }

    private void calcPackCdMetrics()
    {
        exTimeLogger.logEventStart(ETLE.Event.WEAK_STRONG_PACK_CDS_CALC);
        for (Vertex smell : packCds)
        {
            calcWeakStrongPackCycleMetrics(smell);
        }
        exTimeLogger.logEventEnd(ETLE.Event.WEAK_STRONG_PACK_CDS_CALC);
    }

    private void calcUdMetrics()
    {
        exTimeLogger.logEventStart(ETLE.Event.UD_METRICS_CALC);
        Percentile percentile = new Percentile();
        percentile = percentile.withEstimationType(Percentile.EstimationType.R_7);

        for (Vertex smell : uds)
        {
            calcHdUdSize(smell, false);
            calcInstGapQuartiles(smell, percentile);
            int order = smell.value(GraphBuilder.PROPERTY_ORDER);
            smell.property(GraphBuilder.PROPERTY_SHARE_PACKAGES, (double) order / packCount);
        }
        exTimeLogger.logEventEnd(ETLE.Event.UD_METRICS_CALC);

    }

    private void calcHdMetrics()
    {
        exTimeLogger.logEventStart(ETLE.Event.HD_METRICS_CALC);
        for (Vertex smell : hds)
        {
            calcHdUdSize(smell, true);
            calcHubRatio(smell);
            calcNumPackages(smell, packCount);
            int order = smell.value(GraphBuilder.PROPERTY_ORDER);
            smell.property(GraphBuilder.PROPERTY_SHARE_CLASSES, (double) order / classCount);
        }
        exTimeLogger.logEventEnd(ETLE.Event.HD_METRICS_CALC);
    }

    private void calcHdUdSize(Vertex smell, boolean hd)
    {
        Set<Vertex> affectedModules = new HashSet<>(); // excluding the main module
        Iterator<Edge> affectedEdges = smell.edges(Direction.OUT);
        while (affectedEdges.hasNext())
        {
            Edge edge = affectedEdges.next();
            affectedModules.add(edge.inVertex());
        }
        String edgeLabel = hd ? PropertyEdge.LABEL_CLASS_DEPENDENCY.toString() : GraphBuilder.LABEL_PACKAGE_AFFERENCE;
        List<Edge> allEdges = edgeMaps.allEdgesBetweenVertices(Lists.newArrayList(affectedModules), edgeLabel);
        int size = allEdges.size();
        int order = (int) smell.value(GraphBuilder.PROPERTY_ORDER);
        smell.property(GraphBuilder.PROPERTY_SIZE, size);
        calcHdUdOverComplexity(smell, order, size);
        calcBackrefShare(smell, allEdges, size);
    }

    private static void calcHdUdOverComplexity(Vertex smell, int order, int size)
    {
        int minSize = order - 1;
        calcSizeOverComplexity(smell, size, minSize);
    }

    public static void calcSmellDensity(Vertex smell, double order, double size)
    {
        double density = (order == 2) ? 0 : (size - order) / (order * order - 2 * order);
        smell.property(GraphBuilder.PROPERTY_DENSITY, density);
    }

    public static Set<Vertex> calcNumPackages(Vertex smell, long packCount)
    {
        return calcNumPackages(smell, packCount, GraphBuilder.LABEL_AFFECTED_CLASS);
    }

    public static Set<Vertex> calcNumPackages(Vertex smell, long packCount, String affectedLabel)
    {
        Set<Vertex> affectPackages = new HashSet<>();
        Iterator<Edge> edges = smell.edges(Direction.OUT, affectedLabel);
        while (edges.hasNext())
        {
            Vertex classVertex = edges.next().inVertex();
            affectPackages.add(GraphUtils.getPackageOfClass(classVertex));
        }
        smell.property(GraphBuilder.PROPERTY_NUM_PACKAGES, affectPackages.size());
        smell.property(GraphBuilder.PROPERTY_SHARE_PACKAGES, (double) affectPackages.size() / packCount);
        return affectPackages;
    }

    public static void calcSizeOverComplexity(Vertex smell, int size, int minSize)
    {
        double sizeOvercomplexity = (double) (size - minSize) / size;
        smell.property(GraphBuilder.PROPERTY_SIZE_OVERCOMPLEYITY, sizeOvercomplexity);
    }

    private static void calcInstGapQuartiles(Vertex smell, Percentile percentile)
    {
        Vertex mainPackage = smell.edges(Direction.OUT, GraphBuilder.LABEL_AFFECTED_PACKAGE).next().inVertex();
        double mainInstab = mainPackage.value(GraphBuilder.PROPERTY_INSTABILITY);

        List<Double> instabGapValsList = new ArrayList<>();
        Iterator<Edge> edges = smell.edges(Direction.OUT, GraphBuilder.LABEL_BAD_DEPENDENCY);
        while (edges.hasNext())
        {
            double badDepInstab = edges.next().inVertex().value(GraphBuilder.PROPERTY_INSTABILITY);
            instabGapValsList.add(badDepInstab - mainInstab);
        }
        double[] instabGapVals = new double[instabGapValsList.size()];
        for (int i = 0; i < instabGapVals.length; i++)
        {
            instabGapVals[i] = instabGapValsList.get(i);
        }
        percentile.setData(instabGapVals);
        double quartile1 = percentile.evaluate(QUARTILE_1_PERCENTILE);
        double quartile2 = percentile.evaluate(QUARTILE_2_PERCENTILE);
        double quartile3 = percentile.evaluate(QUARTILE_3_PERCENTILE);

        smell.property(GraphBuilder.PROPERTY_INSTABILITY_GAP_1ST_QUARTILE, quartile1);
        smell.property(GraphBuilder.PROPERTY_INSTABILITY_GAP_2ND_QUARTILE, quartile2);
        smell.property(GraphBuilder.PROPERTY_INSTABILITY_GAP_3RD_QUARTILE, quartile3);
    }

    private static void calcHubRatio(Vertex smell)
    {
        int cAff = smell.value(GraphBuilder.PROPERTY_HL_FAN_IN);
        int cEff = smell.value(GraphBuilder.PROPERTY_HL_FAN_OUT);

        double hubRatio = (double) (cAff - cEff) / (cAff + cEff);
        smell.property(GraphBuilder.PROPERTY_HUB_RATIO, hubRatio);
    }

    private void calcBackrefShare(Vertex smell, List<Edge> edges, int size)
    {
        int backrefCount = 0;
        for (int i = 0; i < size; i++)
        {
            Edge edge1 = edges.get(i);
            Vertex inVertex1 = edge1.inVertex();
            Vertex outVertex1 = edge1.outVertex();
            if (edgeMaps.existEdge(edge1.label(), inVertex1, outVertex1) != null)
            {
                backrefCount++;
            }
        }
        double backrefShare = (double) backrefCount * 2 / size;
        smell.property(GraphBuilder.PROPERTY_BACKREF_SHARE, backrefShare);
    }


    public static void calcWeakStrongPackCycleMetrics(Vertex packCycleSmell)
    {
        Set<Vertex> allClassCycles = new HashSet<>();
        Set<Vertex> multiPackClassCycles = new HashSet<>();
        Iterator<Edge> packages = packCycleSmell.edges(Direction.OUT, GraphBuilder.LABEL_SUPERCYCLE_AFFECTED);

        while (packages.hasNext())
        {
            Vertex packageVertex = packages.next().inVertex();
            Iterator<Edge> classCycles = packageVertex.edges(Direction.IN, GraphBuilder.LABEL_CLASS_CYCLE_IN_PACK);

            while (classCycles.hasNext())
            {
                Vertex classCycle = classCycles.next().outVertex();
                allClassCycles.add(classCycle);
                if ((int) classCycle.value(GraphBuilder.PROPERTY_NUM_PACKAGES) > 1)
                {
                    multiPackClassCycles.add(classCycle);
                }
            }
        }
        packCycleSmell.property(GraphBuilder.PROPERTY_NUM_CLASS_SUPERCYCLES, allClassCycles.size());
        packCycleSmell.property(GraphBuilder.PROPERTY_NUM_STRONG_PACK_SUPERCYCLES, multiPackClassCycles.size());
    }
}
