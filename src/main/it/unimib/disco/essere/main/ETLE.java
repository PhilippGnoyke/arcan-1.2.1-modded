package it.unimib.disco.essere.main;

//ExTimeLogger events
public final class ETLE
{
    public enum Event
    {
        CLASS_AND_PACK_READING,
        GRAPH_CREATION,
        CALC_CLASS_METRICS,
        CALC_PACK_METRICS,
        SUBCYLE_CD_DETECTION,
        UD_DETECTION,
        HD_DETECTION,
        SUPERCYLE_CD_DETECTION,
        TD_OVERLAP_CALC,
        MISC_METRICS_CALC,
        ARCAN_PRINTING,
        ARCAN_RUNNING,
        INIT_VERSIONS,
        PROCESS_INTRA_SMELLS,
        PROCESS_INTER_SMELLS,
        CALC_SMELL_DELTAS,
        ASTDEA_PRINTING,
        GB_SEARCH_NODE,
        GB_SUPER_DEP,
        GB_INTERFACE_DEP,
        GB_METHOD_DEP,
        GB_PACKAGE_DEP,
        GB_CALC_AFFERENT_COUPLING,
        GB_MTHD_PARSE_CLASS_NAME,
        GB_MTHD_ADD_NODE_TO_GRAPH,
        GB_MTHD_UPDATE_EDGES,
        GB_MTHD_UPDATE_COUPLING,
        GB_MTHD_INIT_DEPS,
        GB_MTHD_GET_METHOD_GEN,
        GB_MTHD_COMPUTE_INSTRUCTIONS,
        GB_MTHD_COMPUTE_ANNOTATIONS,
        PRT_PROJECT_METRICS,
        PRT_CLASS_CDS_PROPS,
        PRT_CLASS_CDS_COMPS,
        PRT_CLASS_CDS_EDGES,
        PRT_CLASS_CDS_MEFS,
        PRT_PACK_CDS_PROPS,
        PRT_PACK_CDS_COMPS,
        PRT_PACK_CDS_EDGES,
        PRT_PACK_CDS_MEFS,
        PRT_HDS_PROPS,
        PRT_HDS_COMPS,
        PRT_UDS_PROPS,
        PRT_UDS_COMPS,
    }

    public static String getText(Event event)
    {
        switch (event)
        {
            case CLASS_AND_PACK_READING -> {return "Class and package from binary files reading";}
            case GRAPH_CREATION -> {return "Class and package dependency graph creation";}
            case CALC_CLASS_METRICS -> {return "Class metrics calculation";}
            case CALC_PACK_METRICS -> {return "Package metrics calculation";}
            case SUBCYLE_CD_DETECTION -> {return "Subcycle detection";}
            case UD_DETECTION -> {return "Unstable dependency detection";}
            case HD_DETECTION -> {return "Hub-like dependency detection";}
            case SUPERCYLE_CD_DETECTION -> {return "Supercycle detection";}
            case TD_OVERLAP_CALC -> {return "Technical debt and overlap ratio calculation";}
            case MISC_METRICS_CALC -> {return "Misc metrics calculation";}
            case ARCAN_PRINTING -> {return "Arcan Printing";}
            case ARCAN_RUNNING -> {return "Arcan running";}
            case INIT_VERSIONS -> {return "Version initialization";}
            case PROCESS_INTRA_SMELLS -> {return "Intra-version smell processing";}
            case PROCESS_INTER_SMELLS -> {return "Inter-version smell processing";}
            case CALC_SMELL_DELTAS -> {return "Smell delta calculation";}
            case ASTDEA_PRINTING -> {return "AsTdEA printing";}
            case GB_SEARCH_NODE -> {return "GraphBuilder node searching";}
            case GB_SUPER_DEP -> {return "GraphBuilder super dependency checking and creating";}
            case GB_INTERFACE_DEP -> {return "GraphBuilder interface dependency checking and creating";}
            case GB_METHOD_DEP -> {return "GraphBuilder method dependency checking and creating";}
            case GB_PACKAGE_DEP -> {return "GraphBuilder package dependency checking and creating";}
            case GB_CALC_AFFERENT_COUPLING -> {return "GraphBuilder afferent coupling calculation";}
            case GB_MTHD_PARSE_CLASS_NAME -> {return "GraphBuilder method dependency: Class name parsing";}
            case GB_MTHD_ADD_NODE_TO_GRAPH -> {return "GraphBuilder method dependency: Node to graph adding";}
            case GB_MTHD_UPDATE_EDGES -> {return "GraphBuilder method dependency: Edge updating";}
            case GB_MTHD_UPDATE_COUPLING -> {return "GraphBuilder method dependency: Coupling updating";}
            case GB_MTHD_INIT_DEPS -> {return "GraphBuilder method dependency: Dependency initializing";}
            case GB_MTHD_GET_METHOD_GEN -> {return "GraphBuilder method dependency: Method generating";}
            case GB_MTHD_COMPUTE_INSTRUCTIONS -> {return "GraphBuilder method dependency: Instruction computing";}
            case GB_MTHD_COMPUTE_ANNOTATIONS -> {return "GraphBuilder method dependency: Annotation computing";}
            case PRT_PROJECT_METRICS -> {return "Project metrics printing";}
            case PRT_CLASS_CDS_PROPS -> {return "Class-level cyclic-dependency properties printing";}
            case PRT_CLASS_CDS_COMPS -> {return "Class-level cyclic-dependency components printing";}
            case PRT_CLASS_CDS_EDGES -> {return "Class-level cyclic-dependency edges printing";}
            case PRT_CLASS_CDS_MEFS -> {return "Class-level cyclic-dependency minimum edge feedback set printing";}
            case PRT_PACK_CDS_PROPS -> {return "Package-level cyclic-dependency properties printing";}
            case PRT_PACK_CDS_COMPS -> {return "Package-level cyclic-dependency components printing";}
            case PRT_PACK_CDS_EDGES -> {return "Package-level cyclic-dependency edges printing";}
            case PRT_PACK_CDS_MEFS -> {return "Package-level cyclic-dependency minimum edge feedback set printing";}
            case PRT_HDS_PROPS -> {return "Hub-like dependency properties printing";}
            case PRT_HDS_COMPS -> {return "Hub-like dependency components printing";}
            case PRT_UDS_PROPS -> {return "Unstable dependency properties printing";}
            case PRT_UDS_COMPS -> {return "Unstable dependency components printing";}
        }
        return null;
    }

