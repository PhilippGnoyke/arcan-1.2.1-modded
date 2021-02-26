package it.unimib.disco.essere.main.tdengine;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import org.apache.tinkerpop.gremlin.structure.*;

import java.util.Iterator;
import java.util.List;

// Modded from here
public class PageRankCalculator
{
    private static final double DAMP = 0.85;
    private static final double PR_CHANGE_STOP_TR = 0.0000001;
    private static final int MAX_ITERS = 50;
    private static final int LEVEL_CLASS = 0;
    private static final int LEVEL_PACKAGE = 1;

    public static boolean calculateAllPrVals(Graph graph)
    {
        boolean result;
        result = prCalcCore(graph, LEVEL_CLASS);
        result &= prCalcCore(graph, LEVEL_PACKAGE);
        return result;
    }

    private static boolean prCalcCore(Graph graph, int level)
    {
        String vertexLabel;
        String dependencyLabel;
        String couplingLabel;

        switch (level)
        {
            case LEVEL_CLASS:
            default:
                vertexLabel = GraphBuilder.CLASS;
                dependencyLabel = PropertyEdge.LABEL_CLASS_DEPENDENCY.toString();
                couplingLabel = GraphBuilder.PROPERTY_FANOUT;
                break;
            case LEVEL_PACKAGE:
                vertexLabel = GraphBuilder.PACKAGE;
                dependencyLabel = GraphBuilder.LABEL_PACKAGE_AFFERENCE;
                couplingLabel = GraphBuilder.PROPERTY_NUM_TOTAL_DEPENDENCIES;
                break;
        }

        //int smellCount = ProjectMetricsCalculator.getProjectSmellsCount();
        int smellCount = 1; // Seems to correspond to Table 7.3 of Roveda
        double initPageRank = (1 - DAMP) / smellCount;

        List<Vertex> vertices = GraphUtils.findVerticesByLabel(graph, vertexLabel);
        if (vertices == null)
        {
            return true;
        }

        // Init PageRank
        for (Vertex node : vertices)
        {
            node.property(GraphBuilder.PROPERTY_CENTRALITY, initPageRank);
        }

        double highestPrChange;
        int iterCount = 0;
        do
        {
            highestPrChange = 0;
            for (Vertex node : vertices)
            {
                double pageRank = initPageRank;
                double previousPr = node.value(GraphBuilder.PROPERTY_CENTRALITY);
                Iterator<Edge> edgesIn = node.edges(Direction.IN, dependencyLabel);

                while (edgesIn.hasNext())
                {
                    Edge edge = edgesIn.next();
                    Vertex depNode = edge.outVertex();
                    int depCount = depNode.value(couplingLabel);
                    double depPr = depNode.value(GraphBuilder.PROPERTY_CENTRALITY);
                    pageRank += (DAMP * depPr / depCount);
                }
                node.property(GraphBuilder.PROPERTY_CENTRALITY, pageRank);
                double prChange = Math.abs(pageRank - previousPr);
                highestPrChange = Math.max(highestPrChange, prChange);
            }
            iterCount++;
        }
        while (highestPrChange > PR_CHANGE_STOP_TR && iterCount < MAX_ITERS);
        return true;
    }
}
