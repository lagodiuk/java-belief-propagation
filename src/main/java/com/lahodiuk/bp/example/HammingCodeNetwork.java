package com.lahodiuk.bp.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

/**
 * Correcting errors for Hamming (7, 4) code, using Loopy Belief Propagation
 */
public class HammingCodeNetwork {

	public static void main(String[] args) {
		// x =
		// 1 0 0 1
		//
		// A =
		// 1 0 0 0 1 0 1
		// 0 1 0 0 1 1 1
		// 0 0 1 0 1 1 0
		// 0 0 0 1 0 1 1
		//
		// encoded = x * A = (i1 i2 i3 i4 r1 r2 r3)
		//
		// i1, i2, i3, i4 - encoded (informational) bits
		// r1, r2, r3 - checking bits
		//
		// so, encoded will be (1 0 0 1 1 1 0)
		//
		// Noisy channel:
		// Let's flip random bit: (1 0 0 1 1 1 0) -> (1 1 0 1 1 1 0)
		HammingCode hammingCode = new HammingCode(1, 1, 0, 1, 1, 1, 0);
		hammingCode.inference();
		hammingCode.displayPosteriorProbabilities();
		System.out.println(Arrays.toString(hammingCode.getMostProbableCode()));
	}

	private enum BitNodeState {

		ZERO(false),
		ONE(true);

		private boolean value;

		private BitNodeState(boolean value) {
			this.value = value;
		}

		public boolean getValue() {
			return this.value;
		}

		private static Collection<BitNodeState> STATES =
				Arrays.asList(values());

		public static Collection<BitNodeState> getStates() {
			return STATES;
		}
	}

	private static class CheckNodeState {

		private BitNodeState[] bitStates;

		private CheckNodeState(BitNodeState b0, BitNodeState b1, BitNodeState b2, BitNodeState b3) {
			this.bitStates = new BitNodeState[] { b0, b1, b2, b3 };
		}

		public BitNodeState[] getBitStates() {
			return this.bitStates;
		}

		private static Collection<CheckNodeState> STATES;

		static {
			STATES = new ArrayList<>();
			for (BitNodeState b0 : BitNodeState.getStates()) {
				for (BitNodeState b1 : BitNodeState.getStates()) {
					for (BitNodeState b2 : BitNodeState.getStates()) {
						for (BitNodeState b3 : BitNodeState.getStates()) {
							STATES.add(new CheckNodeState(b0, b1, b2, b3));
						}
					}
				}
			}
		}

		public static Collection<CheckNodeState> getStates() {
			return STATES;
		}
	}

	private static class BitNode extends Node<BitNodeState> {

		private BitNodeState initialState;

		public BitNode(int value) {
			if (value == 0) {
				this.initialState = BitNodeState.ZERO;
			}
			if (value == 1) {
				this.initialState = BitNodeState.ONE;
			}
		}

		@Override
		public Iterable<BitNodeState> getStates() {
			return BitNodeState.getStates();
		}

		@Override
		public double getPriorProbablility(BitNodeState state) {
			if (state == this.initialState) {
				return 0.9;
			} else {
				return 0.1;
			}
		}
	}

	private static class CheckNode extends Node<CheckNodeState> {

		@Override
		public Iterable<CheckNodeState> getStates() {
			return CheckNodeState.getStates();
		}

		@Override
		public double getPriorProbablility(CheckNodeState state) {
			BitNodeState[] bitStates = state.getBitStates();
			boolean xor = bitStates[0].getValue() ^ bitStates[1].getValue() ^ bitStates[2].getValue() ^ bitStates[3].getValue();
			if (!xor) {
				return 0.99;
			} else {
				return 0.01;
			}
		}
	}

	private static class BitNodeCheckNodePotential extends Potential<BitNodeState, CheckNodeState> {

		private int position;

		public BitNodeCheckNodePotential(int position) {
			this.position = position;
		}

		@Override
		public double getValue(BitNodeState bitState, CheckNodeState checkNodeState) {
			if (checkNodeState.getBitStates()[this.position] == bitState) {
				return 0.99;
			} else {
				return 0.01;
			}
		}
	}

	public static class HammingCode {

		private BitNode[] dataNodes = new BitNode[4];
		private BitNode[] parityNodes = new BitNode[3];
		private CheckNode[] factorNodes = new CheckNode[3];

		private List<Edge<?, ?>> edges = new ArrayList<>();

