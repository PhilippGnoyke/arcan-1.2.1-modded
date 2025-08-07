package it.unimib.disco.essere.main.graphmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import it.unimib.disco.essere.main.ExTimeLogger;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import it.unimib.disco.essere.main.ETLE.Event;

public class GraphBuilder {

	//properties
	public static final int DEFAULT_NUM_DEPENDENCES = 1;
	public static final String PROPERTY_DEPEDENCY_WEIGHT = "Weight";
	public static final String PROPERTY_DEPEDENCY_WEIGHT_MIN = "MinWeight";
	public static final String PROPERTY_DEPEDENCY_WEIGHT_MAX = "MaxWeight";
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_CLASS_TYPE = "ClassType";
	public static final String PROPERTY_PACKAGE_TYPE = "PackageType";
	public static final String PROPERTY_CLASS_MODIFIER = "classModifier";
	public static final String PROPERTY_NUM_TOTAL_DEPENDENCIES = "numTotalDep";
	public static final String PROPERTY_NUM_BAD_DEPENDENCIES = "numBadDep";
	public static final String PROPERTY_DOUD = "DOUD"; // Modded (renamed)
	public static final String PROPERTY_SMELL_TYPE = "smellType";
	public static final String PROPERTY_CENTRALITY = "centrality"; // Modded
	public static final String PROPERTY_SEVERITY_SCORE = "severityScore"; // Modded
	public static final String PROPERTY_TDI = "tdi"; // Modded
	public static final String PROPERTY_ORDER = "order"; // Modded
	public static final String PROPERTY_SIZE = "size"; // Modded
	public static final String PROPERTY_SIZE_OVERCOMPLEYITY = "sizeOvercomplexity"; // Modded
	public static final String PROPERTY_OVERLAP_RATIO = "overlapRatio"; // Modded
	public static final String PROPERTY_INSTABILITY_GAP_1ST_QUARTILE = "instabilityGap1stQuartile"; // Modded
	public static final String PROPERTY_INSTABILITY_GAP_2ND_QUARTILE = "instabilityGap2ndQuartile"; // Modded
	public static final String PROPERTY_INSTABILITY_GAP_3RD_QUARTILE = "instabilityGap3rdQuartile"; // Modded
	public static final String PROPERTY_HUB_RATIO = "hubRatio"; // Modded
	public static final String PROPERTY_NUM_INHERIT_EDGES = "numInheritEdges"; // Modded
	public static final String PROPERTY_REL_NUM_INHERIT_EDGES = "relNumInheritEdges"; // Modded
	public static final String PROPERTY_BACKREF_SHARE = "backrefShare"; // Modded
	public static final String PROPERTY_DENSITY = "density"; // Modded
	public static final String PROPERTY_MEFS_SIZE = "mEFSSize"; // Modded
	public static final String PROPERTY_MEFS_SIZE_WO_TINYS = "mEFSSizeWOTinys"; // Modded
	public static final String PROPERTY_MEFS = "mEFS"; // Modded
	public static final String PROPERTY_MEFS_WO_TINYS = "mEFSWOTinys"; // Modded
	public static final String PROPERTY_REL_MEFS_SIZE = "relmEFS"; // Modded
	public static final String PROPERTY_REL_MEFS_SIZE_WO_TINYS = "relmEFSWOTinys"; // Modded
	public static final String PROPERTY_REL_MEFS_SIZE_WO_TINYS_REDUCTION = "mEFSWOTinysReduction"; // Modded

	public static final String PROPERTY_NUM_PACKAGES = "numPackages"; // Modded
	public static final String PROPERTY_SHARE_CLASSES = "shareClassesInVersion"; // Modded
	public static final String PROPERTY_SHARE_PACKAGES = "sharePackagesInVersion"; // Modded
	public static final String PROPERTY_NUM_CLASS_SUPERCYCLES = "numClassSupercycles"; // Modded
	public static final String PROPERTY_NUM_STRONG_PACK_SUPERCYCLES = "numStrongPackCycles"; // Modded

