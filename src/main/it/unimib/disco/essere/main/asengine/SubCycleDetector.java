package it.unimib.disco.essere.main.asengine;

import org.apache.tinkerpop.gremlin.structure.*;

import java.util.*;

public class SubCycleDetector {
    private final Graph graph;
    private final List<Vertex> vertices;
    private final Map<Vertex, Set<Vertex>> adjList = new HashMap<>();

    private final Stack<Vertex> stack = new Stack<>();
    private final Set<Vertex> blockedSet = new HashSet<>();
    private final Map<Vertex, Set<Vertex>> blockedMap = new HashMap<>();
    private final List<List<Vertex>> allCycles = new ArrayList<>();

    public SubCycleDetector(Graph graph, List<Vertex> vertices, List<Edge> edges) {
        this.graph = graph;
        this.vertices = vertices;

        for (Vertex v : vertices) {
            adjList.put(v, new HashSet<>());
        }

        for (Edge e : edges) {
            Vertex from = e.outVertex();
            Vertex to = e.inVertex(); // assuming directed edges
            adjList.get(from).add(to);
        }
    }

    public void findCyclesAndStore() {
        Set<Vertex> subgraphVertices = new HashSet<>(vertices);

        for (int i = 0; i < vertices.size(); i++) {
            Vertex startVertex = vertices.get(i);

            // Build subgraph induced from startVertex onwards
            Set<Vertex> subgraph = new HashSet<>(vertices.subList(i, vertices.size()));

            blockedSet.clear();
            blockedMap.clear();
            stack.clear();

            findCycles(startVertex, startVertex, subgraph);

            // Remove startVertex and all its edges
            for (Set<Vertex> neighbors : adjList.values()) {
                neighbors.remove(startVertex);
            }
        }

        // Store found cycles as vertices in the graph
        for (List<Vertex> cycle : allCycles) {
            Vertex cycleNode = graph.addVertex("label", "Cycle");
            for (Vertex v : cycle) {
                cycleNode.addEdge("cycleContains", v);
            }
        }
    }

    private boolean findCycles(Vertex start, Vertex current, Set<Vertex> subgraph) {
        boolean foundCycle = false;
        stack.push(current);
        blockedSet.add(current);

        for (Vertex neighbor : adjList.getOrDefault(current, Collections.emptySet())) {
            if (!subgraph.contains(neighbor)) continue;

            if (neighbor.equals(start)) {
                // Found a cycle
                List<Vertex> cycle = new ArrayList<>(stack);
                allCycles.add(new ArrayList<>(cycle));
                foundCycle = true;
            } else if (!blockedSet.contains(neighbor)) {
                if (findCycles(start, neighbor, subgraph)) {
                    foundCycle = true;
                }
            }
        }

        if (foundCycle) {
            unblock(current);
        } else {
            for (Vertex neighbor : adjList.getOrDefault(current, Collections.emptySet())) {
                blockedMap.computeIfAbsent(neighbor, k -> new HashSet<>()).add(current);
            }
        }

        stack.pop();
        return foundCycle;
    }

    private void unblock(Vertex vertex) {
        blockedSet.remove(vertex);
        Set<Vertex> blocked = blockedMap.get(vertex);
        if (blocked != null) {
            for (Vertex v : blocked) {
                if (blockedSet.contains(v)) {
                    unblock(v);
                }
            }
            blocked.clear();
        }
    }
}