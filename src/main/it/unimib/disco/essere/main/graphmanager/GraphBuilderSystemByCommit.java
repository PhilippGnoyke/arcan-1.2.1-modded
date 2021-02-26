package it.unimib.disco.essere.main.graphmanager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitblit.models.PathModel.PathChangeModel;
import com.gitblit.utils.JGitUtils;

public class GraphBuilderSystemByCommit {
	
	private static final Logger logger = LogManager.getLogger(GraphBuilderSystemByCommit.class);

	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_COMMIT = "propertyCommit";
	public static final String PROPERTY_COUNTER = "counter";

	public static final String PACKAGE_DEPENDENCY_LABEL = "belongsTo";
	public static final String MODIFIED_LABEL = "hasModified";
	public static final String SUCCESSOR_LABEL = "hasSuccessor";
	public static final String STARTING_COMMIT = "startingCommit";
	public static final String COUPLED_MODIFIED_LABEL = "hasCoupledModified";

	public static final String PACKAGE = "package";
	//	public static final String CLASS = "class";
	public static final String JAVAFILE = "java";
	public static final String COMMIT = "commit";

	
	public static final String DELETION = "deletion";
	public static final String INSERTION = "insertion";
	public static final String DATE = "date";
	private Set<PathChangeModel> _javas;

	
	
	public GraphBuilderSystemByCommit(final Set<PathChangeModel> javas) {
		_javas = javas;
	}
	/**
	 * Creates a graph where a class is a node and a dependency is a edge.
	 * 
	 * @throws ClassNotFoundException
	 */
	public void createGraph(final Graph graph, final RevCommit commitBefore, final RevCommit commitNow) {
		Vertex vertexCommit = GraphUtils.existNode(graph, commitNow.getId().getName(), COMMIT);
		if(commitBefore!=null){
			if (vertexCommit == null) {
				vertexCommit = createNode(graph, commitNow.getId().getName(), COMMIT);
				vertexCommit.property(DATE, JGitUtils.getCommitDate(commitNow).getTime());
			}
			Vertex vertexCommitBefore = GraphUtils.existNode(graph, commitBefore.getId().getName(), COMMIT);
			if (vertexCommitBefore==null) {
				vertexCommitBefore = createNode(graph, commitBefore.getId().getName(), COMMIT);
			} 
			if(GraphUtils.notAlreadyExistEdge(SUCCESSOR_LABEL, vertexCommitBefore, vertexCommit)){
				vertexCommitBefore.addEdge(SUCCESSOR_LABEL, vertexCommit);
				vertexCommitBefore.property(DATE, JGitUtils.getCommitDate(commitBefore).getTime());
			}
		}else{
			if (vertexCommit == null) {
				vertexCommit = createNode(graph, commitNow.getId().getName(), STARTING_COMMIT);
				vertexCommit.property(DATE, JGitUtils.getCommitDate(commitNow).getTime());
			}
		}

		Set<Vertex> vertexClassBeforeSet = new HashSet<Vertex>();
		for (PathChangeModel j : _javas) {
			String[] d = parseClassName(j.name);

			Vertex vertexClass = GraphUtils.existNode(graph, d[0], JAVAFILE);
			if (vertexClass == null) {
				vertexClass = createNode(graph, d[0], JAVAFILE);
			} 

			Iterator<Vertex> vcbi = vertexClassBeforeSet.iterator();
			while(vcbi.hasNext()){
				Vertex vertexBefore = vcbi.next();
				Iterator<Edge> edge = vertexClass.edges(Direction.BOTH, COUPLED_MODIFIED_LABEL);
				if(!edge.hasNext()){
					vertexClass.addEdge(COUPLED_MODIFIED_LABEL, vertexBefore, PROPERTY_COUNTER, 1);
				}
				while(edge.hasNext()){
					Edge s = edge.next();
					if(s.inVertex().equals(vertexBefore)||s.outVertex().equals(vertexBefore)){
						int i = s.value(PROPERTY_COUNTER);
						i++;
						s.property(PROPERTY_COUNTER, i);
					}
				}
			}

			Vertex vertexPackage = GraphUtils.existNode(graph, d[1], PACKAGE);
			if (vertexPackage == null) {
				vertexPackage = createNode(graph, d[1], PACKAGE);
			}

			if(GraphUtils.notAlreadyExistEdge(PACKAGE_DEPENDENCY_LABEL, vertexClass, vertexPackage)){
				vertexClass.addEdge(PACKAGE_DEPENDENCY_LABEL, vertexPackage);
			}
			if(GraphUtils.notAlreadyExistEdge(MODIFIED_LABEL, vertexCommit, vertexClass)){
				vertexCommit.addEdge(MODIFIED_LABEL, vertexClass, INSERTION, j.insertions , DELETION, j.deletions);
			}
			vertexClassBeforeSet.add(vertexClass);
		}
	}




	/**
	 * Create a new node and return it. Probably should be added more info for storage or removed if.
	 * 
	 * @param graph
	 * @param nodeType
	 * @param clazz
	 * @return the new node
	 */
	private Vertex createNode(final Graph graph, final String name, final String nodeType) {
		if (STARTING_COMMIT.equals(nodeType)) {
			Vertex vertex = graph.addVertex(T.label, COMMIT, PROPERTY_NAME, name, PROPERTY_COMMIT, STARTING_COMMIT);
			return vertex;
		} else {
			Vertex vertex = graph.addVertex(T.label, nodeType, PROPERTY_NAME, name);
			return vertex;
		}
	}

	/**
	 * Parse a class signature.
	 * 
	 * @param fullClassName
	 * @return an array containing the name of the class and its package.
	 */
	private String[] parseClassName(final String fullClassName) {
		String[] classInfo = new String[2];

		int slashIndex = fullClassName.lastIndexOf('/');
		String packageName = fullClassName.substring(0, slashIndex);
		packageName = packageName.replace('/', '.');

		String className = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
		className = className.replace('/', '.');

		classInfo[0] = className;
		classInfo[1] = packageName;

		return classInfo;

	}

}