	//property metrics package
	public static final String PROPERTY_INSTABILITY = "instability";
	public static final String PROPERTY_INSTABILITY_INTERNAL = "instabilityInternal";
	public static final String PROPERTY_CE = "CE";
	public static final String PROPERTY_CE_INTERNAL = "CEInternal";
	public static final String PROPERTY_CA = "CA";
	public static final String PROPERTY_CA_INTERNAL = "CAinternal";
	public static final String PROPERTY_RMA = "A";
	public static final String PROPERTY_RMD = "D";
	//property metrics class
	public static final String PROPERTY_FANIN = "fanIn";
	public static final String PROPERTY_FANOUT = "fanOut";
	public static final String PROPERTY_LCOM = "LCOM";
	public static final String PROPERTY_CBO = "CBO";
	//property cycle
	public static final String PROPERTY_NUM_CYCLE_VERTICES = "numCycleVertices";
	public static final String PROPERTY_VERTEX_TYPE = "vertexType";
	public static final String PROPERTY_ORDER_IN_CYCLE = "orderInCycle";
	public static final String PROPERTY_SHAPE = "shape"; // Modded (name changed)
	//property supercycle (modded)
	public static final String PROPERTY_NUM_SUBCYCLES = "numSubcycles";
	//property ixpd
	public static final String PROPERTY_IXPD_RATIO_IN = "ratioIn";
	public static final String PROPERTY_IXPD_RATIO_OUT = "ratioOut";
	public static final String PROPERTY_IXPD_LINK_OUT = "linkOut";
	public static final String PROPERTY_IXPD_LINK_IN = "linkIn";
	//property hl
	public static final String PROPERTY_HL_FAN_IN = "afferentCoupling"; // Modded (renamed)
	public static final String PROPERTY_HL_FAN_OUT = "efferentCoupling"; // Modded (renamed)
	public static final String PROPERTY_HL_TOTAL_DEPENDENCY = "totalDependency";

	//node labels
	public static final String CLASS = "class";
	public static final String PACKAGE = "package";
	public static final String SMELL = "smell";
	public static final String CYCLE_SHAPE = "cycleShape";
	public static final String COMPONENT = "component";

	//property values
	public static final String INTERFACE = "Interface";
	public static final String ABSTRACT_CLASS = "Abstract";
	public static final String NO_MODIFIER = "none";
	public static final String SYSTEM_PACKAGE = "SystemPackage";
	public static final String SYSTEM_CLASS = "SystemClass";
	public static final String RETRIEVED_CLASS = "RetrievedClass";
	public static final String RETRIEVED_PACKAGE = "RetrievedPackage";
	public static final String UNSTABLE_DEPENDENCY = "unstableDep";
	public static final String CYCLIC_DEPENDENCY = "cyclicDep";
	public static final String SUPER_CYCLIC_DEPENDENCY = "superCyclicDep"; // Modded
	public static final String HUBLIKE_DEPENDENCY = "hubLikeDep";
	public static final String IMPLICIT_CROSS_PACKAGE_DEPENDENCY = "implicitXPkgDep";
	public static final String TINY = "tiny"; // Modded
	public static final String CHAIN = "chain";
	public static final String STAR = "star";
	public static final String CIRCLE = "circle";
	public static final String CLIQUE = "clique";
	public static final String MULTI_HUB = "multiHub"; // Modded
	public static final String SEMI_CLIQUE = "semiClique"; // Modded
	public static final String UNKNOWN = "unknown"; // Modded


	public static final String LABEL_AFFERENCE = "isAfferentOf";
	public static final String LABEL_PACKAGE_AFFERENCE = "packageIsAfferentOf";
	public static final String LABEL_BAD_DEPENDENCY = "badDep";
	public static final String LABEL_AFFECTED_PACKAGE = "affectedPackage";
	public static final String LABEL_AFFECTED_CLASS = "affectedClass";
	public static final String LABEL_CYCLE_AFFECTED = "partOfCycle";
	public static final String LABEL_SUPERCYCLE_AFFECTED = "partOfSuperCycle"; // Modded
	public static final String LABEL_SUB_OF_SUPERCYCLE = "subOfSuperCycle"; // Modded
	public static final String LABEL_CLASS_CYCLE_IN_PACK = "classCycleInPack"; // Modded
	public static final String LABEL_START_CYCLE = "cycleStart";
	public static final String LABEL_IS_CENTRE_OF_STAR = "isCentreOfStar";
	public static final String LABEL_IS_PART_OF_STAR = "isPartOfStar";
	public static final String LABEL_IS_PART_OF_CHAIN = "isPartOfChain";
	public static final String LABEL_IS_CIRCLE_SHAPED = "isCircleShaped";
	public static final String LABEL_IS_CLIQUE_SHAPED = "isCliqueShaped";
	public static final String LABEL_IS_HL_OUT = "isHLout";
	public static final String LABEL_IS_HL_IN = "isHLin";
	public static final String LABEL_IS_IXPD_OUT = "isIXPDout";
	public static final String LABEL_IS_IXPD_IN = "isIXPDin";
	public static final String LABEL_IS_IXPD_PKG_OUT = "isIXPDPkgout";
	public static final String LABEL_IS_IXPD_PKG_IN = "isIXPDPkgin";
	public static final String IS_PART_OF_COMPONENT = "isPartOfComponent";

