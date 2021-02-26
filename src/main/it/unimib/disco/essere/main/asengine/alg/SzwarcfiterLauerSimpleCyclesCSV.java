package it.unimib.disco.essere.main.asengine.alg;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.GabowStrongConnectivityInspector;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.cycle.SzwarcfiterLauerSimpleCycles;

import it.unimib.disco.essere.main.graphmanager.GraphBuilder;

@Deprecated
public class SzwarcfiterLauerSimpleCyclesCSV<V, E> extends SzwarcfiterLauerSimpleCycles<V, E> {
	private static final Logger logger = LogManager.getLogger(SzwarcfiterLauerSimpleCyclesCSV.class);

	// The graph.
	private DirectedGraph<V, E> graph;

	// The state of the algorithm.
	private V[] iToV = null;
	private Map<V, Integer> vToI = null;
	private Map<V, Set<V>> bSets = null;
	private ArrayDeque<V> stack = null;
	private Set<V> marked = null;
	private Map<V, Set<V>> removed = null;
	private int[] position = null;
	private boolean[] reach = null;
	private List<V> startVertices = null;
	private List<Vertex> classes = null;
	private FileWriter writer = null;
	private CSVFormat formatter = null;
	private CSVPrinter printer = null;
	private int cycleCounter = 0;

	public SzwarcfiterLauerSimpleCyclesCSV(DirectedGraph<V, E> graph,List<Vertex> classes, String filename) {
		this.graph = graph;
		this.classes = classes;
		try {
			writer = new FileWriter(Paths
					.get("Analyzed Projects" , filename + "CyclicDependences.csv")
					.toAbsolutePath()
					.toFile());
			String[] header = new String[classes.size() + 1];

			for (int i = 0; i < header.length; ++i) {
				if (i == 0) {
					header[i] = "Cycle";
				} else {
					header[i] = classes.get(i - 1).value(GraphBuilder.PROPERTY_NAME).toString();
				}
			}
			formatter = CSVFormat.EXCEL.withHeader(header);
			printer = new CSVPrinter(writer, formatter);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};

	}

