package it.unimib.disco.essere.main.asengine;

import it.unimib.disco.essere.main.ETLE;
import it.unimib.disco.essere.main.ExTimeLogger;
import it.unimib.disco.essere.main.asengine.alg.TarjansAlgorithm;
import it.unimib.disco.essere.main.graphmanager.*;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Modded from here
public class SuperCycleDetector
{

    private List<Vertex> superCycleClassCdSmells; //(reduce graph traversals)
    private List<Vertex> superCyclePackageCdSmells; //(reduce graph traversals)
    private List<Vertex> classSubcycles;
    private List<Vertex> packSubcycles;
    private long packCount;
    private long classCount;
    private EdgeMaps edgeMaps;
    ClassFilter classFilter;
    private ExTimeLogger exTimeLogger;

    public SuperCycleDetector()
    {
        classSubcycles = new ArrayList<>();
        packSubcycles = new ArrayList<>();
    }


    public void detectAndRegisterSuperCycles
        (Graph graph, Map<String, Vertex> classes, Map<String, Vertex> packs,
         long packCount, long classCount, EdgeMaps edgeMaps, ClassFilter classFilter, ExTimeLogger exTimeLogger)
    {
        this.packCount = packCount;
        this.classCount = classCount;
        this.edgeMaps = edgeMaps;
        this.classFilter = classFilter;
        this.exTimeLogger = exTimeLogger;
        superCycleClassCdSmells = detectAndRegisterSuperCyclesCore(graph, classes, GraphBuilder.CLASS, classSubcycles);
        superCyclePackageCdSmells = detectAndRegisterSuperCyclesCore(graph, packs, GraphBuilder.PACKAGE, packSubcycles);
    }

    private List<Vertex> detectAndRegisterSuperCyclesCore
        (Graph graph, Map<String, Vertex> comps, String vertexType, List<Vertex> subcycles)
    {
        ETLE.Event event;
        ETLE.Event[] toBeSubtracted;
        if (vertexType.equals(GraphBuilder.CLASS))
        {
            event = ETLE.Event.CDS_SUPERCYLE_CLASS_CD_DETECTION;
            toBeSubtracted = new ETLE.Event[]{
                ETLE.Event.CDS_SUBCYCLE_CLASS_CD_DETECTION,
                ETLE.Event.CDS_SUPERCYCLE_CLASS_CD_MEFS,
                ETLE.Event.CDS_SUPERCYLE_CLASS_CD_SHAPE};
        }
        else
        {
            event = ETLE.Event.CDS_SUPERCYLE_PACK_CD_DETECTION;
            toBeSubtracted = new ETLE.Event[]{
                ETLE.Event.CDS_SUBCYCLE_PACK_CD_DETECTION,
                ETLE.Event.CDS_SUPERCYCLE_PACK_CD_MEFS,
                ETLE.Event.CDS_SUPERCYLE_PACK_CD_SHAPE};
        }
        exTimeLogger.logEventStart(event);
        TarjansAlgorithm tarjansAlgorithm = new TarjansAlgorithm(graph, comps,subcycles, vertexType,
            packCount, classCount, edgeMaps, classFilter, exTimeLogger);
        tarjansAlgorithm.calc();
        List<Vertex> supercycles = tarjansAlgorithm.getSupercycles();

        exTimeLogger.logEventEnd(event);
        exTimeLogger.subtractEventsFromEvent(event, toBeSubtracted);
        return supercycles;
    }

    public List<Vertex> getListOfSuperCycleSmells(String vertexType)
    {
        return vertexType.equals(GraphBuilder.CLASS) ? superCycleClassCdSmells : superCyclePackageCdSmells;
    }

    public List<Vertex> getListOfSubCycles(String vertexType)
    {
        return vertexType.equals(GraphBuilder.CLASS) ? classSubcycles : packSubcycles;
    }

}