	public static final String DEFAULT_PACKAGE = "(default package)";

	//aliasas
	public static final String LBL_CLASS_DEP = PropertyEdge.LABEL_CLASS_DEPENDENCY.toString();
	public static final String LBL_PACK_DEP = GraphBuilder.LABEL_PACKAGE_AFFERENCE;


	private static final Logger logger = LogManager.getLogger(GraphBuilder.class);
	private ExTimeLogger exTimeLogger;

	private HashMap<String, String> _packages;
	private HashMap<String, JavaClass> _classes;
	private boolean _asTdEvolution = false; // Modded
	private List<Vertex> classesList; // Modded (reduce graph traversals)
	private List<Vertex> packagesList; // Modded (reduce graph traversals)
	private long intClassDependencyCount = 0; // Modded
	private long intPackageDependencyCount = 0; // Modded
	private long totalClassDependencyCount = 0; // Modded
	private long totalPackageDependencyCount = 0; // Modded

	private Set<String> extClasses;  // Modded
	private Set<String> extPackages;  // Modded

	private long extClassesCount;  // Modded
	private long totalClassesCount;  // Modded
	private long extPacksCount;  // Modded
	private long totalPacksCount;  // Modded


	public GraphBuilder(HashMap<String, JavaClass> classes, HashMap<String, String> packages) {
		_classes = classes;
		_packages = packages;
		classesList = new ArrayList<>();
		packagesList = new ArrayList<>();
	}

	public GraphBuilder(HashMap<String, JavaClass> classes, HashMap<String, String> packages,
						boolean asTdEvolution, ExTimeLogger exTimeLogger,
						Set<String> extClasses, Set<String> extPackages) {
		this(classes,packages);
		this._asTdEvolution = asTdEvolution; // Modded
		this.exTimeLogger = exTimeLogger;  // Modded
		this.extClasses = extClasses; // Modded
		this.extPackages = extPackages; // Modded
	}

	/**
	 * Creates a graph where a class is a node and a dependency is a edge.
	 *
	 * @throws ClassNotFoundException
	 */
	public void createGraph(Graph graph) {
		// initialization
		logger.debug("***Init***");

		logger.debug(1+" - "+_classes.values().size()+"***Calculate node classes and packages***");
		int i=1;
		for (JavaClass clazz : _classes.values()) {
			logger.debug((i)+" - "+_classes.values().size()+"***searching node***");
			exTimeLogger.logEventStart(Event.GB_SEARCH_NODE);
			Vertex vertex = GraphUtils.existNode(graph, clazz.getClassName(), CLASS);
			if (vertex == null) {
				vertex = createNode(graph, clazz.getClassName(), CLASS);
			}
			exTimeLogger.logEventEnd(Event.GB_SEARCH_NODE);
			logger.debug((i)+" - "+_classes.values().size()+"***super dependencies***");
			exTimeLogger.logEventStart(Event.GB_SUPER_DEP);
			createSuperDependency(graph, clazz, vertex);
			exTimeLogger.logEventEnd(Event.GB_SUPER_DEP);
			logger.debug((i)+" - "+_classes.values().size()+"***create interfacies***");
			exTimeLogger.logEventStart(Event.GB_INTERFACE_DEP);
			createInterfaceDependency(graph, clazz, vertex);
			exTimeLogger.logEventEnd(Event.GB_INTERFACE_DEP);
			logger.debug((i)+" - "+_classes.values().size()+"***method dependencies***");
			exTimeLogger.logEventStart(Event.GB_METHOD_DEP);
			createMethodDependency(graph, clazz, vertex);
			exTimeLogger.logEventEnd(Event.GB_METHOD_DEP);
			logger.debug((i)+" - "+_classes.values().size()+"***package depedencies***");
			exTimeLogger.logEventStart(Event.GB_PACKAGE_DEP);
			createPackageDependency(graph, clazz.getPackageName(), vertex);
			exTimeLogger.logEventEnd(Event.GB_PACKAGE_DEP);
			// createAnnotationDependency(graph, clazz, vertex);
			i++;
		}
		logger.debug("***Calculate afferent coupling btw packages***");
		exTimeLogger.logEventStart(Event.GB_CALC_AFFERENT_COUPLING);
		calculateAfferentCouplingBetweenPackages(graph);
		exTimeLogger.logEventEnd(Event.GB_CALC_AFFERENT_COUPLING);

	}

