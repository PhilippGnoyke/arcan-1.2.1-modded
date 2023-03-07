package it.unimib.disco.essere.main.asengine;

import it.unimib.disco.essere.main.asengine.alg.TarjansAlgorithm;
import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Modded from here
public class SuperCycleDetector
{

    private List<Vertex> superCycleClassCdSmells; //(reduce graph traversals)
    private List<Vertex> superCyclePackageCdSmells; //(reduce graph traversals)

    // Aliases
    public static final String LBL_CLASS_DEP = PropertyEdge.LABEL_CLASS_DEPENDENCY.toString();
    public static final String LBL_PACK_DEP = GraphBuilder.LABEL_PACKAGE_AFFERENCE;

    public void detectAndRegisterSuperCycles
        (Graph graph, List<Vertex> classes, List<Vertex> packs, List<Vertex> classSubs, List<Vertex> packSubs)
    {
        superCycleClassCdSmells = detectAndRegisterSuperCyclesCore(graph, classes, GraphBuilder.CLASS, LBL_CLASS_DEP, classSubs);
        superCyclePackageCdSmells = detectAndRegisterSuperCyclesCore(graph, packs, GraphBuilder.PACKAGE, LBL_PACK_DEP, packSubs);
    }

    private List<Vertex> detectAndRegisterSuperCyclesCore
        (Graph graph, List<Vertex> comps, String vertexType, String depLabel, List<Vertex> subcycles)
    {
        TarjansAlgorithm tarjansAlgorithm = new TarjansAlgorithm(graph, comps, vertexType, depLabel);
        tarjansAlgorithm.calc();
        List<Vertex> supercycles = tarjansAlgorithm.getSupercycles();
        assignSubCycles(subcycles,tarjansAlgorithm.getVertexIdsToSupercycleIds());
        return supercycles;
    }

    private static void assignSubCycles
        (List<Vertex> subcycles, Map<Object,Vertex> vertexIdsToSupercycles)
    {
        for (Vertex subCycle : subcycles)
        {
            Object cycleStartId = GraphUtils.getEdgesByVertex
                (GraphBuilder.LABEL_START_CYCLE, subCycle, Direction.OUT).get(0).inVertex().id();
            Vertex supercycle = vertexIdsToSupercycles.get(cycleStartId);
            supercycle.addEdge(GraphBuilder.LABEL_SUB_OF_SUPERCYCLE, subCycle);
            GraphUtils.incrementVertexIntProperty(supercycle, GraphBuilder.PROPERTY_NUM_SUBCYCLES);
        }
    }

    public List<Vertex> getListOfSuperCycleSmells(String vertexType)
    {
        return vertexType.equals(GraphBuilder.CLASS) ? superCycleClassCdSmells : superCyclePackageCdSmells;
    }
}
