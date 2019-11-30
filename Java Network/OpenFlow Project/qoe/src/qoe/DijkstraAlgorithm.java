package qoe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DijkstraAlgorithm {

	public static List<Vertex> nodes;
	public static List<Edge> edges;
	public Set<Vertex> settledNodes;
	public Set<Vertex> unSettledNodes;
	public Map<Vertex, Vertex> predecessors;
	public Map<Vertex, Integer> distance;

	public DijkstraAlgorithm(Graph graph) {
		// create a copy of the array so that we can operate on this array
		this.nodes = new ArrayList<Vertex>(graph.getVertexes());
		this.edges = new ArrayList<Edge>(graph.getEdges());
	}

	public void execute(Vertex source) {
		settledNodes = new HashSet<Vertex>();
		unSettledNodes = new HashSet<Vertex>();
		distance = new HashMap<Vertex, Integer>();
		predecessors = new HashMap<Vertex, Vertex>();
		distance.put(source, 0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			Vertex node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(Vertex node) {
		List<Vertex> adjacentNodes = getNeighbors(node);
		for (Vertex target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node)
					+ getDistance(node, target)) {
				distance.put(target, getShortestDistance(node)
						+ getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}

	}

	private int getDistance(Vertex node, Vertex target) {
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<Vertex> getNeighbors(Vertex node) {
		List<Vertex> neighbors = new ArrayList<Vertex>();
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& !isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}

	private Vertex getMinimum(Set<Vertex> vertexes) {
		Vertex minimum = null;
		for (Vertex vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(Vertex vertex) {
		return settledNodes.contains(vertex);
	}

	private int getShortestDistance(Vertex destination) {
		Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public LinkedList<Vertex> getPath(Vertex target) {
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		Vertex step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}

	private static void addLane( int sourceLocNo, int destLocNo, int duration) {
		Edge lane = new Edge(nodes.get(sourceLocNo), nodes.get(destLocNo), duration );
		edges.add(lane);
	}

	public static void main(String[] args) {
		nodes = new ArrayList<Vertex>();
		edges  = new ArrayList<Edge>();  
		for (int i = 0; i < 11; i++) {
			Vertex location = new Vertex("Node_" + i, "Node_" + i);
			System.out.println("Node_" + i +  "Node_" + i);
			nodes.add(location);
		}

		addLane( 0, 1, 85);
		addLane( 0, 2, 217);
		addLane( 0, 4, 173);
		addLane( 1, 0, 10);
		addLane( 1, 5, 20);
		addLane( 5, 1, 20);
		addLane( 2, 6, 186);
		addLane( 2, 7, 103);
		addLane( 3, 7, 183);
		addLane( 5, 8, 250);
		addLane( 8, 9, 84);
		addLane( 7, 9, 167);
		addLane( 4, 9, 502);
		addLane( 9, 10, 40);
		addLane( 1, 10, 600);

		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(nodes, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		dijkstra.execute(nodes.get(1));
		LinkedList<Vertex> path = dijkstra.getPath(nodes.get(6));

		for (Vertex vertex : path) {
			System.out.println(vertex);
		}
	}
}