	/**
	 * Creates a dependency between a node class and a node superclass.
	 *
	 * @param graph
	 * @param clazz
	//* @param vertex
	 */
	private void createSuperDependency(Graph graph, JavaClass clazz, Vertex callerVertex) {
		if (!clazz.getSuperclassName().equals("java.lang.Object")) {
			Vertex superclassVertex = GraphUtils.existNode(graph, clazz.getSuperclassName(), CLASS);
			String superclassPackageName = GraphUtils.getPackageName(clazz.getSuperclassName());

			if (superclassVertex == null) {
				superclassVertex = createNode(graph, clazz.getSuperclassName(), CLASS);

				if (superclassVertex.property(PROPERTY_CLASS_TYPE).value().equals(RETRIEVED_CLASS)) {
					createPackageDependency(graph, superclassPackageName, superclassVertex);
				}
			}

			callerVertex.addEdge(PropertyEdge.LABEL_SUPER_DEPENDENCY.toString(), superclassVertex);
			if(!extClasses.contains(superclassVertex.value(GraphBuilder.PROPERTY_NAME).toString()))  //Modded
			{
				intClassDependencyCount++; //modded
			}
			totalClassDependencyCount++; //modded

			if (!clazz.getPackageName().equals(superclassPackageName) && superclassVertex != null) {
				createAfferentCoupling(graph, superclassPackageName, callerVertex);
				createEfferentCoupling(graph, clazz.getPackageName(), superclassVertex);
			}

		}
	}

	private void createInterfaceDependency(Graph graph, JavaClass clazz, Vertex callerVertex) {

		String[] interfaces = clazz.getInterfaceNames();

		for (String i : interfaces) {
			Vertex interfaceVertex = GraphUtils.existNode(graph, i, CLASS);
			String interfacePackageName = GraphUtils.getPackageName(i);

			if (interfaceVertex == null) {
				interfaceVertex = createNode(graph, i, CLASS);

				if (interfaceVertex.property(PROPERTY_CLASS_TYPE).value().toString().equals(RETRIEVED_CLASS)) {

					createPackageDependency(graph, interfacePackageName, interfaceVertex);
				}
			}

			callerVertex.addEdge(PropertyEdge.LABEL_INTERFACE_DEPENDENCY.toString(), interfaceVertex);

			if (!GraphUtils.getPackageName(clazz.getClassName()).equals(interfacePackageName) && interfaceVertex != null) {
				createAfferentCoupling(graph, interfacePackageName, callerVertex);
				createEfferentCoupling(graph, GraphUtils.getPackageName(clazz.getClassName()), interfaceVertex);
			}
		}
	}

