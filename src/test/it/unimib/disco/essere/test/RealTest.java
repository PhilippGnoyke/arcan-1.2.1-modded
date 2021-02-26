package it.unimib.disco.essere.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;

import it.unimib.disco.essere.analysis.file.util.DirUtils;

public class RealTest {
	private static List<JavaClass> classes = new ArrayList<>();
	private static List<String> packages = new ArrayList<>();
	private static Path systemClassPath;

	public static void main(String[] args) {
		// C:\Users\ricca\Downloads\quartz-1.8.6-all-bin\
		Path binpath = Paths.get("C:", "Users", "ricca", "Downloads", "quartz-1.8.6-all-bin", "org");
		// Path binpath =
		// Paths.get("C:","Users","ricca","Downloads","quartz-1.8.6-all-bin","org","quartz");
//		Path binpathcore = Paths.get("C:", "Users", "ricca", "Downloads", "quartz-1.8.6-all-bin", "org", "quartz",
//				"core");
		systemClassPath = binpath;
//		ClassPath classpath = new ClassPath(binpath.toString() + File.pathSeparator + ".");
//		ClassPath classpathcore = new ClassPath(binpathcore.toString());
		// SyntheticRepository.getInstance(classpath);
		//		Repository.setRepository(SyntheticRepository.getInstance(classpath));
		//		Repository.setRepository(SyntheticRepository.getInstance(classpathcore));
		try {
			readClass(binpath);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path path = Paths.get("C:", "Users", "ricca", "Documents", "Neo4j", "default.graphdb");
		BaseConfiguration conf = new BaseConfiguration();
		conf.setProperty("gremlin.neo4j.directory", path.toString());
		conf.setProperty("gremlin.neo4j.multiProperties", true);
		conf.setProperty("gremlin.neo4j.metaProperties", true);

		try {
			DirUtils.cleanIfExists(path);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try {
			createGraph(conf, classes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Reads the .class files from the indicated bin folder.
	 */
	public static void readClass(Path path) throws ClassNotFoundException, IOException {

		Stream<Path> stream = Files.walk(path);

		stream.forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				try {
					System.out.println("file " + filePath);
					if ("class".equals(FilenameUtils.getExtension(filePath.toString()))) {
						// JavaClass clazz =
						// Repository.lookupClass(FilenameUtils.removeExtension(filePath.toFile().getName()));
						ClassParser cp = null;
						FileInputStream is = null;
						try {
							is = new FileInputStream(filePath.toFile());
							// System.out.println("creato input stream");
							cp = new ClassParser(is, filePath.getFileName().toString());
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (cp != null) {
							try {
								JavaClass clazz = cp.parse();
								clazz.setFileName(filePath.toFile().getAbsolutePath());
								Repository.addClass(clazz);
								classes.add(clazz);
								packages.add(clazz.getPackageName());
								System.out.println("Class name: " + clazz.getClassName() + ", Superclass name: " + clazz.getSuperclassName());

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		stream.close();

	}

	/**
	 * Creates a graph where a class is a node and a dependency is a edge.
	 * 
	 * @param classes
	 * @throws ClassNotFoundException
	 * @throws Exception
	 */
	private static void createGraph(BaseConfiguration conf, List<JavaClass> classes) throws ClassNotFoundException {
		// initialization
		//
		// BaseConfiguration conf = new BaseConfiguration();
		// conf.setProperty("gremlin.neo4j.directory", Paths.get("C:", "Users",
		// "Ilaria", "Desktop", "myprog", "LabIngSoft", "neo4jDB").toString());
		// conf.setProperty("gremlin.neo4j.multiProperties", true);
		// conf.setProperty("gremlin.neo4j.metaProperties", true);
		// Path path = Paths.get("C:", "Users", "Ilaria", "Desktop", "myprog",
		// "LabIngSoft", "neo4jDB");
		// try {
		// DirUtils.clean(path);
		// } catch (IOException e2) {
		// // TODO Auto-generated catch block
		// e2.printStackTrace();
		// }
		Neo4jGraph graph = Neo4jGraph.open(conf);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int[] ids = new int[2];
		ids[0] = 0;
		ids[1] = 0;

		Vertex originVertex = createNode(graph, ids, classes.get(0).getClassName(), "class");
		++ids[0];
		try {
			ids = createSuperDependency(graph, ids, classes.get(0), originVertex);
			ids = createMethodDependency(graph, ids, classes.get(0), originVertex);
			ids = createPackageDependency(graph, ids, classes.get(0), originVertex);
			classes.remove(0);
			// end initialization
			for (JavaClass clazz : classes) {
				System.out.println(
						"non esiste già il vertice: " + notAlreadyExistNode(graph, clazz.getClassName(), ids, "class"));
				if (notAlreadyExistNode(graph, clazz.getClassName(), ids, "class")) {
					Vertex vertex = createNode(graph, ids, clazz.getClassName(), "class");
					++ids[0];
					ids = createSuperDependency(graph, ids, clazz, vertex);
					ids = createMethodDependency(graph, ids, clazz, vertex);
					ids = createPackageDependency(graph, ids, clazz, vertex);
				}
			}

			OutputStream out = new FileOutputStream("RealSystem-graph.graphml");

			graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(out, graph);
			;
		} catch (java.lang.UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				graph.tx().commit();
				graph.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a dependency between a node class and a node superclass.
	 * 
	 * @param graph
	 * @param ids
	 * @param clazz
	 * @param vertex
	 * @return array of index
	 * @throws ClassNotFoundException
	 */
	private static int[] createSuperDependency(Graph graph, int[] ids, JavaClass clazz, Vertex callerVertex)
			throws ClassNotFoundException {
		if (!clazz.getSuperclassName().equals("java.lang.Object")) {
			if (notAlreadyExistNode(graph, clazz.getSuperclassName(), ids, "class")) {
				Vertex superclassVertex = createNode(graph, ids, clazz.getSuperclassName(), "class");
				++ids[0];
				callerVertex.addEdge("isChildOf", superclassVertex);
				++ids[1];
				try{
					JavaClass superclazz = Repository.lookupClass(clazz.getSuperclassName());
					ids = createPackageDependency(graph, ids, superclazz, superclassVertex);

					System.out.println(
							"Creo l'edge tra " + callerVertex.property("name").value() + " e " + clazz.getSuperclassName());
				}catch(ClassNotFoundException e){
					e.printStackTrace();
					System.out.println("Class not founded and package not addded "+clazz.getSuperclassName());
				}catch(ClassFormatException e){
					e.printStackTrace();
					System.out.println("Class not founded and package not addded "+clazz.getSuperclassName());
				}
			} else {
				int counter = ids[0];
				while (counter >= 0) {
					Object c = (Object) counter;
					if (graph.vertices(c).hasNext()) {
						Vertex v = graph.vertices(c).next();
						if (v.property("name").value().equals(clazz.getSuperclassName())) {
							callerVertex.addEdge("isChildOf", v);
							++ids[1];
							System.out.println("Creo l'edge tra " + callerVertex.property("name").value() + " e "
									+ clazz.getSuperclassName());
						}
					}
					--counter;
				}
			}

		}
		return ids;
	}

	/**
	 * Creates a dependency between two class nodes when a class uses some
	 * external methods.
	 * 
	 * @param graph
	 * @param ids
	 * @param clazz
	 * @param callerVertex
	 * @return array of index
	 * @throws ClassNotFoundException
	 */
	private static int[] createMethodDependency(Graph graph, int[] ids, JavaClass clazz, Vertex callerVertex)
			throws ClassNotFoundException {
		List<String> dependences = computeDependences(clazz);
		for (String referencedclassName : dependences) {
			System.out.println("CLASSE CHIAMATA: " + referencedclassName);
			if (notAlreadyExistNode(graph, referencedclassName, ids, "class")) {
				Vertex referencedclassVertex = createNode(graph, ids, referencedclassName, "class");
				++ids[0];
				callerVertex.addEdge("dependsOn", referencedclassVertex);
				++ids[1];
				try{
					JavaClass calleeclazz = Repository.lookupClass(referencedclassName);
					ids = createPackageDependency(graph, ids, calleeclazz, referencedclassVertex);
					System.out.println("Creo l'edge di dipendenza tra " + callerVertex.property("name").value() + " e "
							+ referencedclassName);
				}catch(ClassNotFoundException e){
					e.printStackTrace();
					System.out.println("Class not founded and package not addded "+referencedclassName);
				}catch(ClassFormatException e){
					e.printStackTrace();
					System.out.println("Class not founded and package not addded "+referencedclassName);
				}
			} else {
				int counter = ids[0];
				while (counter >= 0) {
					Object c = (Object) counter;
					if (graph.vertices(c).hasNext()) {
						Vertex v = graph.vertices(c).next();
						if (v.property("name").value().equals(referencedclassName)) {
							callerVertex.addEdge("dependsOn", v);
							++ids[1];
							System.out.println("Creo l'edge di dipendenza tra " + callerVertex.property("name").value()
									+ " e " + referencedclassName);
						}
					}
					--counter;
				}
			}
		}
		return ids;
	}

	private static int[] createPackageDependency(Graph graph, int[] ids, JavaClass clazz, Vertex callerVertex)
			throws ClassNotFoundException {
		if (clazz.getPackageName() != null) {
			if (notAlreadyExistNode(graph, clazz.getPackageName(), ids, "package")) {
				Vertex packageVertex = createNode(graph, ids, clazz.getPackageName(), "package");
				++ids[0];
				callerVertex.addEdge("belongsTo", packageVertex);
				++ids[1];
				System.out.println(
						"Creo l'edge tra " + callerVertex.property("name").value() + " e " + clazz.getPackageName());
			} else {
				int counter = ids[0];
				while (counter >= 0) {
					Object c = (Object) counter;
					if (graph.vertices(c).hasNext()) {
						Vertex v = graph.vertices(c).next();
						if (v.property("name").value().equals(clazz.getPackageName())) {
							callerVertex.addEdge("belongsTo", v);
							++ids[1];
							System.out.println("Creo l'edge tra " + callerVertex.property("name").value() + " e "
									+ clazz.getPackageName());
						}
					}
					--counter;
				}
			}
		}
		return ids;
	}

	/**
	 * Compute the list of the class referenced by javaClass.
	 * 
	 * @param javaClass
	 * @return List of referenced classes names
	 */
	private static List<String> computeDependences(JavaClass javaClass) throws ClassNotFoundException {
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
					if (instruction instanceof InvokeInstruction) {
						InvokeInstruction ii = (InvokeInstruction) instruction;
						ReferenceType referenceType = ii.getReferenceType(cpg);

						if (referenceType instanceof ObjectType) {
							ObjectType objectType = (ObjectType) referenceType;
							String referencedClassName = objectType.getClassName();
							dependences.add(referencedClassName);
						}
					}
				}
			}
		}
		return dependences;
	}

	/**
	 * Create a new node and return it.
	 * 
	 * @param graph
	 * @param ids
	 * @param nodeType
	 *            TODO
	 * @param clazz
	 * @return the new node
	 */
	private static Vertex createNode(Graph graph, int[] ids, String name, String nodeType)
			throws ClassNotFoundException {
		String classType = "RetrievedClass";
		String packageType = "RetrievedPackage";
		for (JavaClass clazz : classes) {
			if (name.equals(clazz.getClassName())) {
				classType = "SystemClass";
			}
		}
		for (String pkg : packages) {
			if (pkg.equals(name)) {
				packageType = "SystemPackage";
			}
		}

		if ("class".equals(nodeType)) {
			Vertex vertex = graph.addVertex(T.label, nodeType, "name", name, "ClassType", classType);
			System.out.println("Creo il nodo " + name);
			return vertex;
		} else {
			Vertex vertex = graph.addVertex(T.label, nodeType, "name", name, "PackageType", packageType);
			System.out.println("Creo il nodo " + name);
			return vertex;
		}

	}

	/**
	 * Return true if the class to insert already has its own node in the graph.
	 * 
	 * @param graph
	 * @param clazzName
	 * @param ids
	 * @param nodeType
	 *            TODO
	 * @return true if the node does not exist yet.
	 */
	private static boolean notAlreadyExistNode(Graph graph, String clazzName, int[] ids, String nodeType) {
		System.out.println("sono nel metodo notAlreadyExistNode");

		Iterator<Vertex> i = graph.vertices();
		while (i.hasNext()) {
			Vertex v = i.next();
			System.out.println("controllo nome" + v.property("name").value() + " equals? " + clazzName + " "
					+ v.property("name").value().equals(clazzName));
			System.out.println("NODE TYPEEEEEEEEEEEEEE: " + v.label());
			if (v.label().equals(nodeType) && v.property("name").value().equals(clazzName)) {
				return false;
			}
		}
		return true;
	}

}