	/**
	 * {@inheritDoc}
	 */
	public void findSimpleCyclesPrint() throws IllegalArgumentException {
		logger.info("***Start cycle detection***");
		// Just a straightforward implementation of
		// the algorithm.
		if (graph == null) {
			throw new IllegalArgumentException("Null graph.");
		}
		initState();
		GabowStrongConnectivityInspector<V,E> inspector = new GabowStrongConnectivityInspector<V,E>(graph);
		List<Set<V>> sccs = inspector.stronglyConnectedSets();
		logger.debug("***populating starting vertex set***");
		for (Set<V> scc : sccs) {
			int maxInDegree = -1;
			V startVertex = null;
			for (V v : scc) {
				int inDegree = graph.inDegreeOf(v);
				if (inDegree > maxInDegree) {
					maxInDegree = inDegree;
					startVertex = v;
				}
			}
			startVertices.add(startVertex);
		}
		logger.debug("***populating starting vertex set*** - "+startVertices.size());

		logger.debug("***starting for to cycle detection***");
		for (V vertex : startVertices) {
			cycle(toI(vertex), 0);
		}

		clearState();
		try {
			printer.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("***Closed csv writer and terminated cycle detection***");
	}

	private boolean cycle(int v, int q) {
		boolean foundCycle = false;
		V vV = toV(v);
		marked.add(vV);
		stack.push(vV);
		int t = stack.size();
		position[v] = t;
		if (!reach[v]) {
			q = t;
		}
		Set<V> avRemoved = getRemoved(vV);
		Set<E> edgeSet = graph.outgoingEdgesOf(vV);
		Iterator<E> avIt = edgeSet.iterator();
		while (avIt.hasNext()) {
			E e = avIt.next();
			V wV = graph.getEdgeTarget(e);
			if (avRemoved.contains(wV)) {
				continue;
			}
			int w = toI(wV);
			if (!marked.contains(wV)) {
				boolean gotCycle = cycle(w, q);
				if (gotCycle) {
					foundCycle = gotCycle;
				} else {
					noCycle(v, w);
				}
			} else if (position[w] <= q) {
				foundCycle = true;
				List<V> cycle = new ArrayList<V>();
				Iterator<V> it = stack.descendingIterator();
				V current = null;
				while (it.hasNext()) {
					current = it.next();
					if (wV.equals(current)) {
						break;
					}
				}
				cycle.add(wV);
				while (it.hasNext()) {
					current = it.next();
					cycle.add(current);
					if (current.equals(vV)) {
						break;
					}
				}
				logger.debug("***Start print cycle to CSV*** - "+cycleCounter);
				printCycles(cycle);
				logger.debug("***End print cycle to CSV*** - "+cycleCounter);
				++cycleCounter;
			} else {
				noCycle(v, w);
			}
		}
		stack.pop();
		if (foundCycle) {
			unmark(v);
		}
		reach[v] = true;
		position[v] = graph.vertexSet().size();
		return foundCycle;
	}

	private void printCycles(List<V> cycle) {
		logger.debug("***inside CSV method***");
		List<String> verticesInCycle = new ArrayList<>();
		int i = 0;
		for (V v : cycle) {
			Vertex vx = (Vertex) v;
			verticesInCycle.add(vx.value(GraphBuilder.PROPERTY_NAME).toString());
			i++;
		}
		logger.debug("***Collected vertex of the cycle "+i+"***");
		i=0;
		try {
			printer.print("Cycle" + cycleCounter);
			for (Vertex clazz : classes) {
				if (verticesInCycle.contains(clazz.value(GraphBuilder.PROPERTY_NAME).toString())) {
					printer.print(1);
					i++;
				} else {
					printer.print(0);
				}
			}
			printer.println();
			logger.debug("***Printed "+i+" vertex of the cycle "+cycleCounter+"***");
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.debug("***End of CSV method***");
	}

	private void noCycle(int x, int y) {
		V xV = toV(x);
		V yV = toV(y);

		Set<V> by = getBSet(yV);
		Set<V> axRemoved = getRemoved(xV);

		by.add(xV);
		axRemoved.add(yV);
	}

	private void unmark(int x) {
		V xV = toV(x);
		marked.remove(xV);
		Set<V> bx = getBSet(xV);
		for (V yV : bx) {
			Set<V> ayRemoved = getRemoved(yV);
			ayRemoved.remove(xV);
			if (marked.contains(yV)) {
				unmark(toI(yV));
			}
		}
		bx.clear();
	}

	@SuppressWarnings("unchecked")
	private void initState() {
		iToV = (V[]) graph.vertexSet().toArray();
		vToI = new HashMap<V, Integer>();
		bSets = new HashMap<V, Set<V>>();
		stack = new ArrayDeque<V>();
		marked = new HashSet<V>();
		removed = new HashMap<V, Set<V>>();
		int size = graph.vertexSet().size();
		position = new int[size];
		reach = new boolean[size];
		startVertices = new ArrayList<V>();

		for (int i = 0; i < iToV.length; i++) {
			vToI.put(iToV[i], i);
		}
	}

	private void clearState() {
		iToV = null;
		vToI = null;
		bSets = null;
		stack = null;
		marked = null;
		removed = null;
		position = null;
		reach = null;
		startVertices = null;
	}

	private Integer toI(V v) {
		return vToI.get(v);
	}

	private V toV(int i) {
		return iToV[i];
	}

	private Set<V> getBSet(V v) {
		// B sets are typically not all
		// needed, so instantiate lazily.
		Set<V> result = bSets.get(v);
		if (result == null) {
			result = new HashSet<V>();
			bSets.put(v, result);
		}
		return result;
	}

	private Set<V> getRemoved(V v) {
		// Removed sets typically not all
		// needed, so instantiate lazily.
		Set<V> result = removed.get(v);
		if (result == null) {
			result = new HashSet<V>();
			removed.put(v, result);
		}
		return result;
	}
}
