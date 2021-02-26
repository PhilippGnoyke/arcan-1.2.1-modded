package it.unimib.disco.essere.main.tdengine;

import it.unimib.disco.essere.main.asengine.CyclicDependencyDetector;
import it.unimib.disco.essere.main.asengine.HubLikeDetector;
import it.unimib.disco.essere.main.asengine.SuperCycleDetector;
import it.unimib.disco.essere.main.asengine.UnstableDependencyDetector;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.metricsengine.ProjectMetricsCalculator;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

// Modded from here
public class TdSmellCalculator
{
    private static final QuantileCalculator quanCal = QuantileCalculator.getInstance();
    private int totalAsDepNum;

    private List<Vertex> classCds;
    private List<Vertex> packCds;
    private List<Vertex> hds;
    private List<Vertex> uds;

    public TdSmellCalculator(List<Vertex> classCds, List<Vertex> packCds, List<Vertex> hds, List<Vertex> uds)
    {
        this.classCds = classCds;
        this.packCds = packCds;
        this.hds = hds;
        this.uds = uds;
    }

    public boolean calculateAllTdVals(ProjectMetricsCalculator projectMetricsCalculator)
    {
        totalAsDepNum = projectMetricsCalculator.getTotalAsAffectedComps();
        calculateCdClassTdVals();
        calculateCdPackTdVals();
        calculateHdTdVals();
        calculateUdTdVals();
        return true;
    }

    private void calculateCdClassTdVals()
    {
        if (classCds != null)
        {
            String quanProp = GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES;
            TdSmellTypeUtil smellTypeUtil = new TdCdClassUtil();
            calculateTdValsCore(classCds, smellTypeUtil, quanProp);
        }
    }

    private void calculateCdPackTdVals()
    {
        if (packCds != null)
        {
            String quanProp = GraphBuilder.PROPERTY_NUM_CYCLE_VERTICES;
            TdSmellTypeUtil smellTypeUtil = new TdCdPackUtil();
            calculateTdValsCore(packCds, smellTypeUtil, quanProp);
        }
    }

    private void calculateHdTdVals()
    {
        if (hds != null)
        {
            String quanProp = GraphBuilder.PROPERTY_HL_TOTAL_DEPENDENCY;
            TdSmellTypeUtil smellTypeUtil = new TdHdUtil();
            calculateTdValsCore(hds, smellTypeUtil, quanProp);
        }
    }

    private void calculateUdTdVals()
    {
        if (uds != null)
        {
            String quanProp = GraphBuilder.PROPERTY_NUM_BAD_DEPENDENCIES;
            TdSmellTypeUtil smellTypeUtil = new TdUdUtil();
            calculateTdValsCore(uds, smellTypeUtil, quanProp);
        }
    }

    private void calculateTdValsCore(List<Vertex> smells, TdSmellTypeUtil smellTypeUtil, String quanProp)
    {
        for (Vertex smell : smells)
        {
            int count = smell.value(quanProp);
            int weight = smellTypeUtil.weight(smell, count);
            double ss = smellTypeUtil.sevScore(count);
            double pr = smellTypeUtil.pageRank(smell);
            //double prQuant = smellTypeUtil.pageRankQuant(pr); // Using the quantiles results in almost no smells with a TDI?!
            assignTdVars(smell, ss, pr, pr, weight, totalAsDepNum);
        }
    }

    private void assignTdVars(Vertex smell, double ss, double pr, double prQuant, int weight, int totalAsDepNum)
    {
        double tdi = smellTdi(ss, prQuant, weight, totalAsDepNum);
        smell.property(GraphBuilder.PROPERTY_CENTRALITY, pr);
        smell.property(GraphBuilder.PROPERTY_ORDER, weight);
        smell.property(GraphBuilder.PROPERTY_SEVERITY_SCORE, ss);
        smell.property(GraphBuilder.PROPERTY_TDI, tdi);
    }

    private static double calculateCdPr(Vertex smell)
    {
        double prMax = 0;
        Iterator<Edge> edges = smell.edges(Direction.OUT, GraphBuilder.LABEL_CYCLE_AFFECTED);

        while (edges.hasNext())
        {
            Edge edge = edges.next();
            Vertex cdVertex = edge.inVertex();
            double prVertex = cdVertex.value(GraphBuilder.PROPERTY_CENTRALITY);
            prMax = Math.max(prMax, prVertex);
        }
        return prMax;
    }

    private static double calculateHdUdPr(Vertex smell, String labelAffected)
    {
        Iterator<Edge> edges = smell.edges(Direction.OUT, labelAffected);
        Edge edge = edges.next(); // Always contains a single element
        Vertex pack = edge.inVertex();
        return pack.value(GraphBuilder.PROPERTY_CENTRALITY);
    }

