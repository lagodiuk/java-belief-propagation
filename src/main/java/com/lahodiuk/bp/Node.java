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

	public double getLogPriorProbablility(String state) {
		return Math.log(this.getPriorProbablility(state));
	}

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

	public List<Edge> getEdges() {
		return this.edges;
	}

	public Map<String, Double> getPosteriorProbabilities() {
		Map<String, Double> stateToLogPriorProbabilityAndProductIncomingMessages =
				this.getStateToLogPriorProbabilityAndProductIncomingMessages();

		// normalize
		double sum = Edge.logOfSum(stateToLogPriorProbabilityAndProductIncomingMessages.values());
		for (String state : stateToLogPriorProbabilityAndProductIncomingMessages.keySet()) {
			stateToLogPriorProbabilityAndProductIncomingMessages.put(state, Math.exp(stateToLogPriorProbabilityAndProductIncomingMessages.get(state) - sum));
		}

		return stateToLogPriorProbabilityAndProductIncomingMessages;
	}

	public Map<String, Double> getStateToLogPriorProbabilityAndProductIncomingMessages() {
		Map<String, Double> stateToLogPriorProbabilityAndProductIncomingMessages = new HashMap<String, Double>();
		for (String state : this.getStates()) {
			double logProductOfIncomingMessages = 0;
			for (Edge edge : this.edges) {
				logProductOfIncomingMessages += edge.getLogIncomingMessage(this, state);
			}
			stateToLogPriorProbabilityAndProductIncomingMessages.put(state,
					this.getLogPriorProbablility(state) + logProductOfIncomingMessages);
		}
		return stateToLogPriorProbabilityAndProductIncomingMessages;
	}

	public String getMostProbableState() {
		Map<String, Double> stateToProbability = this.getPosteriorProbabilities();
		String mostProbableState = null;
		double mostProbableStateProbability = 0;
		for (String state : stateToProbability.keySet()) {
			double probability = stateToProbability.get(state);
			if (probability > mostProbableStateProbability) {
				mostProbableState = state;
				mostProbableStateProbability = probability;
			}
		}
		return mostProbableState;
	}
}
