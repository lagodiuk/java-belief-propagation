package com.lahodiuk.bp.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

public class HammingCode {

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
		HammingCodeNetwork hammingCodeNetwork = new HammingCodeNetwork(new int[] { 1, 1, 0, 1, 1, 1, 0 });
		hammingCodeNetwork.inference();
		hammingCodeNetwork.displayPosteriorProbabilities();
		System.out.println(Arrays.toString(hammingCodeNetwork.getMostProbableCode()));
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

		private static Collection<BitNodeState> states = Arrays.asList(values());

		public static Collection<BitNodeState> getStates() {
			return states;
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

		private static Collection<CheckNodeState> states;

		static {
			states = new ArrayList<>();
			for (BitNodeState b0 : BitNodeState.values()) {
				for (BitNodeState b1 : BitNodeState.values()) {
					for (BitNodeState b2 : BitNodeState.values()) {
						for (BitNodeState b3 : BitNodeState.values()) {
							states.add(new CheckNodeState(b0, b1, b2, b3));
						}
					}
				}
			}
		}

		public static Collection<CheckNodeState> getStates() {
			return states;
		}
	}

	private static class BitNode extends Node<BitNodeState> {

		private BitNodeState initialState;

		public BitNode(BitNodeState initialState) {
			this.initialState = initialState;
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

	private static class HammingCodeNetwork {

		private BitNode[] bitNodes = new BitNode[7];

		private CheckNode[] checkNodes = new CheckNode[3];

		private List<Edge<?, ?>> edges = new ArrayList<>();

		public HammingCodeNetwork(int[] bits) {
			for (int i = 0; i < 7; i++) {
				if (bits[i] == 0) {
					this.bitNodes[i] = new BitNode(BitNodeState.ZERO);
				}
				if (bits[i] == 1) {
					this.bitNodes[i] = new BitNode(BitNodeState.ONE);
				}
			}

			for (int i = 0; i < 3; i++) {
				this.checkNodes[i] = new CheckNode();
			}

			this.edges.add(Edge.connect(this.bitNodes[0], this.checkNodes[0], new BitNodeCheckNodePotential(0)));
			this.edges.add(Edge.connect(this.bitNodes[1], this.checkNodes[0], new BitNodeCheckNodePotential(1)));
			this.edges.add(Edge.connect(this.bitNodes[2], this.checkNodes[0], new BitNodeCheckNodePotential(2)));
			this.edges.add(Edge.connect(this.bitNodes[4], this.checkNodes[0], new BitNodeCheckNodePotential(3)));

			this.edges.add(Edge.connect(this.bitNodes[1], this.checkNodes[1], new BitNodeCheckNodePotential(0)));
			this.edges.add(Edge.connect(this.bitNodes[2], this.checkNodes[1], new BitNodeCheckNodePotential(1)));
			this.edges.add(Edge.connect(this.bitNodes[3], this.checkNodes[1], new BitNodeCheckNodePotential(2)));
			this.edges.add(Edge.connect(this.bitNodes[5], this.checkNodes[1], new BitNodeCheckNodePotential(3)));

			this.edges.add(Edge.connect(this.bitNodes[0], this.checkNodes[2], new BitNodeCheckNodePotential(0)));
			this.edges.add(Edge.connect(this.bitNodes[1], this.checkNodes[2], new BitNodeCheckNodePotential(1)));
			this.edges.add(Edge.connect(this.bitNodes[3], this.checkNodes[2], new BitNodeCheckNodePotential(2)));
			this.edges.add(Edge.connect(this.bitNodes[6], this.checkNodes[2], new BitNodeCheckNodePotential(3)));
		}

		public void inference() {
			for (int i = 0; i < 50; i++) {
				for (Edge<?, ?> edge : this.edges) {
					edge.updateMessages();
				}
				for (Edge<?, ?> edge : this.edges) {
					edge.refreshMessages();
				}
			}
		}

		public void displayPosteriorProbabilities() {
			for (int i = 0; i < this.bitNodes.length; i++) {
				System.out.println(i + "\t" + this.bitNodes[i].getPosteriorProbabilities());
			}
		}

		public int[] getMostProbableCode() {
			int[] result = new int[this.bitNodes.length];
			for (int i = 0; i < this.bitNodes.length; i++) {
				if (this.bitNodes[i].getMostProbableState() == BitNodeState.ONE) {
					result[i] = 1;
				}
				if (this.bitNodes[i].getMostProbableState() == BitNodeState.ZERO) {
					result[i] = 0;
				}
			}
			return result;
		}
	}
}
