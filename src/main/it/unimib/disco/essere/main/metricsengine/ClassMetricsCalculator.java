package it.unimib.disco.essere.main.metricsengine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import it.unimib.disco.essere.main.graphmanager.*;
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

public class ClassMetricsCalculator
{
    private static final Logger logger = LogManager.getLogger(ClassMetricsCalculator.class);
    public Graph graph;
    private Map<String, Vertex> classes; // Modded
    private EdgeMaps edgeMaps; // Modded


    public ClassMetricsCalculator(Graph graph)
    {
        this.graph = graph;
        //Modded
        List<Vertex> classList = GraphUtils.findVerticesByLabel(graph, GraphBuilder.CLASS);
        for (Vertex vertex : classList)
        {
            classes.put(vertex.value(GraphBuilder.PROPERTY_NAME), vertex);
        }
    }

    //Modded
    public ClassMetricsCalculator(Graph graph, Map<String, Vertex> classes, EdgeMaps edgeMaps)
    {
        this.graph = graph;
        this.classes = classes;
        this.edgeMaps = edgeMaps;
    }


    /**
     * @param classVertex
     * @return the number of outgoing dependences of classVertex.
     */

    public int calculateFanOut(final Vertex classVertex)
    {
        List<Edge> edges = edgeMaps.getEdgesByOutVertex(GraphBuilder.LBL_CLASS_DEP, classVertex); //Modded
        int fanout = edges == null ? 0 : edges.size(); //Modded
        classVertex.property(GraphBuilder.PROPERTY_FANOUT, fanout);
        return fanout;
    }

    public int calculateFanOut(String classVertex) throws TypeVertexException
    {
        Vertex clazz = classes.get(classVertex);
        return calculateFanOut(clazz);
    }

    /**
     * @param classVertex
     * @return the number of ingoing dependences of classVertex.
     */
    public int calculateFanIn(final Vertex classVertex)
    {
        List<Edge> edges = edgeMaps.getEdgesByInVertex(GraphBuilder.LBL_CLASS_DEP, classVertex); //Modded
        int fanin = edges == null ? 0 : edges.size(); //Modded
        classVertex.property(GraphBuilder.PROPERTY_FANIN, fanin);
        return fanin;
    }

    public int calculateFanIn(String classVertex) throws TypeVertexException
    {
        Vertex clazz = classes.get(classVertex); //Modded
        return calculateFanIn(clazz);
    }

    /**
     * Calculates the number of classes to which a class is coupled according to
     * Chidamber & Kemerer metric. Multiple accesses to the same class are
     * counted as one access.
     *
     * @param classVertex
     * @return the number of classes to which classVertex is coupled.
     */
    // TODO: This currently returns the same as fan out.
    // TODO: The original GraphBuilder implementation also never created more than one class dependency edge between two classes.
    // TODO: This should consider the edge weight instead.
    public int calculateCBO(final Vertex classVertex)
    {
        List<Edge> edges = edgeMaps.getEdgesByOutVertex(GraphBuilder.LBL_CLASS_DEP, classVertex); //Modded
        int cbo = edges == null ? 0 : edges.size(); //Modded
        classVertex.property(GraphBuilder.PROPERTY_CBO, cbo);
        return cbo;
    }

    public int calculateCBO(String classVertex) throws TypeVertexException
    {
        Vertex clazz = classes.get(classVertex); //Modded
        return calculateCBO(clazz);
    }

    // TODO refactoring

    /**
     * Calculates the lack of cohesion in method according to the
     * Henderson-Sellers metric.
     *
     * @param clazz
     * @return the lack of internal cohesion of clazz or -1 if there are no
     * methods/fields in the class.
     */
    public double calculateLCOM(final JavaClass clazz)
    {
        Field[] fields = clazz.getFields();
        Method[] methods = clazz.getMethods();

        if (fields.length == 0 || methods.length == 0)
        {
            double lcom = 0;
            Vertex v = classes.get(clazz.getClassName()); //Modded
            //Vertex v = GraphUtils.findVertex(graph, clazz.getClassName(),GraphBuilder.CLASS);
            v.property(GraphBuilder.PROPERTY_LCOM, lcom);
            return lcom;
        }
        else
        {
            Map<String, Integer[]> fieldsMap = new HashMap<>();

            for (Field f : fields)
            {
                fieldsMap.put(f.getName(), new Integer[methods.length]);
            }

            ConstantPool cp = clazz.getConstantPool();
            ConstantPoolGen cpg = new ConstantPoolGen(cp);
            int index = 0;
            for (Method m : methods)
            {
                MethodGen mg = new MethodGen(m, clazz.getClassName(), cpg);
                InstructionList instructions = mg.getInstructionList();
                if (instructions != null)
                {
                    InstructionHandle[] ihs = instructions.getInstructionHandles();
                    for (int i = 0; i < ihs.length; i++)
                    {
                        InstructionHandle ih = ihs[i];
                        Instruction instruction = ih.getInstruction();
                        if (instruction instanceof FieldInstruction)
                        {
                            FieldInstruction fi = (FieldInstruction) instruction;
                            String name = fi.getFieldName(cpg);
                            if (fieldsMap.containsKey(name) && fieldsMap.get(name)[index] == null)
                            {
                                fieldsMap.get(name)[index] = 1;
                            }
                        }
                    }
                }
                ++index;
            }

            double sum = 0;
            for (Entry<String, Integer[]> entry : fieldsMap.entrySet())
            {
                for (Integer occurrence : entry.getValue())
                {
                    if (occurrence != null)
                    {
                        ++sum;
                    }
                }
            }

            double mean = sum / fields.length;

            if (clazz.getMethods().length != 1 && mean - clazz.getMethods().length != 0)
            {
                double lcom = (mean - clazz.getMethods().length) / (1 - clazz.getMethods().length);
                Vertex v = classes.get(clazz.getClassName()); //Modded
                //Vertex v = GraphUtils.findVertex(graph, clazz.getClassName(),GraphBuilder.CLASS);
                v.property(GraphBuilder.PROPERTY_LCOM, lcom);
                return lcom;
            }
            else
            {
                double lcom = 0;
                Vertex v = classes.get(clazz.getClassName()); //Modded
                //Vertex v = GraphUtils.findVertex(graph, clazz.getClassName(),GraphBuilder.CLASS);
                v.property(GraphBuilder.PROPERTY_LCOM, lcom);
                return lcom;
            }
        }
    }

}