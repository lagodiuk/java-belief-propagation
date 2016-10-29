package com.lahodiuk.bp;

public abstract class Potential<STATES_OF_NODE_1, STATES_OF_NODE_2> {

	public abstract double getValue(STATES_OF_NODE_1 node1State, STATES_OF_NODE_2 node2State);

	public double getLogValue(STATES_OF_NODE_1 node1State, STATES_OF_NODE_2 node2State) {
		return Math.log(this.getValue(node1State, node2State));
	}

	@SuppressWarnings("unchecked")
	public double getValueNoTypeCheck(Object node1State, Object node2State) {
		return this.getValue((STATES_OF_NODE_1) node1State, (STATES_OF_NODE_2) node2State);
	}
}
