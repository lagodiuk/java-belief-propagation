package com.lahodiuk.bp;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Node<STATES> {

	public abstract Set<STATES> getStates();

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

	public TObjectDoubleMap<STATES> getPosteriorProbabilities() {
		TObjectDoubleMap<STATES> stateToLogPriorProbabilityAndProductIncomingMessages =
				this.getStateToLogPriorProbabilityAndProductIncomingMessages();

		// If we uncomment the following lines of code -
		// Hamming Code Recovery via Belief Propagation will reconstruct
		// correctly even parity-check bits. Otherwise - only payload bits are
		// recovered correctly.
		// TODO: investigate the reason of this observation.
		//
		// for (STATES s :
		// stateToLogPriorProbabilityAndProductIncomingMessages.keySet()) {
		// if
		// (!stateToLogPriorProbabilityAndProductIncomingMessages.adjustValue(s,
		// -0.3 * this.getLogPriorProbablility(s))) {
		// throw new RuntimeException();
		// }
		// }

		// normalize
		double sum = Edge.logOfSum(stateToLogPriorProbabilityAndProductIncomingMessages.values());
		for (STATES state : stateToLogPriorProbabilityAndProductIncomingMessages.keySet()) {
			stateToLogPriorProbabilityAndProductIncomingMessages.put(state, Math.exp(stateToLogPriorProbabilityAndProductIncomingMessages.get(state) - sum));
		}

		return stateToLogPriorProbabilityAndProductIncomingMessages;
	}

	public TObjectDoubleMap<STATES> getStateToLogPriorProbabilityAndProductIncomingMessages() {
		TObjectDoubleMap<STATES> stateToLogPriorProbabilityAndProductIncomingMessages = new TObjectDoubleHashMap<>();
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
		TObjectDoubleMap<STATES> stateToProbability = this.getPosteriorProbabilities();
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