	/**
	 * Creates a dependency between two class nodes when a class uses some
	 * external methods.
	 *
	 * @param graph
	 * @param clazz
	 * @param callerVertex
	 */
	private void createMethodDependency(Graph graph, JavaClass clazz, Vertex callerVertex) {
		List<String> dependences = computeDependences(clazz);
		for (String referencedclassName : dependences) {
			exTimeLogger.logEventStart(Event.GB_MTHD_PARSE_CLASS_NAME);
			String[] classInfo = parseClassName(referencedclassName);
			exTimeLogger.logEventEnd(Event.GB_MTHD_PARSE_CLASS_NAME);
			exTimeLogger.logEventStart(Event.GB_MTHD_ADD_NODE_TO_GRAPH);
			Vertex referencedclassVertex = GraphUtils.existNode(graph, classInfo[0], CLASS);
			if (referencedclassVertex == null && !classInfo[0].equals("java.lang.Object")) {
				referencedclassVertex = createNode(graph, classInfo[0], CLASS);

				if (referencedclassVertex.property(PROPERTY_CLASS_TYPE).value().equals(RETRIEVED_CLASS)) {
					createPackageDependency(graph, classInfo[1], referencedclassVertex);
				}
			}
			exTimeLogger.logEventEnd(Event.GB_MTHD_ADD_NODE_TO_GRAPH);

			// referencedclassVertex only stay null if classInfo[0] ==
			// "java.lang.Object"
			exTimeLogger.logEventStart(Event.GB_MTHD_UPDATE_EDGES);
			if (referencedclassVertex != null
					&& !classInfo[0].equals(callerVertex.property(PROPERTY_NAME).value().toString())) {
				Edge dependency = GraphUtils.existEdge(PropertyEdge.LABEL_CLASS_DEPENDENCY.toString(), callerVertex, referencedclassVertex, Direction.OUT);
				if (dependency == null) {
					callerVertex.addEdge(PropertyEdge.LABEL_CLASS_DEPENDENCY.toString(), referencedclassVertex, PROPERTY_DEPEDENCY_WEIGHT.toString(), DEFAULT_NUM_DEPENDENCES);
					if(!extClasses.contains(referencedclassVertex.value(GraphBuilder.PROPERTY_NAME).toString()))  //Modded
					{
						intClassDependencyCount++; //modded
					}
					totalClassDependencyCount++; //modded
				}
				else{
					int weight = dependency.value(PROPERTY_DEPEDENCY_WEIGHT);
					++weight;
					dependency.property(PROPERTY_DEPEDENCY_WEIGHT, weight);
				}
			}
			exTimeLogger.logEventEnd(Event.GB_MTHD_UPDATE_EDGES);
			exTimeLogger.logEventStart(Event.GB_MTHD_UPDATE_COUPLING);
			String clazzPackage = clazz.getPackageName();
			if ("".equals(clazzPackage)) {
				clazzPackage = DEFAULT_PACKAGE;
			}
			if (!clazzPackage.equals(classInfo[1]) && referencedclassVertex != null) {
				createAfferentCoupling(graph, classInfo[1], callerVertex);
				createEfferentCoupling(graph, clazzPackage, referencedclassVertex);
			}
			exTimeLogger.logEventEnd(Event.GB_MTHD_UPDATE_COUPLING);
		}
	}


	/**
	 * Creates a dependency between a class node and a package node when the
	 * class belongs to the package.
	 *
	 * @param graph
	 * @param packageName
	 * @param callerVertex
	 */
	void createPackageDependency(Graph graph, String packageName, Vertex callerVertex) {

		if (packageName != null) {
			if ("".equals(packageName)) {
				packageName = DEFAULT_PACKAGE;
			}
			Vertex packageVertex = GraphUtils.existNode(graph, packageName, PACKAGE);
			if (packageVertex == null) {
				packageVertex = createNode(graph, packageName, PACKAGE);
				packageVertex.property(PROPERTY_NUM_TOTAL_DEPENDENCIES, 0);
			}

			callerVertex.addEdge(PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString(), packageVertex);
		}
	}

	/**
	 * Creates a dependency between a class node and a package node when the
	 * class depends on a package's class
	 *
	 * @param graph
	 * @param packageName
	 * @param callerVertex
	 */
	protected void createAfferentCoupling(Graph graph, String packageName, Vertex callerVertex) {
		if (packageName != null) {
			Vertex packageVertex = GraphUtils.existNode(graph, packageName, PACKAGE);
			if (packageVertex == null) {
				packageVertex = createNode(graph, packageName, PACKAGE);
				packageVertex.property(PROPERTY_NUM_TOTAL_DEPENDENCIES, 0);
			}

			if (GraphUtils.existEdge(LABEL_AFFERENCE, callerVertex, packageVertex, Direction.OUT) == null) {
				callerVertex.addEdge(LABEL_AFFERENCE, packageVertex);
			}
		}
	}

