package com.lahodiuk.bp.example;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

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
public class ColoringTest {

	private Map<Integer, GraphColorNode> nodeIdToNode;

	private List<Edge> edges;

	@Before
	public void init() {
		this.nodeIdToNode = new HashMap<Integer, GraphColorNode>();
		for (int i = 1; i <= 10; i++) {
			this.nodeIdToNode.put(i, new GraphColorNode());
		}
		// Make 1 node: Green
		this.nodeIdToNode.put(1, new GraphColorNode() {
			@Override
			public double getPriorProbablility(String state) {
				if (state == GREEN) {
					return 0.99;
				}
				return 0.005;
			}
		});
		// Make 2 node: Red
		this.nodeIdToNode.put(2, new GraphColorNode() {
			@Override
			public double getPriorProbablility(String state) {
				if (state == RED) {
					return 0.99;
				}
				return 0.005;
			}
		});

		Potential potential = new GraphColorPotential();

		this.edges = new ArrayList<>();

		this.edges.add(Edge.connect(this.nodeIdToNode.get(1), this.nodeIdToNode.get(3), null, potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(1), this.nodeIdToNode.get(4), null, potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(1), this.nodeIdToNode.get(10), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(2), this.nodeIdToNode.get(5), null, potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(2), this.nodeIdToNode.get(4), null, potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(2), this.nodeIdToNode.get(9), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(3), this.nodeIdToNode.get(5), null, potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(3), this.nodeIdToNode.get(8), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(4), this.nodeIdToNode.get(7), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(5), this.nodeIdToNode.get(6), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(6), this.nodeIdToNode.get(10), null, potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(6), this.nodeIdToNode.get(7), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(7), this.nodeIdToNode.get(8), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(8), this.nodeIdToNode.get(9), null, potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(9), this.nodeIdToNode.get(10), null, potential));
	}

	@Test
	public void test() {
		this.inference();

		/**
		 * Verify constraint: all connected nodes has different color <br/>
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
		 */

		assertFalse(this.nodeIdToNode.get(1).getMostProbableState().equals(this.nodeIdToNode.get(3).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(1).getMostProbableState().equals(this.nodeIdToNode.get(4).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(1).getMostProbableState().equals(this.nodeIdToNode.get(10).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(2).getMostProbableState().equals(this.nodeIdToNode.get(5).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(2).getMostProbableState().equals(this.nodeIdToNode.get(4).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(2).getMostProbableState().equals(this.nodeIdToNode.get(9).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(3).getMostProbableState().equals(this.nodeIdToNode.get(1).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(3).getMostProbableState().equals(this.nodeIdToNode.get(5).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(3).getMostProbableState().equals(this.nodeIdToNode.get(8).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(4).getMostProbableState().equals(this.nodeIdToNode.get(2).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(4).getMostProbableState().equals(this.nodeIdToNode.get(1).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(4).getMostProbableState().equals(this.nodeIdToNode.get(7).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(5).getMostProbableState().equals(this.nodeIdToNode.get(2).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(5).getMostProbableState().equals(this.nodeIdToNode.get(3).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(5).getMostProbableState().equals(this.nodeIdToNode.get(6).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(6).getMostProbableState().equals(this.nodeIdToNode.get(5).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(6).getMostProbableState().equals(this.nodeIdToNode.get(10).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(6).getMostProbableState().equals(this.nodeIdToNode.get(7).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(7).getMostProbableState().equals(this.nodeIdToNode.get(4).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(7).getMostProbableState().equals(this.nodeIdToNode.get(6).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(7).getMostProbableState().equals(this.nodeIdToNode.get(8).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(8).getMostProbableState().equals(this.nodeIdToNode.get(3).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(8).getMostProbableState().equals(this.nodeIdToNode.get(7).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(8).getMostProbableState().equals(this.nodeIdToNode.get(9).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(9).getMostProbableState().equals(this.nodeIdToNode.get(2).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(9).getMostProbableState().equals(this.nodeIdToNode.get(8).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(9).getMostProbableState().equals(this.nodeIdToNode.get(10).getMostProbableState()));

		assertFalse(this.nodeIdToNode.get(10).getMostProbableState().equals(this.nodeIdToNode.get(1).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(10).getMostProbableState().equals(this.nodeIdToNode.get(6).getMostProbableState()));
		assertFalse(this.nodeIdToNode.get(10).getMostProbableState().equals(this.nodeIdToNode.get(9).getMostProbableState()));
	}

	private void inference() {
		for (int i = 0; i < 10; i++) {
			for (Edge e : this.edges) {
				e.updateMessages();
			}
			for (Edge e : this.edges) {
				e.refreshMessages();
			}
		}
	}

	private static class GraphColorNode extends Node {

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

	private static class GraphColorPotential extends Potential {

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
}