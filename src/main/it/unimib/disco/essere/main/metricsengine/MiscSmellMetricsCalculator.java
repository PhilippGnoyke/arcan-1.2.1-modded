package it.unimib.disco.essere.main.metricsengine;

import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
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

    // Aliases
    private static final String LBL_CLASS_DEP = PropertyEdge.LABEL_CLASS_DEPENDENCY.toString();
    private static final String LBL_PACK_DEP = GraphBuilder.LABEL_PACKAGE_AFFERENCE;

    private List<Vertex> hds;
    private List<Vertex> uds;

    public MiscSmellMetricsCalculator(List<Vertex> hds, List<Vertex> uds)
    {
        this.hds = hds;
        this.uds = uds;
    }

    public void calculateAll()
    {
        calcUdMetrics();
        calcHdMetrics();
    }

    private void calcUdMetrics()
    {
        Percentile percentile = new Percentile();
        percentile = percentile.withEstimationType(Percentile.EstimationType.R_7);

        for (Vertex smell : uds)
        {
            calcHdUdSize(smell, LBL_PACK_DEP, GraphBuilder.LABEL_AFFECTED_PACKAGE);
            calcInstGapQuartiles(smell, percentile);
        }
    }

    private void calcHdMetrics()
    {
        for (Vertex smell : hds)
        {
            calcHdUdSize(smell, LBL_CLASS_DEP, GraphBuilder.LABEL_AFFECTED_CLASS);
            calcHubRatio(smell);
        }
    }


    private static void calcHdUdSize(Vertex smell, String depLabel, String mainModuleLabel)
    {
        Set<Vertex> affectedModules = new HashSet<>(); // excluding the main module
        int size = 0;
        Iterator<Edge> edges = smell.edges(Direction.OUT);
        while (edges.hasNext())
        {
            Edge edge = edges.next();
            if (!edge.label().equals(mainModuleLabel))
            {
                size++;
                affectedModules.add(edge.inVertex());
            }
        }
        size += GraphUtils.allEdgesBetweenVertices(affectedModules, depLabel).size();
        smell.property(GraphBuilder.PROPERTY_SIZE, size);
        calcHdUdOverComplexity(smell, size);
    }

    private static void calcHdUdOverComplexity(Vertex smell, int size)
    {
        int minSize = (int) smell.value(GraphBuilder.PROPERTY_ORDER) - 1;
        calcSizeOverComplexity(smell, size, minSize);
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


}
