package it.unimib.disco.essere.main.graphmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	private static final Logger logger = LogManager.getLogger(GraphBuilder.class);

	private HashMap<String, String> _packages;
	private HashMap<String, JavaClass> _classes;
	private boolean _asTdEvolution = false; // Modded
	private List<Vertex> classesList; // Modded (reduce graph traversals)
	private List<Vertex> packagesList; // Modded (reduce graph traversals)


	public GraphBuilder(HashMap<String, JavaClass> classes, HashMap<String, String> packages) {
		_classes = classes;
		_packages = packages;
		classesList = new ArrayList<>();
		packagesList = new ArrayList<>();
	}

	public GraphBuilder(HashMap<String, JavaClass> classes, HashMap<String, String> packages, boolean asTdEvolution) {
		this(classes,packages);
		this._asTdEvolution = asTdEvolution; // Modded
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
			Vertex vertex = GraphUtils.existNode(graph, clazz.getClassName(), CLASS);
			if (vertex == null) {
				vertex = createNode(graph, clazz.getClassName(), CLASS);
			}
			logger.debug((i)+" - "+_classes.values().size()+"***super dependencies***");
			createSuperDependency(graph, clazz, vertex);
			logger.debug((i)+" - "+_classes.values().size()+"***create interfacies***");
			createInterfaceDependency(graph, clazz, vertex);
			logger.debug((i)+" - "+_classes.values().size()+"***method dependencies***");
			createMethodDependency(graph, clazz, vertex);
			logger.debug((i)+" - "+_classes.values().size()+"***package depedencies***");
			createPackageDependency(graph, clazz.getPackageName(), vertex);
			// createAnnotationDependency(graph, clazz, vertex);
			i++;
		}
		logger.debug("***Calculate afferent coupling btw packages***");
		calculateAfferentCouplingBetweenPackages(graph);

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
			String[] classInfo = parseClassName(referencedclassName);
			Vertex referencedclassVertex = GraphUtils.existNode(graph, classInfo[0], CLASS);
			if (referencedclassVertex == null && !classInfo[0].equals("java.lang.Object")) {
				referencedclassVertex = createNode(graph, classInfo[0], CLASS);

				if (referencedclassVertex.property(PROPERTY_CLASS_TYPE).value().equals(RETRIEVED_CLASS)) {
					createPackageDependency(graph, classInfo[1], referencedclassVertex);
				}
			}
			// referencedclassVertex only stay null if classInfo[0] ==
			// "java.lang.Object"
			if (referencedclassVertex != null
					&& !classInfo[0].equals(callerVertex.property(PROPERTY_NAME).value().toString())) {
				Edge dependency = GraphUtils.existEdge(PropertyEdge.LABEL_CLASS_DEPENDENCY.toString(), callerVertex, referencedclassVertex, Direction.OUT);
				if (dependency == null) {
					callerVertex.addEdge(PropertyEdge.LABEL_CLASS_DEPENDENCY.toString(), referencedclassVertex, PROPERTY_DEPEDENCY_WEIGHT.toString(), DEFAULT_NUM_DEPENDENCES);
				}
				else{
					int weight = dependency.value(PROPERTY_DEPEDENCY_WEIGHT);
					++weight;
					dependency.property(PROPERTY_DEPEDENCY_WEIGHT, weight);
				}
			}
			String clazzPackage = clazz.getPackageName();
			if ("".equals(clazzPackage)) {
				clazzPackage = DEFAULT_PACKAGE;
			}
			if (!clazzPackage.equals(classInfo[1]) && referencedclassVertex != null) {
				createAfferentCoupling(graph, classInfo[1], callerVertex);
				createEfferentCoupling(graph, clazzPackage, referencedclassVertex);
			}
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
		List<String> dependences = new ArrayList<>();
		ConstantPool cp = javaClass.getConstantPool();
		ConstantPoolGen cpg = new ConstantPoolGen(cp);
		for (Method m : javaClass.getMethods()) {
			String className = javaClass.getClassName();
			MethodGen mg = new MethodGen(m, className, cpg);
			InstructionList instructions = mg.getInstructionList();
			if (instructions != null) {
				InstructionHandle[] ihs = instructions.getInstructionHandles();
				for (int i = 0; i < ihs.length; i++) {
					InstructionHandle ih = ihs[i];
					Instruction instruction = ih.getInstruction();
					dependences = InstructionIdentifier.identify(dependences, cpg, instruction);
				}
			}
			//FIXME
			for (AnnotationEntry entry : m.getAnnotationEntries()) {
				dependences.add(entry.getAnnotationType());
			}

			for (ParameterAnnotationEntry entry2 : m.getParameterAnnotationEntries()) {
				for (AnnotationEntry entry : entry2.getAnnotationEntries()) {
					dependences.add(entry.getAnnotationType());
				}
			}
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
	public List<Vertex> getClasses()
	{
		return classesList;
	}

	// Modded
	public List<Vertex> getPackages()
	{
		return packagesList;
	}
}
