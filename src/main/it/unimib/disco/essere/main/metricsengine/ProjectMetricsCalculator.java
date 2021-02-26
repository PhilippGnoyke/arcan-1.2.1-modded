package it.unimib.disco.essere.main.metricsengine;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

// Modded from here
public class ProjectMetricsCalculator
{
    public final static String PROPERTY_LOC = "LOC";
    public final static String PROPERTY_CLASS_COUNT = "classCount";
    public final static String PROPERTY_PACK_COUNT = "packageCount";

    public final static String PROPERTY_SUPERCYCLE_CLASS_CD_COUNT = "classCDCount"; //see CDs
    public final static String PROPERTY_SUPERCYCLE_PACK_CD_COUNT = "packageCDCount"; //see CDs
    public final static String PROPERTY_HD_COUNT = "HDCount";
    public final static String PROPERTY_UD_COUNT = "UDCount";
    public final static String PROPERTY_AS_COUNT = "ASCount"; // Includes supercycle CDs, not subcycle CDs

    public final static String PROPERTY_ASS_PER_LOC = "ASsPerLOC";
    public final static String PROPERTY_ASS_PER_CLASS = "ASsPerClass";
    public final static String PROPERTY_ASS_PER_PACK = "ASsPerPack";

    public final static String PROPERTY_CLASS_CDS_PER_LOC = "classCDsPerLOC";
    public final static String PROPERTY_CLASS_CDS_PER_CLASS = "classCDsPerClass";
    public final static String PROPERTY_PACK_CDS_PER_LOC = "packCDsPerLOC";
    public final static String PROPERTY_PACK_CDS_PER_PACK = "packCDsPerPack";
    public final static String PROPERTY_HDS_PER_LOC = "HDsPerLOC";
    public final static String PROPERTY_HDS_PER_CLASS = "HDsPerClass";
    public final static String PROPERTY_UDS_PER_LOC = "UDsPerLOC";
    public final static String PROPERTY_UDS_PER_PACK = "UDsPerPack";

    public final static String PROPERTY_AS_AFFECTED_CLASSES_COUNT = "ASAffectedClassesCount";
    public final static String PROPERTY_AS_AFFECTED_PACKS_COUNT = "ASAffectedPackagesCount";
    public final static String PROPERTY_AS_MULTI_AFFECTED_CLASSES_COUNT = "ASMultiAffectedClassesCount";
    public final static String PROPERTY_AS_MULTI_AFFECTED_PACKS_COUNT = "ASMultiAffectedPackagesCount";

    public final static String PROPERTY_AS_AFFECTED_CLASSES_DEGREE = "ASAffectedClassesDegree";
    public final static String PROPERTY_AS_AFFECTED_PACKS_DEGREE = "ASAffectedPackagesDegree";
    public final static String PROPERTY_AS_MULTI_AFFECTED_CLASSES_DEGREE = "ASMultiAffectedClassesDegree";
    public final static String PROPERTY_AS_MULTI_AFFECTED_PACKS_DEGREE = "ASMultiAffectedPackagesDegree";

    public final static String PROPERTY_TD_AMOUNT = "TDAmount";
    public final static String PROPERTY_TD_PER_LOC = "TDPerLOC";
    public final static String PROPERTY_TD_PER_CLASS = "TDPerClass";
    public final static String PROPERTY_TD_PER_PACK = "TDPerPackage";
    public final static String PROPERTY_CLASS_CD_SHARE_ON_TD = "classCDShareOnTD";
    public final static String PROPERTY_PACK_CD_SHARE_ON_TD = "packageCDShareOnTD";
    public final static String PROPERTY_HD_SHARE_ON_TD = "HDShareOnTD";
    public final static String PROPERTY_UD_SHARE_ON_TD = "UDShareOnTD";

    // Aliases
    private final static String LBL_CD_AFFECTED = GraphBuilder.LABEL_SUPERCYCLE_AFFECTED;
    private final static String LBL_HD_AFFECTED_1 = GraphBuilder.LABEL_AFFECTED_CLASS;
    private final static String LBL_HD_AFFECTED_2 = GraphBuilder.LABEL_IS_HL_IN;
    private final static String LBL_HD_AFFECTED_3 = GraphBuilder.LABEL_IS_HL_OUT;
    private final static String LBL_UD_AFFECTED_1 = GraphBuilder.LABEL_AFFECTED_PACKAGE;
    private final static String LBL_UD_AFFECTED_2 = GraphBuilder.LABEL_BAD_DEPENDENCY;

    private HashMap<String, Object> projectProps = new HashMap<>();

    private List<Vertex> classSupercycles;
    private List<Vertex> packSupercycles;
    private List<Vertex> hds;
    private List<Vertex> uds;

