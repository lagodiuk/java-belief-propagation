package com.lahodiuk.bp.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

/**
 * Petersen graph <br/>
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
		Potential potential = new GraphColorPotential();

		Map<Integer, GraphColorNode> nodeIdToNode = new HashMap<Integer, GraphColorNode>();
		for (int i = 1; i <= 10; i++) {
			nodeIdToNode.put(i, new GraphColorNode());
		}
		// Make 1 node: Green
		nodeIdToNode.put(1, new GraphColorNode() {
			@Override
			public double getPriorProbablility(String state) {
				if (state == GREEN) {
					return 0.99;
				}
				return 0.005;
			}
		});
		// Make 2 node: Red
		nodeIdToNode.put(2, new GraphColorNode() {
			@Override
			public double getPriorProbablility(String state) {
				if (state == RED) {
					return 0.99;
				}
				return 0.005;
			}
		});

		List<Edge> edges = new ArrayList<>();

		edges.add(Edge.connect(nodeIdToNode.get(1), nodeIdToNode.get(3), null, potential));
		edges.add(Edge.connect(nodeIdToNode.get(1), nodeIdToNode.get(4), null, potential));
		edges.add(Edge.connect(nodeIdToNode.get(1), nodeIdToNode.get(10), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(2), nodeIdToNode.get(5), null, potential));
		edges.add(Edge.connect(nodeIdToNode.get(2), nodeIdToNode.get(4), null, potential));
		edges.add(Edge.connect(nodeIdToNode.get(2), nodeIdToNode.get(9), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(3), nodeIdToNode.get(5), null, potential));
		edges.add(Edge.connect(nodeIdToNode.get(3), nodeIdToNode.get(8), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(4), nodeIdToNode.get(7), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(5), nodeIdToNode.get(6), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(6), nodeIdToNode.get(10), null, potential));
		edges.add(Edge.connect(nodeIdToNode.get(6), nodeIdToNode.get(7), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(7), nodeIdToNode.get(8), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(8), nodeIdToNode.get(9), null, potential));

		edges.add(Edge.connect(nodeIdToNode.get(9), nodeIdToNode.get(10), null, potential));

		for (int i = 0; i < 10; i++) {
			for (Edge e : edges) {
				e.updateMessages();
			}
			for (Edge e : edges) {
				e.refreshMessages();
			}
		}

		for (int i = 1; i <= 10; i++) {
			System.out.print(i);
			Map<String, Double> stateProbability = nodeIdToNode.get(i).getPosteriorProbabilities();
			for (String state : stateProbability.keySet()) {
				System.out.print(String.format("\t %s = %.2f", state, stateProbability.get(state)));
			}
			System.out.println();
		}
	}
}

class GraphColorNode extends Node {

	public static final String RED = "red";
	public static final String GREEN = "green";
	public static final String BLUE = "blue";

	private static final Set<String> COLORS = new HashSet<>();

	static {
		COLORS.add(RED);
		COLORS.add(GREEN);
		COLORS.add(BLUE);
	}

	@Override
	public Set<String> getStates() {
		return COLORS;
	}

	@Override
	public double getPriorProbablility(String state) {
		return 1.0 / COLORS.size();
	}
}

class GraphColorPotential extends Potential {

	private static double EPSILON = 0.000001;

	@Override
	public double getValue(String node1State, String node2State, String edgeType) {
		if (node1State == node2State) {
			return EPSILON;
		} else {
			return 1.0 - EPSILON;
		}
	}
}