		public HammingCode(int dataBit1, int dataBit2, int dataBit3, int dataBit4, int parityBit1, int parityBit2, int parityBit3) {

			this.dataNodes[0] = new BitNode(dataBit1);
			this.dataNodes[1] = new BitNode(dataBit2);
			this.dataNodes[2] = new BitNode(dataBit3);
			this.dataNodes[3] = new BitNode(dataBit4);

			this.parityNodes[0] = new BitNode(parityBit1);
			this.parityNodes[1] = new BitNode(parityBit2);
			this.parityNodes[2] = new BitNode(parityBit3);

			this.factorNodes[0] = new CheckNode();
			this.factorNodes[1] = new CheckNode();
			this.factorNodes[2] = new CheckNode();

			this.edges.add(Edge.connect(this.dataNodes[0], this.factorNodes[0], new BitNodeCheckNodePotential(0)));
			this.edges.add(Edge.connect(this.dataNodes[1], this.factorNodes[0], new BitNodeCheckNodePotential(1)));
			this.edges.add(Edge.connect(this.dataNodes[2], this.factorNodes[0], new BitNodeCheckNodePotential(2)));
			this.edges.add(Edge.connect(this.parityNodes[0], this.factorNodes[0], new BitNodeCheckNodePotential(3)));

			this.edges.add(Edge.connect(this.dataNodes[1], this.factorNodes[1], new BitNodeCheckNodePotential(0)));
			this.edges.add(Edge.connect(this.dataNodes[2], this.factorNodes[1], new BitNodeCheckNodePotential(1)));
			this.edges.add(Edge.connect(this.dataNodes[3], this.factorNodes[1], new BitNodeCheckNodePotential(2)));
			this.edges.add(Edge.connect(this.parityNodes[1], this.factorNodes[1], new BitNodeCheckNodePotential(3)));

			this.edges.add(Edge.connect(this.dataNodes[0], this.factorNodes[2], new BitNodeCheckNodePotential(0)));
			this.edges.add(Edge.connect(this.dataNodes[1], this.factorNodes[2], new BitNodeCheckNodePotential(1)));
			this.edges.add(Edge.connect(this.dataNodes[3], this.factorNodes[2], new BitNodeCheckNodePotential(2)));
			this.edges.add(Edge.connect(this.parityNodes[2], this.factorNodes[2], new BitNodeCheckNodePotential(3)));
		}

		public void inference() {
			for (int i = 0; i < 10; i++) {
				for (Edge<?, ?> edge : this.edges) {
					edge.updateMessages();
				}
				for (Edge<?, ?> edge : this.edges) {
					edge.refreshMessages();
				}
			}
		}

		public void displayPosteriorProbabilities() {
			for (int i = 0; i < this.dataNodes.length; i++) {
				System.out.println("Data bit " + i + "\t" + this.dataNodes[i].getPosteriorProbabilities());
			}
			for (int i = 0; i < this.parityNodes.length; i++) {
				System.out.println("Parity bit " + i + "\t" + this.parityNodes[i].getPosteriorProbabilities());
			}
		}

		public int[] getMostProbableCode() {
			int[] result = new int[this.dataNodes.length + this.parityNodes.length];
			for (int i = 0; i < this.dataNodes.length; i++) {
				result[i] =
						this.dataNodes[i].getMostProbableState().getValue() ? 1 : 0;
			}
			for (int i = 0; i < this.parityNodes.length; i++) {
				result[this.dataNodes.length + i] =
						this.parityNodes[i].getMostProbableState().getValue() ? 1 : 0;
			}
			return result;
		}
	}

	private static final int[][] GENERATOR_MATRIX = {
			{ 1, 0, 0, 0, 1, 0, 1 },
			{ 0, 1, 0, 0, 1, 1, 1 },
			{ 0, 0, 1, 0, 1, 1, 0 },
			{ 0, 0, 0, 1, 0, 1, 1 }
	};

	public static int[] encode(int b1, int b2, int b3, int b4) {
		int[] input = { b1, b2, b3, b4 };
		int[] encoded = new int[7];
		for (int i = 0; i < 7; i++) {
			int sum = 0;
			for (int j = 0; j < 4; j++) {
				sum += input[j] * GENERATOR_MATRIX[j][i];
			}
			encoded[i] = sum % 2;
		}
		return encoded;
	}
}
