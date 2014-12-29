package com.lahodiuk.bp;

import java.util.HashMap;
import java.util.Map;

public class Edge {

	private Node node1;

	private Node node2;

	private Potential potential;

	private String edgeType;

	private Map<String, Double> node1ToNode2Messages = new HashMap<String, Double>();

	private Map<String, Double> node1ToNode2MessagesNew = new HashMap<String, Double>();

	private Map<String, Double> node2ToNode1Messages = new HashMap<String, Double>();

	private Map<String, Double> node2ToNode1MessagesNew = new HashMap<String, Double>();

	private Edge(Node node1, Node node2, String edgeType, Potential potential) {
		this.edgeType = edgeType;
		this.potential = potential;
		this.node1 = node1;
		this.node2 = node2;

		for (String stateOfNode1 : node1.getStates()) {
			this.node2ToNode1Messages.put(stateOfNode1, 1.0);
		}

		for (String stateOfNode2 : node2.getStates()) {
			this.node1ToNode2Messages.put(stateOfNode2, 1.0);
		}
	}

	public static Edge connect(Node node1, Node node2, String edgeType, Potential potential) {
		Edge edge = new Edge(node1, node2, edgeType, potential);
		node1.addEdge(edge);
		node2.addEdge(edge);
		return edge;
	}

	public double getIncomingMessage(Node node, String state) {
		if (node == this.node1) {
			return this.node2ToNode1Messages.get(state);
		}

		if (node == this.node2) {
			return this.node1ToNode2Messages.get(state);
		}

		throw new RuntimeException();
	}

	public void updateMessages() {
		// node1 -> node2
		for (String stateOfNode2 : this.node2.getStates()) {
			double sum = 0;
			for (String stateOfNode1 : this.node1.getStates()) {
				double productOfIncomingMessages = 1;
				for (Edge edge : this.node1.getEdges()) {
					if (edge == this) {
						continue;
					}
					productOfIncomingMessages *= edge.getIncomingMessage(this.node1, stateOfNode1);
				}
				sum += this.node1.getPriorProbablility(stateOfNode1) * this.potential.getValue(stateOfNode1, stateOfNode2, this.edgeType) * productOfIncomingMessages;
			}
			this.node1ToNode2MessagesNew.put(stateOfNode2, sum);
		}

		// node2 -> node1
		for (String stateOfNode1 : this.node1.getStates()) {
			double sum = 0;
			for (String stateOfNode2 : this.node2.getStates()) {
				double productOfIncomingMessages = 1;
				for (Edge edge : this.node2.getEdges()) {
					if (edge == this) {
						continue;
					}
					productOfIncomingMessages *= edge.getIncomingMessage(this.node2, stateOfNode2);
				}
				sum += this.node2.getPriorProbablility(stateOfNode2) * this.potential.getValue(stateOfNode1, stateOfNode2, this.edgeType) * productOfIncomingMessages;
			}
			this.node2ToNode1MessagesNew.put(stateOfNode1, sum);
		}
	}

	public void refreshMessages() {
		// node1 -> node2
		double sum1 = 0;
		for (double message : this.node1ToNode2MessagesNew.values()) {
			sum1 += message;
		}
		for (String stateOfNode2 : this.node1ToNode2MessagesNew.keySet()) {
			this.node1ToNode2Messages.put(stateOfNode2, this.node1ToNode2MessagesNew.get(stateOfNode2) / sum1);
		}

		// node2 -> node1
		double sum2 = 0;
		for (double message : this.node2ToNode1MessagesNew.values()) {
			sum2 += message;
		}
		for (String stateOfNode1 : this.node2ToNode1MessagesNew.keySet()) {
			this.node2ToNode1Messages.put(stateOfNode1, this.node2ToNode1MessagesNew.get(stateOfNode1) / sum2);
		}
	}
}