    public ProjectMetricsCalculator(int classCount, int packCount, List<Vertex> classSupercycles,
                                    List<Vertex> packSupercycles, List<Vertex> hds, List<Vertex> uds)
    {
        projectProps.put(PROPERTY_CLASS_COUNT, classCount);
        projectProps.put(PROPERTY_PACK_COUNT, packCount);
        this.classSupercycles = classSupercycles;
        this.packSupercycles = packSupercycles;
        this.hds = hds;
        this.uds = uds;
    }

    public Object get(String key)
    {
        return projectProps.get(key);
    }

    public void put(String key, Object value)
    {
        projectProps.put(key, value);
    }

    public int getTotalAsAffectedComps()
    {
        return (int) get(PROPERTY_AS_AFFECTED_CLASSES_COUNT) + (int) get(PROPERTY_AS_AFFECTED_PACKS_COUNT);
    }

    public void updateAsCounts(int loc)
    {
        put(PROPERTY_LOC, loc);

        int classCdCount = classSupercycles.size();
        int packCdCount = packSupercycles.size();
        int hdCount = hds.size();
        int udCount = uds.size();
        int asCount = classCdCount + packCdCount + hdCount + udCount;

        put(PROPERTY_SUPERCYCLE_CLASS_CD_COUNT, classCdCount);
        put(PROPERTY_SUPERCYCLE_PACK_CD_COUNT, packCdCount);
        put(PROPERTY_HD_COUNT, hdCount);
        put(PROPERTY_UD_COUNT, udCount);
        put(PROPERTY_AS_COUNT, asCount);

        calcAndPutRelSmellCounts(asCount, PROPERTY_ASS_PER_LOC, PROPERTY_ASS_PER_CLASS, PROPERTY_ASS_PER_PACK);
        calcAndPutRelSmellCounts(classCdCount, PROPERTY_CLASS_CDS_PER_LOC, PROPERTY_CLASS_CDS_PER_CLASS, null);
        calcAndPutRelSmellCounts(packCdCount, PROPERTY_PACK_CDS_PER_LOC, null, PROPERTY_PACK_CDS_PER_PACK);
        calcAndPutRelSmellCounts(hdCount, PROPERTY_HDS_PER_LOC, PROPERTY_HDS_PER_CLASS, null);
        calcAndPutRelSmellCounts(udCount, PROPERTY_UDS_PER_LOC, null, PROPERTY_UDS_PER_PACK);
    }

    private void calcAndPutRelSmellCounts(int dividend, String keyLoc, String keyClass, String keyPack)
    {
        put(keyLoc, (double) dividend / (int) get(PROPERTY_LOC));
        if (keyClass != null)
        {
            put(keyClass, (double) dividend / (int) get(PROPERTY_CLASS_COUNT));
        }
        if (keyPack != null)
        {
            put(keyPack, (double) dividend / (int) get(PROPERTY_PACK_COUNT));
        }
    }

    public void calcProjAsAffectedCompsAndOverlapRatios()
    {
        Set<Vertex> affectedClasses = new HashSet<>();
        Set<Vertex> affectedPacks = new HashSet<>();
        Set<Vertex> multiAffectedClasses = new HashSet<>();
        Set<Vertex> multiAffectedPacks = new HashSet<>();

        addVerticesToSetCore(affectedClasses, multiAffectedClasses, classSupercycles, LBL_CD_AFFECTED);
        addVerticesToSetCore(affectedPacks, multiAffectedPacks, packSupercycles, LBL_CD_AFFECTED);
        addVerticesToSetCore(affectedClasses, multiAffectedClasses, hds, LBL_HD_AFFECTED_1, LBL_HD_AFFECTED_2, LBL_HD_AFFECTED_3);
        addVerticesToSetCore(affectedPacks, multiAffectedPacks, uds, LBL_UD_AFFECTED_1, LBL_UD_AFFECTED_2);

        put(PROPERTY_AS_AFFECTED_CLASSES_COUNT, affectedClasses.size());
        put(PROPERTY_AS_AFFECTED_PACKS_COUNT, affectedPacks.size());
        put(PROPERTY_AS_MULTI_AFFECTED_CLASSES_COUNT, multiAffectedClasses.size());
        put(PROPERTY_AS_MULTI_AFFECTED_PACKS_COUNT, multiAffectedPacks.size());

        int classCount = (int) get(PROPERTY_CLASS_COUNT);
        int packCount = (int) get(PROPERTY_PACK_COUNT);
        put(PROPERTY_AS_AFFECTED_CLASSES_DEGREE, (double) affectedClasses.size() / classCount);
        put(PROPERTY_AS_AFFECTED_PACKS_DEGREE, (double) affectedPacks.size() / packCount);
        put(PROPERTY_AS_MULTI_AFFECTED_CLASSES_DEGREE, (double) multiAffectedClasses.size() / classCount);
        put(PROPERTY_AS_MULTI_AFFECTED_PACKS_DEGREE, (double) multiAffectedPacks.size() / packCount);

        calculateOverlapRatiosSimple(classSupercycles, multiAffectedClasses, LBL_CD_AFFECTED);
        calculateOverlapRatiosSimple(packSupercycles, multiAffectedPacks, LBL_CD_AFFECTED);
        calculateOverlapRatiosRedundantEdges(hds, multiAffectedClasses, LBL_HD_AFFECTED_1, LBL_HD_AFFECTED_2, LBL_HD_AFFECTED_3);
        calculateOverlapRatiosSimple(uds, multiAffectedPacks, LBL_UD_AFFECTED_1, LBL_UD_AFFECTED_2);
    }