	/**
	 * Creates a dependency between a class node and a package node when a
	 * package's class depends on the class
	 *
	 * @param graph
	 * @param packageName
	 * @param efferentVertex
	 */
	private void createEfferentCoupling(Graph graph, String packageName, Vertex efferentVertex) {
		if (packageName != null) {
			Vertex packageVertex = GraphUtils.existNode(graph, packageName, PACKAGE);

			if (packageVertex == null) {
				packageVertex = createNode(graph, packageName, PACKAGE);
				packageVertex.property(PROPERTY_NUM_TOTAL_DEPENDENCIES, 0);
			}

			if (GraphUtils.existEdge(PropertyEdge.LABEL_EFFERENCE.toString(), efferentVertex, packageVertex, Direction.OUT) == null) {
				efferentVertex.addEdge(PropertyEdge.LABEL_EFFERENCE.toString(), packageVertex);
			}

		}
	}

	/**
	 * Creates a dependency between two package vertex when one is afferent of
	 * the other
	 *
	 * @param graph
	 */
	void calculateAfferentCouplingBetweenPackages(Graph graph) {
		List<Edge> affEdges = GraphUtils.findEdgesByLabel(graph, GraphBuilder.LABEL_AFFERENCE);
		if(affEdges!=null){
			for (Edge e : affEdges) {
				Edge belongToEdge = graph.traversal().V(e.outVertex().id()).bothE(PropertyEdge.LABEL_PACKAGE_DEPENDENCY.toString())
						.next();
				if (GraphUtils.existEdge(LABEL_PACKAGE_AFFERENCE, belongToEdge.inVertex(), e.inVertex(), Direction.OUT) == null) {
					belongToEdge.inVertex().addEdge(LABEL_PACKAGE_AFFERENCE, e.inVertex());
					if(!extPackages.contains(e.inVertex().value(GraphBuilder.PROPERTY_NAME).toString()))  //Modded
					{
						intPackageDependencyCount++; //modded
					}
					totalPackageDependencyCount++; //modded

					Integer numTotalDep = belongToEdge.inVertex().value(PROPERTY_NUM_TOTAL_DEPENDENCIES);
					numTotalDep += 1;
					belongToEdge.inVertex().property(PROPERTY_NUM_TOTAL_DEPENDENCIES, numTotalDep);
				}
			}
		}
	}

	/**
	 * Compute the list of the class referenced by javaClass.
	 *
	 * @param javaClass
	 * @return List of referenced classes names
	 */
	private List<String> computeDependences(JavaClass javaClass) {
		exTimeLogger.logEventStart(Event.GB_MTHD_INIT_DEPS);
		List<String> dependences = new ArrayList<>();
		ConstantPool cp = javaClass.getConstantPool();
		ConstantPoolGen cpg = new ConstantPoolGen(cp);
		exTimeLogger.logEventEnd(Event.GB_MTHD_INIT_DEPS);
		for (Method m : javaClass.getMethods()) {
			exTimeLogger.logEventStart(Event.GB_MTHD_GET_METHOD_GEN);
			String className = javaClass.getClassName();
			MethodGen mg = new MethodGen(m, className, cpg); //TODO seems intense
			exTimeLogger.logEventEnd(Event.GB_MTHD_GET_METHOD_GEN);
			exTimeLogger.logEventStart(Event.GB_MTHD_COMPUTE_INSTRUCTIONS);
			InstructionList instructions = mg.getInstructionList();
			if (instructions != null) {
				InstructionHandle[] ihs = instructions.getInstructionHandles();
				for (int i = 0; i < ihs.length; i++) {
					InstructionHandle ih = ihs[i];
					Instruction instruction = ih.getInstruction();
					dependences = InstructionIdentifier.identify(dependences, cpg, instruction); //TODO seems intense
				}
			}
			exTimeLogger.logEventEnd(Event.GB_MTHD_COMPUTE_INSTRUCTIONS);
			exTimeLogger.logEventStart(Event.GB_MTHD_COMPUTE_ANNOTATIONS);
			//FIXME
			for (AnnotationEntry entry : m.getAnnotationEntries()) {
				dependences.add(entry.getAnnotationType());
			}

			for (ParameterAnnotationEntry entry2 : m.getParameterAnnotationEntries()) {
				for (AnnotationEntry entry : entry2.getAnnotationEntries()) {
					dependences.add(entry.getAnnotationType());
				}
			}
			exTimeLogger.logEventEnd(Event.GB_MTHD_COMPUTE_ANNOTATIONS);
		}

		/*  for(String s : dependences){
            logger.debug("nome classe: " + javaClass.getClassName() + " referencedClassName: " + s);
        }*/

		//        dependences.addAll(InstructionIdentifier.findAnnotations(javaClass));
		InstructionIdentifier.findAnnotations(javaClass,dependences);
		return dependences;
	}