    public static Event getParent(Event child)
    {
        switch (child)
        {
            case GB_SEARCH_NODE -> {return Event.GRAPH_CREATION;}
            case GB_SUPER_DEP -> {return Event.GRAPH_CREATION;}
            case GB_INTERFACE_DEP -> {return Event.GRAPH_CREATION;}
            case GB_METHOD_DEP -> {return Event.GRAPH_CREATION;}
            case GB_PACKAGE_DEP -> {return Event.GRAPH_CREATION;}
            case GB_CALC_AFFERENT_COUPLING -> {return Event.GRAPH_CREATION;}
            case GB_MTHD_PARSE_CLASS_NAME -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_ADD_NODE_TO_GRAPH -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_UPDATE_EDGES -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_UPDATE_COUPLING -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_INIT_DEPS -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_GET_METHOD_GEN -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_COMPUTE_INSTRUCTIONS -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_COMPUTE_ANNOTATIONS -> {return Event.GB_METHOD_DEP;}
            case PRT_PROJECT_METRICS -> {return Event.ARCAN_PRINTING;}
            case PRT_CLASS_CDS_PROPS -> {return Event.ARCAN_PRINTING;}
            case PRT_CLASS_CDS_COMPS -> {return Event.ARCAN_PRINTING;}
            case PRT_CLASS_CDS_EDGES -> {return Event.ARCAN_PRINTING;}
            case PRT_CLASS_CDS_MEFS -> {return Event.ARCAN_PRINTING;}
            case PRT_PACK_CDS_PROPS -> {return Event.ARCAN_PRINTING;}
            case PRT_PACK_CDS_COMPS -> {return Event.ARCAN_PRINTING;}
            case PRT_PACK_CDS_EDGES -> {return Event.ARCAN_PRINTING;}
            case PRT_PACK_CDS_MEFS -> {return Event.ARCAN_PRINTING;}
            case PRT_HDS_PROPS -> {return Event.ARCAN_PRINTING;}
            case PRT_HDS_COMPS -> {return Event.ARCAN_PRINTING;}
            case PRT_UDS_PROPS -> {return Event.ARCAN_PRINTING;}
            case PRT_UDS_COMPS -> {return Event.ARCAN_PRINTING;}
            default -> {return null;}
        }
    }

}
