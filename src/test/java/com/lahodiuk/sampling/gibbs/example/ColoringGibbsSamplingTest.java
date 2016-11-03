package com.lahodiuk.sampling.gibbs.example;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.example.Coloring;
import com.lahodiuk.bp.example.Coloring.Color;
import com.lahodiuk.bp.example.Coloring.GraphColorNode;
import com.lahodiuk.sampling.gibbs.GibbsSamplingMRF;

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
public class ColoringGibbsSamplingTest {

	private Map<Integer, GraphColorNode> nodeIdToNode;

	private List<Edge<Color, Color>> edges;

	@Before
	public void init() {
		this.nodeIdToNode = Coloring.initializeNodesOfPetersenGraph();
		this.edges = Coloring.initializeEdgesOfPetersenGraph(this.nodeIdToNode);
	}

	@Test
	public void test() {

		GibbsSamplingMRF gibbsSampling = new GibbsSamplingMRF(
				this.edges, this.nodeIdToNode.values());

		gibbsSampling.infer(200, 1001, new Random(1));

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

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(1)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(3)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(1)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(4)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(1)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(10)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(2)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(5)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(2)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(4)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(2)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(9)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(3)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(1)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(3)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(5)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(3)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(8)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(4)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(2)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(4)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(1)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(4)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(7)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(5)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(2)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(5)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(3)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(5)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(6)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(6)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(5)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(6)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(10)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(6)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(7)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(7)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(4)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(7)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(6)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(7)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(8)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(8)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(3)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(8)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(7)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(8)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(9)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(9)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(2)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(9)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(8)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(9)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(10)));

		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(10)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(1)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(10)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(6)));
		assertTrue(gibbsSampling.getMostProbableState(this.nodeIdToNode.get(10)) != gibbsSampling.getMostProbableState(this.nodeIdToNode.get(9)));
	}
}