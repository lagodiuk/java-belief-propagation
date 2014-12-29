package com.lahodiuk.bp;

public abstract class Potential {

	public abstract double getValue(String node1State, String node2State, String edgeType);

	public double getLogValue(String node1State, String node2State, String edgeType) {
		return Math.log(this.getValue(node1State, node2State, edgeType));
	}

}
