package com.lahodiuk.bp.example;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Potential;
import com.lahodiuk.bp.example.Coloring.Color;
import com.lahodiuk.bp.example.Coloring.GraphColorNode;
import com.lahodiuk.bp.example.Coloring.GraphColorPotential;

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

	private List<Edge<Color, Color>> edges;

	@Before
	public void init() {
		this.nodeIdToNode = new HashMap<Integer, GraphColorNode>();

		// Make 1 node: Green
		this.nodeIdToNode.put(1, new GraphColorNode() {
			@Override
			public double getPriorProbablility(Color state) {
				if (state == Color.GREEN) {
					return 0.99;
				}
				return 0.005;
			}
		});
		// Make 2 node: Red
		this.nodeIdToNode.put(2, new GraphColorNode() {
			@Override
			public double getPriorProbablility(Color state) {
				if (state == Color.RED) {
					return 0.99;
				}
				return 0.005;
			}
		});

		for (int i = 3; i <= 10; i++) {
			this.nodeIdToNode.put(i, new GraphColorNode());
		}

		Potential<Color, Color> potential = new GraphColorPotential();

		this.edges = new ArrayList<>();

		this.edges.add(Edge.connect(this.nodeIdToNode.get(1), this.nodeIdToNode.get(3), potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(1), this.nodeIdToNode.get(4), potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(1), this.nodeIdToNode.get(10), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(2), this.nodeIdToNode.get(5), potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(2), this.nodeIdToNode.get(4), potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(2), this.nodeIdToNode.get(9), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(3), this.nodeIdToNode.get(5), potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(3), this.nodeIdToNode.get(8), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(4), this.nodeIdToNode.get(7), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(5), this.nodeIdToNode.get(6), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(6), this.nodeIdToNode.get(10), potential));
		this.edges.add(Edge.connect(this.nodeIdToNode.get(6), this.nodeIdToNode.get(7), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(7), this.nodeIdToNode.get(8), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(8), this.nodeIdToNode.get(9), potential));

		this.edges.add(Edge.connect(this.nodeIdToNode.get(9), this.nodeIdToNode.get(10), potential));
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

		assertTrue(this.nodeIdToNode.get(1).getMostProbableState() != this.nodeIdToNode.get(3).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(1).getMostProbableState() != this.nodeIdToNode.get(4).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(1).getMostProbableState() != this.nodeIdToNode.get(10).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(2).getMostProbableState() != this.nodeIdToNode.get(5).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(2).getMostProbableState() != this.nodeIdToNode.get(4).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(2).getMostProbableState() != this.nodeIdToNode.get(9).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(3).getMostProbableState() != this.nodeIdToNode.get(1).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(3).getMostProbableState() != this.nodeIdToNode.get(5).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(3).getMostProbableState() != this.nodeIdToNode.get(8).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(4).getMostProbableState() != this.nodeIdToNode.get(2).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(4).getMostProbableState() != this.nodeIdToNode.get(1).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(4).getMostProbableState() != this.nodeIdToNode.get(7).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(5).getMostProbableState() != this.nodeIdToNode.get(2).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(5).getMostProbableState() != this.nodeIdToNode.get(3).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(5).getMostProbableState() != this.nodeIdToNode.get(6).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(6).getMostProbableState() != this.nodeIdToNode.get(5).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(6).getMostProbableState() != this.nodeIdToNode.get(10).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(6).getMostProbableState() != this.nodeIdToNode.get(7).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(7).getMostProbableState() != this.nodeIdToNode.get(4).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(7).getMostProbableState() != this.nodeIdToNode.get(6).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(7).getMostProbableState() != this.nodeIdToNode.get(8).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(8).getMostProbableState() != this.nodeIdToNode.get(3).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(8).getMostProbableState() != this.nodeIdToNode.get(7).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(8).getMostProbableState() != this.nodeIdToNode.get(9).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(9).getMostProbableState() != this.nodeIdToNode.get(2).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(9).getMostProbableState() != this.nodeIdToNode.get(8).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(9).getMostProbableState() != this.nodeIdToNode.get(10).getMostProbableState());

		assertTrue(this.nodeIdToNode.get(10).getMostProbableState() != this.nodeIdToNode.get(1).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(10).getMostProbableState() != this.nodeIdToNode.get(6).getMostProbableState());
		assertTrue(this.nodeIdToNode.get(10).getMostProbableState() != this.nodeIdToNode.get(9).getMostProbableState());
	}

	private void inference() {
		for (int i = 0; i < 10; i++) {
			for (Edge<Color, Color> e : this.edges) {
				e.updateMessages();
			}
			for (Edge<Color, Color> e : this.edges) {
				e.refreshMessages();
			}
		}
	}
}