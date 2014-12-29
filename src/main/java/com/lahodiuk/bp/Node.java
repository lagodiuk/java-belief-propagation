package com.lahodiuk.bp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Node {

	public abstract Set<String> getStates();

	public abstract double getPriorProbablility(String state);

	private List<Edge> edges = new ArrayList<>();

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

	public List<Edge> getEdges() {
		return this.edges;
	}

	public Map<String, Double> getPosteriorProbabilities() {
		Map<String, Double> stateToProbability = new HashMap<String, Double>();
		for (String state : this.getStates()) {
			double productOfIncomingMessages = 1;
			for (Edge edge : this.edges) {
				productOfIncomingMessages *= edge.getIncomingMessage(this, state);
			}
			stateToProbability.put(state, this.getPriorProbablility(state) * productOfIncomingMessages);
		}

		// normalize
		double sum = 0;
		for (double probability : stateToProbability.values()) {
			sum += probability;
		}
		for (String state : stateToProbability.keySet()) {
			stateToProbability.put(state, stateToProbability.get(state) / sum);
		}

		return stateToProbability;
	}

}
