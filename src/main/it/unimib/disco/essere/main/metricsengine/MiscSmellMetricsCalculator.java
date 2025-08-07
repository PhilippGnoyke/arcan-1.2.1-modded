package it.unimib.disco.essere.main.metricsengine;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
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

    public MiscSmellMetricsCalculator(List<Vertex> hds, List<Vertex> uds, List<Vertex> packCds,
                                      long packCount, long classCount)
    {
        this.hds = hds;
        this.uds = uds;
        this.packCds = packCds;
        this.packCount = packCount;
        this.classCount = classCount;
    }

    public void calculateAll()
    {
        calcUdMetrics();
        calcHdMetrics();
        calcPackCdMetrics();
    }

    private void calcPackCdMetrics()
    {
        for (Vertex smell : packCds)
        {
            calcWeakStrongPackCycleMetrics(smell);
        }
    }

    private void calcUdMetrics()
    {
        Percentile percentile = new Percentile();
        percentile = percentile.withEstimationType(Percentile.EstimationType.R_7);

        for (Vertex smell : uds)
        {
            calcHdUdSize(smell);
            calcInstGapQuartiles(smell, percentile);
            int order = smell.value(GraphBuilder.PROPERTY_ORDER);
            smell.property(GraphBuilder.PROPERTY_SHARE_PACKAGES, (double)order/packCount);
        }
    }

    private void calcHdMetrics()
    {
        for (Vertex smell : hds)
        {
            calcHdUdSize(smell);
            calcHubRatio(smell);
            calcNumPackages(smell,packCount);
            int order = smell.value(GraphBuilder.PROPERTY_ORDER);
            smell.property(GraphBuilder.PROPERTY_SHARE_CLASSES, (double) order/classCount);
        }
    }

    private static void calcHdUdSize(Vertex smell)
    {
        Set<Vertex> affectedModules = new HashSet<>(); // excluding the main module
        Iterator<Edge> affectedEdges = smell.edges(Direction.OUT);
        while (affectedEdges.hasNext())
        {
            Edge edge = affectedEdges.next();
            affectedModules.add(edge.inVertex());
        }
        List<Edge> allEdges = GraphUtils.allEdgesBetweenVertices(affectedModules);
        int size = allEdges.size();
        int order = (int) smell.value(GraphBuilder.PROPERTY_ORDER);
        smell.property(GraphBuilder.PROPERTY_SIZE, size);
        calcHdUdOverComplexity(smell, order, size);
        calcBackrefShare(smell,allEdges,size);
    }

    private static void calcHdUdOverComplexity(Vertex smell, int order, int size)
    {
        int minSize = order - 1;
        calcSizeOverComplexity(smell, size, minSize);
    }

    public static void calcSmellDensity(Vertex smell,double order, double size)
    {
        double density = (order==2)? 0 : (size - order)/(order*order-2*order);
        smell.property(GraphBuilder.PROPERTY_DENSITY, density);
    }

    public static Set<Vertex> calcNumPackages(Vertex smell, long packCount)
    {
        return calcNumPackages( smell,  packCount,  GraphBuilder.LABEL_AFFECTED_CLASS);
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
        smell.property(GraphBuilder.PROPERTY_SHARE_PACKAGES, (double)affectPackages.size()/packCount);
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


    private static void calcBackrefShare(Vertex smell, List<Edge> edges, int size)
    {
        int backrefCount = 0;
        for (int i = 0; i < size; i++)
        {
            Edge edge1 = edges.get(i);
            Vertex inVertex1 = edge1.inVertex();
            Vertex outVertex1 = edge1.outVertex();
            for (int j = i + 1; j < size; j++)
            {
                Edge edge2 = edges.get(j);
                Vertex inVertex2 = edge2.inVertex();
                Vertex outVertex2 = edge2.outVertex();
                if (inVertex1.id().equals(outVertex2.id()) && outVertex1.id().equals(inVertex2.id()))
                {
                    backrefCount++;
                    break;
                }
            }
        }
        double backrefShare = (double) backrefCount * 2 / size;
        smell.property(GraphBuilder.PROPERTY_BACKREF_SHARE, backrefShare);
    }

    public static void calcMEFS(Vertex smell, Set<Vertex> comps, List<Edge> edges)
    {
        MEFSCalculator mefsCalculator = new MEFSCalculator(comps,edges);
        smell.property(GraphBuilder.PROPERTY_MEFS_SIZE, mefsCalculator.getMEFSSize());
        smell.property(GraphBuilder.PROPERTY_REL_MEFS_SIZE, mefsCalculator.getRelativeMEFSSize());
        smell.property(GraphBuilder.PROPERTY_MEFS, mefsCalculator.getEdgeFeedbackSet());
        smell.property(GraphBuilder.PROPERTY_MEFS_SIZE_WO_TINYS, mefsCalculator.getMEFSSizeWOTinys());
        smell.property(GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS, mefsCalculator.getRelativeMEFSSizeWOTinys());
        smell.property(GraphBuilder.PROPERTY_MEFS_WO_TINYS, mefsCalculator.getEdgeFeedbackSetWOTinys());
        smell.property(GraphBuilder.PROPERTY_REL_MEFS_SIZE_WO_TINYS_REDUCTION, mefsCalculator.getMEFSSizeWOTinysReduction());
    }

    public static void calcWeakStrongPackCycleMetrics(Vertex packCycleSmell)
    {
        Set<Vertex> allClassCycles = new HashSet<>();
        Set<Vertex> multiPackClassCycles = new HashSet<>();
        Iterator<Edge> packages = packCycleSmell.edges(Direction.OUT,GraphBuilder.LABEL_SUPERCYCLE_AFFECTED);

        while (packages.hasNext())
        {
            Vertex packageVertex = packages.next().inVertex();
            Iterator<Edge> classCycles =  packageVertex.edges(Direction.IN,GraphBuilder.LABEL_CLASS_CYCLE_IN_PACK);

            while (classCycles.hasNext())
            {
                Vertex classCycle = classCycles.next().outVertex();
                allClassCycles.add(classCycle);
                if ((int)classCycle.value(GraphBuilder.PROPERTY_NUM_PACKAGES)>1)
                {
                    multiPackClassCycles.add(classCycle);
                }
            }
        }
        packCycleSmell.property(GraphBuilder.PROPERTY_NUM_CLASS_SUPERCYCLES,allClassCycles.size());
        packCycleSmell.property(GraphBuilder.PROPERTY_NUM_STRONG_PACK_SUPERCYCLES ,multiPackClassCycles.size());
    }
}
