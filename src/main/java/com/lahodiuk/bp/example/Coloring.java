package com.lahodiuk.bp.example;

import gnu.trove.map.TObjectDoubleMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

/**
 * Coloring of Petersen graph <br/>
 * <br/>
 * 1: [3, 4, 10], <br/>
 * 2: [5, 4, 9], <br/>
 * 3: [1, 5, 8], <br/>
 * 4: [2, 1, 7], <br/>
 * 5: [2, 3, 6], <br/>
 * 6: [5, 10, 7], <br/>
 * 7: [4, 6, 8], <br/>
 * 8: [3, 7, 9], <br/>
 * 9: [2, 8, 10], <br/>
 * 10: [1, 6, 9]
 *
 */
public class Coloring {

	public static void main(String[] args) {
		Map<Integer, GraphColorNode> nodeIdToNode = initializeNodesOfPetersenGraph();

		List<Edge<Color, Color>> edges = initializeEdgesOfPetersenGraph(nodeIdToNode);

		inferenceOfMostProbableColorsOfNodes(edges);

		for (int i = 1; i <= 10; i++) {
			System.out.print(i);
			TObjectDoubleMap<Color> stateProbability = nodeIdToNode.get(i).getPosteriorProbabilities();
			for (Color state : stateProbability.keySet()) {
				System.out.print(String.format("\t %s = %.2f", state, stateProbability.get(state)));
			}
			System.out.println();
		}
	}

	public static void inferenceOfMostProbableColorsOfNodes(List<Edge<Color, Color>> edges) {
		for (int i = 0; i < 10; i++) {
			for (Edge<Color, Color> e : edges) {
				e.updateMessages();
			}
			for (Edge<Color, Color> e : edges) {
				e.refreshMessages();
			}
		}
	}

	public static List<Edge<Color, Color>> initializeEdgesOfPetersenGraph(Map<Integer, GraphColorNode> nodeIdToNode) {
		Potential<Color, Color> potential = new GraphColorPotential();

		List<Edge<Color, Color>> edges = new ArrayList<>();

		edges.add(Edge.connect(nodeIdToNode.get(1), nodeIdToNode.get(3), potential));
		edges.add(Edge.connect(nodeIdToNode.get(1), nodeIdToNode.get(4), potential));
		edges.add(Edge.connect(nodeIdToNode.get(1), nodeIdToNode.get(10), potential));

		edges.add(Edge.connect(nodeIdToNode.get(2), nodeIdToNode.get(5), potential));
		edges.add(Edge.connect(nodeIdToNode.get(2), nodeIdToNode.get(4), potential));
		edges.add(Edge.connect(nodeIdToNode.get(2), nodeIdToNode.get(9), potential));

		edges.add(Edge.connect(nodeIdToNode.get(3), nodeIdToNode.get(5), potential));
		edges.add(Edge.connect(nodeIdToNode.get(3), nodeIdToNode.get(8), potential));

		edges.add(Edge.connect(nodeIdToNode.get(4), nodeIdToNode.get(7), potential));

		edges.add(Edge.connect(nodeIdToNode.get(5), nodeIdToNode.get(6), potential));

		edges.add(Edge.connect(nodeIdToNode.get(6), nodeIdToNode.get(10), potential));
		edges.add(Edge.connect(nodeIdToNode.get(6), nodeIdToNode.get(7), potential));

		edges.add(Edge.connect(nodeIdToNode.get(7), nodeIdToNode.get(8), potential));

		edges.add(Edge.connect(nodeIdToNode.get(8), nodeIdToNode.get(9), potential));

		edges.add(Edge.connect(nodeIdToNode.get(9), nodeIdToNode.get(10), potential));
		return edges;
	}

	public static Map<Integer, GraphColorNode> initializeNodesOfPetersenGraph() {

		Map<Integer, GraphColorNode> nodeIdToNode = new HashMap<Integer, GraphColorNode>();

		// Make 1 node: Green
		nodeIdToNode.put(1, new GraphColorNode() {
			@Override
			public double getPriorProbablility(Color state) {
				if (state == Color.GREEN) {
					return 0.99;
				}
				return 0.005;
			}
		});

		// Make 2 node: Red
		nodeIdToNode.put(2, new GraphColorNode() {
			@Override
			public double getPriorProbablility(Color state) {
				if (state == Color.RED) {
					return 0.99;
				}
				return 0.005;
			}
		});

		for (int i = 3; i <= 10; i++) {
			nodeIdToNode.put(i, new GraphColorNode());
		}

		return nodeIdToNode;
	}

	public enum Color {
		RED,
		GREEN,
		BLUE
	}

	public static class GraphColorNode extends Node<Color> {

		private static final Set<Color> COLORS =
				new HashSet<>(Arrays.asList(Color.values()));

		@Override
		public Set<Color> getStates() {
			return COLORS;
		}

		@Override
		public double getPriorProbablility(Color state) {
			return 1.0 / COLORS.size();
		}
	}

	private static final double EPSILON = 0.000001;

	public static class GraphColorPotential extends Potential<Color, Color> {

		@Override
		public double getValue(Color node1State, Color node2State) {
			if (node1State == node2State) {
				return EPSILON;
			} else {
				return 1.0 - EPSILON;
			}
		}
	}
}