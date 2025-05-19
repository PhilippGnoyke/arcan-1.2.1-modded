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
        CD_DETECTION,
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
        GB_MTHD_PROCESS_INSTRUCTIONS,
        GB_MTHD_PROCESS_ANNOTATIONS,
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
        CDS_SUPERCYLE_CLASS_CD_DETECTION,
        CDS_SUPERCYLE_PACK_CD_DETECTION,
        CDS_SUPERCYLE_CLASS_CD_SHAPE,
        CDS_SUPERCYLE_PACK_CD_SHAPE,
        CDS_SUBCYCLE_CLASS_CD_DETECTION,
        CDS_SUBCYCLE_PACK_CD_DETECTION,
        CDS_SUPERCYCLE_CLASS_CD_MEFS,
        CDS_SUPERCYCLE_PACK_CD_MEFS,
        CLASS_CD_SHAPES_BACKREF,
        CLASS_CD_SHAPES_CHAIN,
        CLASS_CD_SHAPES_STAR,
        CLASS_CD_SHAPES_HUB_CYCLE_GRAPH,
        CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY,
        CLASS_CD_SHAPES_HUB_GINI,
        PACK_CD_SHAPES_BACKREF,
        PACK_CD_SHAPES_CHAIN,
        PACK_CD_SHAPES_STAR,
        PACK_CD_SHAPES_HUB_CYCLE_GRAPH,
        PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY,
        PACK_CD_SHAPES_HUB_GINI,
        AS_AFFECTED_COUNTS_CALC,
        OVERLAP_DETER,
        PAGERANK_CALC,
        TD_CALC,
        UPDATE_AS_COUNTS,
        HD_METRICS_CALC,
        UD_METRICS_CALC,
        WEAK_STRONG_PACK_CDS_CALC,
        PROJECT_METRICS_AGGREGATES_CALC,
        READ_WALK_JARS,
        READ_READ_BYTES,
        READ_INIT_IN_STREAMS,
        READ_GET_JAR_ENTRY,
        READ_INIT_OUT_STREAMS,
        READ_PARSE_CLASS,
        READ_ADD_TO_REPO,
        READ_ADD_TO_LISTS,
        BC_CLASS_INIT_VERTICES,
        BC_CLASS_SE_INIT_QUEUES,
        BC_CLASS_SE_SETUP_VERTICES,
        BC_CLASS_SE_PROCESS_QUEUE,
        BC_CLASS_SE_PROCESS_EDGES,
        BC_CLASS_EMPTY_QUEUE,
        BC_PACK_INIT_VERTICES,
        BC_PACK_SE_INIT_QUEUES,
        BC_PACK_SE_SETUP_VERTICES,
        BC_PACK_SE_PROCESS_QUEUE,
        BC_PACK_SE_PROCESS_EDGES,
        BC_PACK_EMPTY_QUEUE,
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
            case CD_DETECTION -> {return "Cycle detection";}
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
            case GB_MTHD_PROCESS_INSTRUCTIONS -> {return "GraphBuilder method dependency: Instruction processing";}
            case GB_MTHD_PROCESS_ANNOTATIONS -> {return "GraphBuilder method dependency: Annotation processing";}
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
            case CDS_SUPERCYLE_CLASS_CD_DETECTION -> {return "Class-level supercycle detection";}
            case CDS_SUPERCYLE_PACK_CD_DETECTION -> {return "Package-level supercycle detection";}
            case CDS_SUPERCYLE_CLASS_CD_SHAPE -> {return "Class-level supercycle shape assignment";}
            case CDS_SUPERCYLE_PACK_CD_SHAPE -> {return "Package-level supercycle shape assignment";}
            case CDS_SUBCYCLE_CLASS_CD_DETECTION -> {return "Class-level subcycle detection";}
            case CDS_SUBCYCLE_PACK_CD_DETECTION -> {return "Package-level subcycle detection";}
            case CDS_SUPERCYCLE_CLASS_CD_MEFS -> {return "Class-level supercycle MEFS calculation";}
            case CDS_SUPERCYCLE_PACK_CD_MEFS -> {return "Package-level subcycle MEFS calculation";}
            case CLASS_CD_SHAPES_BACKREF -> {return "Class-level supercycle shape classification: Backref calculation";}
            case CLASS_CD_SHAPES_CHAIN -> {return "Class-level supercycle shape classification: Chain determination";}
            case CLASS_CD_SHAPES_STAR -> {return "Class-level supercycle shape classification: Star determination";}
            case CLASS_CD_SHAPES_HUB_CYCLE_GRAPH -> {
                return "Class-level supercycle shape classification: Hub determination (Cycle graph creation)";
            }
            case CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY -> {
                return "Class-level supercycle shape classification: Hub determination (Betweenness centrality calculation)";
            }
            case CLASS_CD_SHAPES_HUB_GINI -> {
                return "Class-level supercycle shape classification: Hub determination (Gini calculation)";
            }
            case PACK_CD_SHAPES_BACKREF -> {
                return "Package-level supercycle shape classification: Backref calculation";
            }
            case PACK_CD_SHAPES_CHAIN -> {return "Package-level supercycle shape classification: Chain determination";}
            case PACK_CD_SHAPES_STAR -> {return "Package-level supercycle shape classification: Star determination";}
            case PACK_CD_SHAPES_HUB_CYCLE_GRAPH -> {
                return "Package-level supercycle shape classification: Hub determination (Cycle graph creation)";
            }
            case PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY -> {
                return "Package-level supercycle shape classification: Hub determination (Betweenness centrality calculation)";
            }
            case PACK_CD_SHAPES_HUB_GINI -> {
                return "Package-level supercycle shape classification: Hub determination (Gini calculation)";
            }
            case AS_AFFECTED_COUNTS_CALC -> {return "Architecture-smell affected component counting";}
            case OVERLAP_DETER -> {return "Overlap determination";}
            case PAGERANK_CALC -> {return "PageRank calculation";}
            case TD_CALC -> {return "Technical debt calculation";}
            case UPDATE_AS_COUNTS -> {return "Architecture-smell count update";}
            case HD_METRICS_CALC -> {return "Hub-like dependency metrics calculation";}
            case UD_METRICS_CALC -> {return "Unstable dependency metrics calculation";}
            case WEAK_STRONG_PACK_CDS_CALC -> {return "Weak/strong package-level cyclic dependency determination";}
            case PROJECT_METRICS_AGGREGATES_CALC -> {return "Project metric aggregates calculation";}
            case READ_WALK_JARS -> {return "Binary file reading: Jar walking";}
            case READ_READ_BYTES -> {return "Binary file reading: Bytes reading";}
            case READ_INIT_IN_STREAMS -> {return "Binary file reading: Input streams initializing";}
            case READ_GET_JAR_ENTRY -> {return "Binary file reading: Jar entry getting";}
            case READ_INIT_OUT_STREAMS -> {return "Binary file reading: Output streams initializing";}
            case READ_PARSE_CLASS -> {return "Binary file reading: Class parsing";}
            case READ_ADD_TO_REPO -> {return "Binary file reading: To repo adding";}
            case READ_ADD_TO_LISTS -> {return "Binary file reading: To lists adding";}
            case BC_CLASS_INIT_VERTICES -> {return "Betweeness centrality class-level: Vertex initializing";}
            case BC_CLASS_SE_INIT_QUEUES -> {return "Betweeness centrality simple explore class-level: Queue initializing";}
            case BC_CLASS_SE_SETUP_VERTICES -> {return "Betweeness centrality simple explore class-level: Vertex setup";}
            case BC_CLASS_SE_PROCESS_QUEUE -> {return "Betweeness centrality simple explore class-level: Queue processing";}
            case BC_CLASS_SE_PROCESS_EDGES -> {return "Betweeness centrality simple explore class-level: edge processing";}
            case BC_CLASS_EMPTY_QUEUE -> {return "Betweeness centrality class-level: Queue emptying";}
            case BC_PACK_INIT_VERTICES -> {return "Betweeness centrality package-level: Vertex initializing";}
            case BC_PACK_SE_INIT_QUEUES -> {
                return "Betweeness centrality simple explore package-level: Queue initializing";
            }
            case BC_PACK_SE_SETUP_VERTICES -> {
                return "Betweeness centrality simple explore package-level: Vertex setup";
            }
            case BC_PACK_SE_PROCESS_QUEUE -> {
                return "Betweeness centrality simple explore package-level: Queue processing";
            }
            case BC_PACK_SE_PROCESS_EDGES -> {
                return "Betweeness centrality simple explore package-level: edge processing";
            }
            case BC_PACK_EMPTY_QUEUE -> {return "Betweeness centrality package-level: Queue emptying";}

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
            case GB_MTHD_PROCESS_INSTRUCTIONS -> {return Event.GB_METHOD_DEP;}
            case GB_MTHD_PROCESS_ANNOTATIONS -> {return Event.GB_METHOD_DEP;}
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
            case CDS_SUPERCYLE_CLASS_CD_DETECTION -> {return Event.CD_DETECTION;}
            case CDS_SUPERCYLE_PACK_CD_DETECTION -> {return Event.CD_DETECTION;}
            case CDS_SUPERCYLE_CLASS_CD_SHAPE -> {return Event.CD_DETECTION;}
            case CDS_SUPERCYLE_PACK_CD_SHAPE -> {return Event.CD_DETECTION;}
            case CDS_SUBCYCLE_CLASS_CD_DETECTION -> {return Event.CD_DETECTION;}
            case CDS_SUBCYCLE_PACK_CD_DETECTION -> {return Event.CD_DETECTION;}
            case CDS_SUPERCYCLE_CLASS_CD_MEFS -> {return Event.CD_DETECTION;}
            case CDS_SUPERCYCLE_PACK_CD_MEFS -> {return Event.CD_DETECTION;}
            case CLASS_CD_SHAPES_BACKREF -> {return Event.CDS_SUPERCYLE_CLASS_CD_SHAPE;}
            case CLASS_CD_SHAPES_CHAIN -> {return Event.CDS_SUPERCYLE_CLASS_CD_SHAPE;}
            case CLASS_CD_SHAPES_STAR -> {return Event.CDS_SUPERCYLE_CLASS_CD_SHAPE;}
            case CLASS_CD_SHAPES_HUB_CYCLE_GRAPH -> {return Event.CDS_SUPERCYLE_CLASS_CD_SHAPE;}
            case CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY -> {return Event.CDS_SUPERCYLE_CLASS_CD_SHAPE;}
            case CLASS_CD_SHAPES_HUB_GINI -> {return Event.CDS_SUPERCYLE_CLASS_CD_SHAPE;}
            case PACK_CD_SHAPES_BACKREF -> {return Event.CDS_SUPERCYLE_PACK_CD_SHAPE;}
            case PACK_CD_SHAPES_CHAIN -> {return Event.CDS_SUPERCYLE_PACK_CD_SHAPE;}
            case PACK_CD_SHAPES_STAR -> {return Event.CDS_SUPERCYLE_PACK_CD_SHAPE;}
            case PACK_CD_SHAPES_HUB_CYCLE_GRAPH -> {return Event.CDS_SUPERCYLE_PACK_CD_SHAPE;}
            case PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY -> {return Event.CDS_SUPERCYLE_PACK_CD_SHAPE;}
            case PACK_CD_SHAPES_HUB_GINI -> {return Event.CDS_SUPERCYLE_PACK_CD_SHAPE;}
            case AS_AFFECTED_COUNTS_CALC -> {return Event.TD_OVERLAP_CALC;}
            case OVERLAP_DETER -> {return Event.TD_OVERLAP_CALC;}
            case PAGERANK_CALC -> {return Event.TD_OVERLAP_CALC;}
            case TD_CALC -> {return Event.TD_OVERLAP_CALC;}
            case UPDATE_AS_COUNTS -> {return Event.MISC_METRICS_CALC;}
            case HD_METRICS_CALC -> {return Event.MISC_METRICS_CALC;}
            case UD_METRICS_CALC -> {return Event.MISC_METRICS_CALC;}
            case WEAK_STRONG_PACK_CDS_CALC -> {return Event.MISC_METRICS_CALC;}
            case PROJECT_METRICS_AGGREGATES_CALC -> {return Event.MISC_METRICS_CALC;}
            case READ_WALK_JARS -> {return Event.CLASS_AND_PACK_READING;}
            case READ_READ_BYTES -> {return Event.CLASS_AND_PACK_READING;}
            case READ_INIT_IN_STREAMS -> {return Event.CLASS_AND_PACK_READING;}
            case READ_GET_JAR_ENTRY -> {return Event.CLASS_AND_PACK_READING;}
            case READ_INIT_OUT_STREAMS -> {return Event.CLASS_AND_PACK_READING;}
            case READ_PARSE_CLASS -> {return Event.CLASS_AND_PACK_READING;}
            case READ_ADD_TO_REPO -> {return Event.CLASS_AND_PACK_READING;}
            case READ_ADD_TO_LISTS -> {return Event.CLASS_AND_PACK_READING;}
            case BC_CLASS_INIT_VERTICES -> {return Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_CLASS_SE_INIT_QUEUES -> {return Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_CLASS_SE_SETUP_VERTICES -> {return Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_CLASS_SE_PROCESS_QUEUE -> {return Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_CLASS_SE_PROCESS_EDGES -> {return Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_CLASS_EMPTY_QUEUE -> {return Event.CLASS_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_PACK_INIT_VERTICES -> {return Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_PACK_SE_INIT_QUEUES -> {return Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_PACK_SE_SETUP_VERTICES -> {return Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_PACK_SE_PROCESS_QUEUE -> {return Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_PACK_SE_PROCESS_EDGES -> {return Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            case BC_PACK_EMPTY_QUEUE -> {return Event.PACK_CD_SHAPES_HUB_BETWEENNESS_CENTRALITY;}
            default -> {return null;}
        }
    }


}
