package com.lahodiuk.bp.example;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.example.Coloring.Color;
import com.lahodiuk.bp.example.Coloring.GraphColorNode;

/**
 * Coloring of Petersen graph <br/>
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
		this.nodeIdToNode = Coloring.initializeNodesOfPetersenGraph();
		this.edges = Coloring.initializeEdgesOfPetersenGraph(this.nodeIdToNode);
	}

	@Test
	public void test() {
		Coloring.inferenceOfMostProbableColorsOfNodes(this.edges);

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
}