    // Calculates the TDI of a single smell
    // Requires the smell to know its severity score
    private double smellTdi(double sevScore, double pageRank, int smellWeight, int totalAsDepNum, double history)
    {
        return sevScore * pageRank * smellWeight / totalAsDepNum * history;
    }

    // TDI calculation without the history variable
    private double smellTdi(double sevScore, double pageRank, int smellWeight, int totalAsDepNum)
    {
        return smellTdi(sevScore, pageRank, smellWeight, totalAsDepNum, 1);
    }

    public static void calculateSuperCdTdVals(List<Vertex> supercycles)
    {
        if (supercycles != null)
        {
            for (Vertex supercycle : supercycles)
            {
                Iterator<Edge> edges = supercycle.edges(Direction.OUT, GraphBuilder.LABEL_SUB_OF_SUPERCYCLE);
                double prMax = 0;
                double tdi = 0;
                double sevScore = 0;
                while (edges.hasNext())
                {
                    Vertex subcycle = edges.next().inVertex();
                    double pr = subcycle.value(GraphBuilder.PROPERTY_CENTRALITY);
                    tdi += (double) subcycle.value(GraphBuilder.PROPERTY_TDI);
                    sevScore += (double) subcycle.value(GraphBuilder.PROPERTY_SEVERITY_SCORE);
                    if (pr > prMax)
                    {
                        prMax = pr;
                    }
                }
                supercycle.property(GraphBuilder.PROPERTY_CENTRALITY, prMax);
                supercycle.property(GraphBuilder.PROPERTY_TDI, tdi);
                supercycle.property(GraphBuilder.PROPERTY_SEVERITY_SCORE, sevScore);
            }
        }
    }

    private interface TdSmellTypeUtil
    {
        double sevScore(int count);

        double pageRank(Vertex smell);

        double pageRankQuant(double pr);

        // Smell is required for HDs
        int weight(Vertex smell, int count);
    }

    private class TdCdClassUtil implements TdSmellTypeUtil
    {
        public double sevScore(int nov) { return quanCal.quantileCdClassNov(nov); }

        public double pageRank(Vertex smell)
        {
            return calculateCdPr(smell);
        }

        public double pageRankQuant(double pr)
        {
            return quanCal.quantileCdClassPr(pr);
        }

        public int weight(Vertex smell, int nov) {return nov;}
    }

    private class TdCdPackUtil implements TdSmellTypeUtil
    {
        public double sevScore(int nov) { return quanCal.quantileCdPackNov(nov); }

        public double pageRank(Vertex smell)
        {
            return calculateCdPr(smell);
        }

        public double pageRankQuant(double pr)
        {
            return quanCal.quantileCdPackPr(pr);
        }

        public int weight(Vertex smell, int nov) {return nov;}
    }

    private class TdHdUtil implements TdSmellTypeUtil
    {
        public double sevScore(int ntd) { return quanCal.quantileHdNtd(ntd); }

        public double pageRank(Vertex smell)
        {
            return calculateHdUdPr(smell, GraphBuilder.LABEL_AFFECTED_CLASS);
        }

        public double pageRankQuant(double pr)
        {
            return quanCal.quantileHdPr(pr);
        }

        // Incoming and outgoing deps can overlap. This requires some extra logic to find the number of unique classes.
        public int weight(Vertex smell, int ntd)
        {
            Set<Vertex> uniqueSet = new HashSet<>();

            Iterator<Edge> inEdges = smell.edges(Direction.OUT, GraphBuilder.LABEL_IS_HL_IN);
            while (inEdges.hasNext())
            {
                Edge edge = inEdges.next();
                Vertex vertex = edge.inVertex();
                uniqueSet.add(vertex);
            }
            Iterator<Edge> outEdges = smell.edges(Direction.OUT, GraphBuilder.LABEL_IS_HL_OUT);
            while (outEdges.hasNext())
            {
                Edge edge = outEdges.next();
                Vertex vertex = edge.inVertex();
                uniqueSet.add(vertex);
            }
            return uniqueSet.size();
        }
    }

    private class TdUdUtil implements TdSmellTypeUtil
    {
        public double sevScore(int nud) { return quanCal.quantileUdNud(nud); }

        public double pageRank(Vertex smell)
        {
            return calculateHdUdPr(smell, GraphBuilder.LABEL_AFFECTED_PACKAGE);
        }

        public double pageRankQuant(double pr)
        {
            return quanCal.quantileUdPr(pr);
        }

        public int weight(Vertex smell, int nud) {return nud + 1;}
    }
}



