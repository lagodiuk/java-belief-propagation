package com.lahodiuk.bp;

public interface Potential {

	double getValue(String node1State, String node2State, String edgeType);

}