	/**
	 * Create a new node and return it.
	 *
	 * @param graph
	 * @param nodeType
	//* @param clazz
	 * @return the new node
	 */

	Vertex createNode(Graph graph, String name, String nodeType) {

		String classType = RETRIEVED_CLASS;
		String packageType = RETRIEVED_PACKAGE;
		String classModifier = NO_MODIFIER;
		if (CLASS.equals(nodeType)) {
			//			for (JavaClass clazz : classes) {
			//				if (name.equals(clazz.getClassName())) {
			//					classType = SYSTEM_CLASS;
			//					if (clazz.isAbstract()) {
			//						classModifier = ABSTRACT_CLASS;
			//					}
			//					if (clazz.isInterface()) {
			//						classModifier = INTERFACE;
			//					}
			//				}
			//			}
			JavaClass f = _classes.get(name);
			if(f!=null){
				classType = SYSTEM_CLASS;
				if (f.isAbstract()) {
					classModifier = ABSTRACT_CLASS;
				}
				if (f.isInterface()) {
					classModifier = INTERFACE;
				}
			}
			Vertex vertex = graph.addVertex(T.label, nodeType, PROPERTY_NAME, name, PROPERTY_CLASS_TYPE, classType,
					PROPERTY_CLASS_MODIFIER, classModifier);
			classesList.add(vertex);
			if(extClasses.contains(name))
			{
				extClassesCount++;
			}
			totalClassesCount++;
			return vertex;
		} else {
			//			for (String pkg : packages) {
			//				if (pkg.equals(name)) {
			//					packageType = SYSTEM_PACKAGE;
			//				}
			//			}
			if (_packages.get(name)!=null) {
				packageType = SYSTEM_PACKAGE;
			}
			Vertex vertex = graph.addVertex(T.label, nodeType, PROPERTY_NAME, name, PROPERTY_PACKAGE_TYPE, packageType);
			// Modded
			packagesList.add(vertex);
			if(extPackages.contains(name))
			{
				extPacksCount++;
			}
			totalPacksCount++;
			return vertex;
		}

	}

	/**
	 * Parse a class signature.
	 *
	 * @param fullClassName
	 * @return an array containing the name of the class and its package.
	 */
	private String[] parseClassName(String fullClassName) {
		String[] classInfo = new String[2];
		String className = "";
		String packageName = "";
		if (fullClassName.contains("/")) {
			int slashIndex = fullClassName.lastIndexOf('/');
			// logger.debug("nome classe " + fullClassName);
			packageName = "";
			if (slashIndex >= 0) {
				packageName = fullClassName.substring(1, slashIndex);
				packageName = packageName.replace('/', '.');
			} else {
				packageName = DEFAULT_PACKAGE;
			}

			className = fullClassName.substring(1, fullClassName.length() - 1);
			className = className.replace('/', '.');

		} else {
			packageName = GraphUtils.getPackageName(fullClassName);
			className = fullClassName;
		}
		classInfo[0] = className;
		classInfo[1] = packageName;

		return classInfo;

	}

	// Modded
	public static boolean isClassLevel(String depLabel)
	{
		return depLabel.equals(PropertyEdge.LABEL_CLASS_DEPENDENCY.toString());
	}

	// Modded
	public List<Vertex> getClasses()
	{
		return classesList;
	}

	// Modded
	public List<Vertex> getPackages()
	{
		return packagesList;
	}

	// Modded
	public long getIntClassDependencyCount()
	{
		return intClassDependencyCount;
	}

	// Modded
	public long getIntPackageDependencyCount()
	{
		return intPackageDependencyCount;
	}

	// Modded
	public long getTotalClassDependencyCount()
	{
		return totalClassDependencyCount;
	}

	// Modded
	public long getTotalPackageDependencyCount()
	{
		return totalPackageDependencyCount;
	}

	// Modded
	public long getExtClassesCount()
	{
		return extClassesCount;
	}

	// Modded
	public long getTotalClassesCount()
	{
		return totalClassesCount;
	}

	// Modded
	public long getExtPacksCount()
	{
		return extPacksCount;
	}

	// Modded
	public long getTotalPacksCount()
	{
		return totalPacksCount;
	}
}