    private void calculateOverlapRatiosSimple(List<Vertex> smells, Set<Vertex> multiAffectedComps, String... labels)
    {
        for (Vertex smell : smells)
        {
            int compCount = 0;
            int multiAffectedCount = 0;
            Iterator<Edge> edges = smell.edges(Direction.OUT, labels);
            while (edges.hasNext())
            {
                Vertex comp = edges.next().inVertex();
                compCount++;
                if (multiAffectedComps.contains(comp))
                {
                    multiAffectedCount++;
                }
            }
            calculateAndAssignOverlapRatio(smell, multiAffectedCount, compCount);
        }
    }

    private void calculateOverlapRatiosRedundantEdges(List<Vertex> smells, Set<Vertex> multiAffectedComps, String... labels)
    {
        for (Vertex smell : smells)
        {
            int multiAffectedCount = 0;
            Set<Vertex> comps = new HashSet<>();
            Iterator<Edge> edges = smell.edges(Direction.OUT, labels);
            while (edges.hasNext())
            {
                Vertex comp = edges.next().inVertex();
                if (!comps.contains(comp))
                {
                    comps.add(comp);
                    if (multiAffectedComps.contains(comp))
                    {
                        multiAffectedCount++;
                    }
                }
            }
            calculateAndAssignOverlapRatio(smell, multiAffectedCount, comps.size());
        }
    }

    private void calculateAndAssignOverlapRatio(Vertex smell, int multiAffectedCount, int compCount)
    {
        double overlapRatio = (double) multiAffectedCount / compCount;
        smell.property(GraphBuilder.PROPERTY_OVERLAP_RATIO, overlapRatio);
    }

    private void addVerticesToSetCore
        (Set<Vertex> affected, Set<Vertex> multiAffected, List<Vertex> smells, String... labels)
    {
        if (smells != null)
        {
            for (Vertex smell : smells)
            {
                Iterator<Edge> edges = smell.edges(Direction.OUT, labels);
                while (edges.hasNext())
                {
                    Vertex vertex = edges.next().inVertex();
                    if (!affected.contains(vertex))
                    {
                        affected.add(vertex);
                    }
                    else
                    {
                        multiAffected.add(vertex);
                    }
                }
            }
        }
    }

    public void calculateProjectTdMetrics()
    {
        double classCdTd = sumUpTd(classSupercycles);
        double packCdTd = sumUpTd(packSupercycles);
        double hdTd = sumUpTd(hds);
        double udTd = sumUpTd(uds);

        double totalTd = classCdTd + packCdTd + hdTd + udTd;
        put(PROPERTY_TD_AMOUNT, totalTd);
        put(PROPERTY_TD_PER_LOC, totalTd / (int) get(PROPERTY_LOC));
        put(PROPERTY_TD_PER_CLASS, totalTd / (int) get(PROPERTY_CLASS_COUNT));
        put(PROPERTY_TD_PER_PACK, totalTd / (int) get(PROPERTY_PACK_COUNT));
        put(PROPERTY_CLASS_CD_SHARE_ON_TD, classCdTd / totalTd);
        put(PROPERTY_PACK_CD_SHARE_ON_TD, packCdTd / totalTd);
        put(PROPERTY_HD_SHARE_ON_TD, hdTd / totalTd);
        put(PROPERTY_UD_SHARE_ON_TD, udTd / totalTd);
    }

    private double sumUpTd(List<Vertex> smells)
    {
        double smellTypeTdSum = 0;
        for (Vertex smell : smells)
        {
            smellTypeTdSum += (double) smell.value(GraphBuilder.PROPERTY_TDI);
        }
        return smellTypeTdSum;
    }
}
