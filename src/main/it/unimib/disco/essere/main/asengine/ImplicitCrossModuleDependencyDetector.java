package it.unimib.disco.essere.main.asengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.util.tools.MultiMap;

import com.google.common.collect.ImmutableList;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;
import it.unimib.disco.essere.main.graphmanager.GraphBuilderSystemByCommit;
import it.unimib.disco.essere.main.graphmanager.GraphUtils;
import it.unimib.disco.essere.main.graphmanager.TypeVertexException;

public class ImplicitCrossModuleDependencyDetector {
	private static final Logger logger = LogManager.getLogger(ImplicitCrossModuleDependencyDetector.class);
	public Graph _graph;
	//    public MetricsCalculator calc;
	public MultiMap _smellMap;

	public ImplicitCrossModuleDependencyDetector(final Graph graph) {
		_graph = graph;
		//        this.calc = calculator;
		_smellMap = null;
	}
	
	public Map<String, List<String>> detect() throws TypeVertexException {
		List<Edge> modification = GraphUtils.findEdgesByLabel(_graph, GraphBuilderSystemByCommit.COUPLED_MODIFIED_LABEL);
		Map<String, List<String>> r = new HashMap<String, List<String>>();
		if(modification!=null){
			for(Edge e : modification){
				Vertex outJavaV = e.outVertex();
				Vertex inJavaV = e.inVertex();
				Iterator<Edge> packageOut = outJavaV.edges(Direction.OUT, GraphBuilderSystemByCommit.PACKAGE_DEPENDENCY_LABEL);
				Iterator<Edge> packageIn = inJavaV.edges(Direction.OUT, GraphBuilderSystemByCommit.PACKAGE_DEPENDENCY_LABEL);
				String nameOutV = outJavaV.value(GraphBuilderSystemByCommit.PROPERTY_NAME);
				if(!r.containsKey(nameOutV)){
					List<String> l = new ArrayList<String>();
					r.put(nameOutV, l);
				}
				while(packageOut.hasNext()){
					Edge fout = packageOut.next();
//					String packageOutName = fout.inVertex().value(GraphBuilderSystemByCommit.PROPERTY_NAME);
//					if(!r.containsKey(packageOutName)){
//						List<String> l = new ArrayList<String>();
//						r.put(packageOutName, l);
//					}
					while(packageIn.hasNext()){
						Edge fin = packageIn.next();
						int counter = e.value(GraphBuilderSystemByCommit.PROPERTY_COUNTER);
						if(!fin.inVertex().equals(fout.inVertex())&&counter>2){
							//							logger.debug(String.format("%s %s %s TROVATO: %s",
							//									outV.value(GraphBuilderSystemByCommit.PROPERTY_NAME),
							//									inV.value(GraphBuilderSystemByCommit.PROPERTY_NAME),
							//									e.value(GraphBuilderSystemByCommit.PROPERTY_COUNTER),
							//									((int)e.value(GraphBuilderSystemByCommit.PROPERTY_COUNTER)>3)));
							Iterator<Edge> pout = outJavaV.edges(Direction.IN, GraphBuilderSystemByCommit.MODIFIED_LABEL);
							Iterator<Edge> pin = inJavaV.edges(Direction.IN, GraphBuilderSystemByCommit.MODIFIED_LABEL);
							int o =ImmutableList.copyOf(pout).size();
							int i =ImmutableList.copyOf(pin).size();
							double ratioOut = ((double)counter/o);
							double ratioIn = ((double)counter/i);
							if(ratioIn>0.6 && ratioOut>0.6){
								String nameInV = inJavaV.value(GraphBuilderSystemByCommit.PROPERTY_NAME);
								logger.debug(String.format("%s commit-out: %s, in: %s, ratio out: %f, in: %f; %s -> %s", counter, o,i, ratioOut,ratioIn, nameOutV,nameInV));
								List<String> l = r.get(nameOutV);
								l.add(nameInV);
								l.add(""+o);
								l.add(""+i);
								l.add(""+ratioOut);
								l.add(""+ratioIn);
								l.add(""+counter);
								r.put(nameOutV, l);
								Vertex ixpd = GraphUtils.createImplicitCrossPackageDependencySmellVertex(_graph,String.format("%s -> %s",nameOutV,nameInV),o,i,ratioOut,ratioIn);
								ixpd.addEdge(GraphBuilder.LABEL_IS_IXPD_OUT, outJavaV);
								ixpd.addEdge(GraphBuilder.LABEL_IS_IXPD_IN, inJavaV);
								ixpd.addEdge(GraphBuilder.LABEL_IS_IXPD_PKG_OUT, fout.inVertex());
								ixpd.addEdge(GraphBuilder.LABEL_IS_IXPD_PKG_IN, fin.inVertex());
							}
						}
					}
				}
			}
		}
		return r;
	}
	
	

}
