package it.unimib.disco.essere.main.metricsengine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.google.common.collect.ImmutableList;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.PropertyEdge;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;

public class ClassMetricsCalculator {
	private static final Logger logger = LogManager.getLogger(ClassMetricsCalculator.class);
	public Graph graph;

	public ClassMetricsCalculator(Graph graph) {
		this.graph = graph;
	}

	/**
	 * @param classVertex
	 * @return the number of outgoing dependences of classVertex.
	 * @throws TypeVertexException
	 */

	public int calculateFanOut(final Vertex classVertex) throws TypeVertexException {
		List<Edge> dependencesEdges = calculateClassEdges(classVertex, GraphBuilder.LBL_CLASS_DEP,
				Direction.OUT);
		if(dependencesEdges == null){
			return 0;
		}
		return dependencesEdges.size();
	}

	public int calculateFanOut(String classVertex) throws TypeVertexException {
		Vertex clazz = GraphUtils.findVertex(graph, classVertex,GraphBuilder.CLASS);
		int fanout = calculateFanOut(clazz);
		clazz.property(GraphBuilder.PROPERTY_FANOUT, fanout);
		return fanout;
	}

	/**
	 * @param classVertex
	 * @return the number of ingoing dependences of classVertex.
	 * @throws TypeVertexException
	 */
	public int calculateFanIn(final Vertex classVertex) throws TypeVertexException {
		List<Edge> dependencesEdges = calculateClassEdges(classVertex, GraphBuilder.LBL_CLASS_DEP,
				Direction.IN);
		if(dependencesEdges == null){
			return 0;
		}
		return dependencesEdges.size();
	}

	public int calculateFanIn(String classVertex) throws TypeVertexException {
		Vertex clazz = GraphUtils.findVertex(graph, classVertex,GraphBuilder.CLASS);
		int fanin = calculateFanIn(clazz);
		clazz.property(GraphBuilder.PROPERTY_FANIN, fanin);
		return fanin;
	}

	// TODO usa set
	/**
	 * Calculates the number of classes to which a class is coupled according to
	 * Chidamber & Kemerer metric. Multiple accesses to the same class are
	 * counted as one access.
	 * 
	 * @param classVertex
	 * @return the number of classes to which classVertex is coupled.
	 * @throws TypeVertexException
	 */
	public int calculateCBO(final Vertex classVertex) throws TypeVertexException {
		List<Edge> dependecesEdges = calculateClassEdges(classVertex, GraphBuilder.LBL_CLASS_DEP,
				Direction.OUT);

		Set<Vertex> vcs = new HashSet<Vertex>();
		for (Edge e : dependecesEdges) {
			vcs.add(e.inVertex());
		}
		return vcs.size();
	}

	public int calculateCBO(String classVertex) throws TypeVertexException {
		Vertex clazz = GraphUtils.findVertex(graph, classVertex,GraphBuilder.CLASS);
		int cbo = calculateCBO(clazz);
		clazz.property(GraphBuilder.PROPERTY_CBO, cbo);
		return cbo;
	}

	// TODO refactoring
	/**
	 * Calculates the lack of cohesion in method according to the
	 * Henderson-Sellers metric.
	 * 
	 * @param clazz
	 * @return the lack of internal cohesion of clazz or -1 if there are no
	 *         methods/fields in the class.
	 */
	public double calculateLCOM(final JavaClass clazz) {
		Field[] fields = clazz.getFields();
		Method[] methods = clazz.getMethods();

		if (fields.length == 0 || methods.length == 0) {
			double lcom = 0;
			Vertex v = GraphUtils.findVertex(graph, clazz.getClassName(),GraphBuilder.CLASS);
			v.property(GraphBuilder.PROPERTY_LCOM, lcom);
			return lcom;
		} else {
			Map<String, Integer[]> fieldsMap = new HashMap<>();

			for (Field f : fields) {
				fieldsMap.put(f.getName(), new Integer[methods.length]);
			}

			ConstantPool cp = clazz.getConstantPool();
			ConstantPoolGen cpg = new ConstantPoolGen(cp);
			int index = 0;
			for (Method m : methods) {
				MethodGen mg = new MethodGen(m, clazz.getClassName(), cpg);
				InstructionList instructions = mg.getInstructionList();
				if (instructions != null) {
					InstructionHandle[] ihs = instructions.getInstructionHandles();
					for (int i = 0; i < ihs.length; i++) {
						InstructionHandle ih = ihs[i];
						Instruction instruction = ih.getInstruction();
						if (instruction instanceof FieldInstruction) {
							FieldInstruction fi = (FieldInstruction) instruction;
							String name = fi.getFieldName(cpg);
							if (fieldsMap.containsKey(name) && fieldsMap.get(name)[index] == null) {
								fieldsMap.get(name)[index] = 1;
							}
						}
					}
				}
				++index;
			}

			double sum = 0;
			for (Entry<String, Integer[]> entry : fieldsMap.entrySet()) {
				for (Integer occurrence : entry.getValue()) {
					if (occurrence != null) {
						++sum;
					}
				}
			}

			double mean = sum / fields.length;

			if (clazz.getMethods().length != 1 && mean - clazz.getMethods().length != 0) {
				double lcom = (mean - clazz.getMethods().length) / (1 - clazz.getMethods().length);
				Vertex v = GraphUtils.findVertex(graph, clazz.getClassName(),GraphBuilder.CLASS);
				v.property(GraphBuilder.PROPERTY_LCOM, lcom);
				return lcom;
			} else {
				double lcom = 0;
				Vertex v = GraphUtils.findVertex(graph, clazz.getClassName(),GraphBuilder.CLASS);
				v.property(GraphBuilder.PROPERTY_LCOM, lcom);
				return lcom;
			}
		}
	}

	/**
	 * @param classVertex
	 * @param label
	 * @param dir
	 * @return the ingoing or outgoing edges of the given class.
	 * @throws TypeVertexException
	 */
	public List<Edge> calculateClassEdges(Vertex classVertex, String label, Direction dir) throws TypeVertexException {
		if (GraphBuilder.PACKAGE.equals(classVertex.label())) {
			throw new TypeVertexException("Wrong Vertex type. Expected type class");
		}
		return ImmutableList.copyOf(classVertex.edges(dir, label));
	}
}