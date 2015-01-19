package com.lahodiuk.bp;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class Edge<STATES_OF_NODE_1, STATES_OF_NODE_2> {

	private Node<STATES_OF_NODE_1> node1;

	private Node<STATES_OF_NODE_2> node2;

	private Potential<STATES_OF_NODE_1, STATES_OF_NODE_2> potential;

	private TObjectDoubleMap<STATES_OF_NODE_2> logNode1ToNode2Messages = new TObjectDoubleHashMap<>();

	private TObjectDoubleMap<STATES_OF_NODE_2> logNode1ToNode2MessagesNew = new TObjectDoubleHashMap<>();

	private TObjectDoubleMap<STATES_OF_NODE_1> logNode2ToNode1Messages = new TObjectDoubleHashMap<>();

	private TObjectDoubleMap<STATES_OF_NODE_1> logNode2ToNode1MessagesNew = new TObjectDoubleHashMap<>();

	private double[] bufferForUpdatingMessagesFromNode2;

	private double[] bufferForUpdatingMessagesFromNode1;

	private Edge(Node<STATES_OF_NODE_1> node1, Node<STATES_OF_NODE_2> node2, Potential<STATES_OF_NODE_1, STATES_OF_NODE_2> potential) {
		this.potential = potential;
		this.node1 = node1;
		this.node2 = node2;

		for (STATES_OF_NODE_1 stateOfNode1 : node1.getStates()) {
			this.logNode2ToNode1Messages.put(stateOfNode1, 0.0);
		}

		for (STATES_OF_NODE_2 stateOfNode2 : node2.getStates()) {
			this.logNode1ToNode2Messages.put(stateOfNode2, 0.0);
		}

		this.bufferForUpdatingMessagesFromNode2 = new double[this.node2.getStates().size()];
		this.bufferForUpdatingMessagesFromNode1 = new double[this.node1.getStates().size()];
	}

	public static <STATES_OF_NODE_1, STATES_OF_NODE_2> Edge<STATES_OF_NODE_1, STATES_OF_NODE_2> connect(
			Node<STATES_OF_NODE_1> node1,
			Node<STATES_OF_NODE_2> node2,
			Potential<STATES_OF_NODE_1, STATES_OF_NODE_2> potential) {

		Edge<STATES_OF_NODE_1, STATES_OF_NODE_2> edge = new Edge<>(node1, node2, potential);
		node1.addEdge(edge);
		node2.addEdge(edge);
		return edge;
	}

	public <T> double getLogIncomingMessage(Node<T> node, T state) {
		if (node == this.node1) {
			return this.logNode2ToNode1Messages.get(state);
		}

		if (node == this.node2) {
			return this.logNode1ToNode2Messages.get(state);
		}

		throw new RuntimeException();
	}

	public void updateMessages() {
		this.updateMessagesNode1ToNode2();
		this.updateMessagesNode2ToNode1();
	}

	/**
	 * node2 -> node1
	 */
	public void updateMessagesNode2ToNode1() {
		TObjectDoubleMap<STATES_OF_NODE_2> node2StateToLogPriorProbabilityAndProductIncomingMessages =
				this.node2.getStateToLogPriorProbabilityAndProductIncomingMessages();

		for (STATES_OF_NODE_1 stateOfNode1 : this.node1.getStates()) {
			int i = 0;
			for (STATES_OF_NODE_2 stateOfNode2 : this.node2.getStates()) {

				double logProductOfIncomingMessages =
						node2StateToLogPriorProbabilityAndProductIncomingMessages.get(stateOfNode2)
								- this.getLogIncomingMessage(this.node2, stateOfNode2);

				this.bufferForUpdatingMessagesFromNode2[i] = this.node2.getLogPriorProbablility(stateOfNode2)
						+ this.potential.getLogValue(stateOfNode1, stateOfNode2)
						+ logProductOfIncomingMessages;

				i += 1;
			}
			this.logNode2ToNode1MessagesNew.put(stateOfNode1, logOfSum(this.bufferForUpdatingMessagesFromNode2));
		}
	}

	/**
	 * node1 -> node2
	 */
	public void updateMessagesNode1ToNode2() {
		TObjectDoubleMap<STATES_OF_NODE_1> node1StateToLogPriorProbabilityAndProductIncomingMessages =
				this.node1.getStateToLogPriorProbabilityAndProductIncomingMessages();

		for (STATES_OF_NODE_2 stateOfNode2 : this.node2.getStates()) {
			int i = 0;
			for (STATES_OF_NODE_1 stateOfNode1 : this.node1.getStates()) {

				double logProductOfIncomingMessages =
						node1StateToLogPriorProbabilityAndProductIncomingMessages.get(stateOfNode1)
								- this.getLogIncomingMessage(this.node1, stateOfNode1);

				this.bufferForUpdatingMessagesFromNode1[i] = this.node1.getLogPriorProbablility(stateOfNode1)
						+ this.potential.getLogValue(stateOfNode1, stateOfNode2)
						+ logProductOfIncomingMessages;

				i += 1;
			}
			this.logNode1ToNode2MessagesNew.put(stateOfNode2, logOfSum(this.bufferForUpdatingMessagesFromNode1));
		}
	}

	public void refreshMessages() {
		this.refreshMessagesNode1ToNode2();
		this.refreshMessagesNode2ToNode1();
	}

	/**
	 * node2 -> node1
	 */
	public void refreshMessagesNode2ToNode1() {
		double sum2 = logOfSum(this.logNode2ToNode1MessagesNew.values());
		for (STATES_OF_NODE_1 stateOfNode1 : this.logNode2ToNode1MessagesNew.keySet()) {
			this.logNode2ToNode1Messages.put(stateOfNode1, this.logNode2ToNode1MessagesNew.get(stateOfNode1) - sum2);
		}
	}

	/**
	 * node1 -> node2
	 */
	public void refreshMessagesNode1ToNode2() {
		double sum1 = logOfSum(this.logNode1ToNode2MessagesNew.values());
		for (STATES_OF_NODE_2 stateOfNode2 : this.logNode1ToNode2MessagesNew.keySet()) {
			this.logNode1ToNode2Messages.put(stateOfNode2, this.logNode1ToNode2MessagesNew.get(stateOfNode2) - sum1);
		}
	}

	/**
	 * Given: log(X1), log(X2), ... log(Xn) <br/>
	 * Returns: log(X1 + X2 + ... + Xn)
	 */
	public static double logOfSum(double[] arrLogs) {
		Double maxLog = arrLogs[0];
		for (int i = 1; i < arrLogs.length; i++) {
			maxLog = Math.max(arrLogs[i], maxLog);
		}

		double sumExp = 0.0;
		for (int i = 0; i < arrLogs.length; i++) {
			sumExp += Math.exp(arrLogs[i] - maxLog);
		}

		return maxLog + Math.log(sumExp);
	}

	public Potential<STATES_OF_NODE_1, STATES_OF_NODE_2> getPotential() {
		return this.potential;
	}
}
