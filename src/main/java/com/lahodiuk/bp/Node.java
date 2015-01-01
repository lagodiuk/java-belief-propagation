package com.lahodiuk.bp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Node<STATES> {

	public abstract Iterable<STATES> getStates();

	public abstract double getPriorProbablility(STATES state);

	private List<Edge<?, ?>> edges = new ArrayList<>();

	public double getLogPriorProbablility(STATES state) {
		return Math.log(this.getPriorProbablility(state));
	}

	public void addEdge(Edge<?, ?> edge) {
		this.edges.add(edge);
	}

	public List<Edge<?, ?>> getEdges() {
		return this.edges;
	}

	public Map<STATES, Double> getPosteriorProbabilities() {
		Map<STATES, Double> stateToLogPriorProbabilityAndProductIncomingMessages =
				this.getStateToLogPriorProbabilityAndProductIncomingMessages();

		// normalize
		double sum = Edge.logOfSum(stateToLogPriorProbabilityAndProductIncomingMessages.values());
		for (STATES state : stateToLogPriorProbabilityAndProductIncomingMessages.keySet()) {
			stateToLogPriorProbabilityAndProductIncomingMessages.put(state, Math.exp(stateToLogPriorProbabilityAndProductIncomingMessages.get(state) - sum));
		}

		return stateToLogPriorProbabilityAndProductIncomingMessages;
	}

	public Map<STATES, Double> getStateToLogPriorProbabilityAndProductIncomingMessages() {
		Map<STATES, Double> stateToLogPriorProbabilityAndProductIncomingMessages = new HashMap<>();
		for (STATES state : this.getStates()) {
			double logProductOfIncomingMessages = 0;
			for (Edge<?, ?> edge : this.edges) {
				logProductOfIncomingMessages += edge.getLogIncomingMessage(this, state);
			}
			stateToLogPriorProbabilityAndProductIncomingMessages.put(state,
					this.getLogPriorProbablility(state) + logProductOfIncomingMessages);
		}
		return stateToLogPriorProbabilityAndProductIncomingMessages;
	}

	public STATES getMostProbableState() {
		Map<STATES, Double> stateToProbability = this.getPosteriorProbabilities();
		STATES mostProbableState = null;
		double mostProbableStateProbability = 0;
		for (STATES state : stateToProbability.keySet()) {
			double probability = stateToProbability.get(state);
			if (probability > mostProbableStateProbability) {
				mostProbableState = state;
				mostProbableStateProbability = probability;
			}
		}
		return mostProbableState;
	}
}
