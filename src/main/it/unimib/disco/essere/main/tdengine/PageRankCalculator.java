package it.unimib.disco.essere.main.tdengine;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import org.apache.tinkerpop.gremlin.structure.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Modded from here
public class PageRankCalculator
{
    private static final double DAMP = 0.85;
    private static final double PR_CHANGE_STOP_TR = 0.0001;
    private static final int MAX_ITERS = 30;
    private static final int LEVEL_CLASS = 0;
    private static final int LEVEL_PACKAGE = 1;

    private Map<Vertex, Double> centralities;

    public PageRankCalculator()
    {
        centralities = new HashMap<>();
    }

    public boolean calculateAllPrVals(Map<String, Vertex> classes, Map<String, Vertex> packages)
    {
        boolean result;
        result = prCalcCore(classes, LEVEL_CLASS);
        result &= prCalcCore(packages, LEVEL_PACKAGE);
        return result;
    }

    private boolean prCalcCore(Map<String, Vertex> components, int level)
    {
        String dependencyLabel;
        String couplingLabel;

        switch (level)
        {
            case LEVEL_CLASS:
            default:
                dependencyLabel = GraphBuilder.LBL_CLASS_DEP;
                couplingLabel = GraphBuilder.PROPERTY_FANOUT;
                break;
            case LEVEL_PACKAGE:
                dependencyLabel = GraphBuilder.LBL_PACK_DEP;
                couplingLabel = GraphBuilder.PROPERTY_NUM_TOTAL_DEPENDENCIES;
                break;
        }

        //int smellCount = ProjectMetricsCalculator.getProjectSmellsCount();
        int smellCount = 1; // Seems to correspond to Table 7.3 of Roveda
        double initPageRank = (1 - DAMP) / smellCount;

        if (components.size() == 0)
        {
            return true;
        }

        // Init PageRank
        for (Vertex node : components.values())
        {
            centralities.put(node, initPageRank);
        }

        double highestPrChange;
        int iterCount = 0;
        do
        {
            highestPrChange = 0;
            for (Vertex node : components.values())
            {
                double pageRank = initPageRank;
                double previousPr = centralities.get(node);
                Iterator<Edge> edgesIn = node.edges(Direction.IN, dependencyLabel);

                while (edgesIn.hasNext())
                {
                    Edge edge = edgesIn.next();
                    Vertex depNode = edge.outVertex();
                    int depCount = depNode.value(couplingLabel);
                    double depPr = centralities.get(depNode);
                    pageRank += (DAMP * depPr / depCount);
                }

                centralities.put(node, pageRank);

                double prChange = Math.abs(pageRank - previousPr);
                highestPrChange = Math.max(highestPrChange, prChange);
            }
            iterCount++;
        }
        while (highestPrChange > PR_CHANGE_STOP_TR && iterCount < MAX_ITERS);
        for (Vertex node : components.values())
        {
            node.property(GraphBuilder.PROPERTY_CENTRALITY, centralities.get(node));
        }

        return true;
    }
}
