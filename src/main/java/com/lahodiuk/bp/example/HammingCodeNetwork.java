package com.lahodiuk.bp.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

/**
 * Correcting errors for Hamming (7, 4) code, using Loopy Belief Propagation
 */
public class HammingCodeNetwork {

	public static void main(String[] args) {

		int[] data = new int[] { 1, 0, 0, 1 };
		System.out.println("Data: " + Arrays.toString(data));

		int[] encoded = encode(data[0], data[1], data[2], data[3]);
		System.out.println("Encoded: " + Arrays.toString(encoded));

		int[] transmitted = Arrays.copyOf(encoded, encoded.length);
		// flip bit
		int flipPosition = new Random(System.currentTimeMillis()).nextInt(encoded.length);
		transmitted[flipPosition] = 1 - transmitted[flipPosition];
		System.out.println("Transmitted: " + Arrays.toString(transmitted));

		HammingCode hammingCode = new HammingCode(
				transmitted[0],
				transmitted[1],
				transmitted[2],
				transmitted[3],
				transmitted[4],
				transmitted[5],
				transmitted[6]);

		hammingCode.inference();

		hammingCode.displayPosteriorProbabilities();

		int[] corrected = hammingCode.getMostProbableCode();
		System.out.println("Corrected: " + Arrays.toString(corrected));
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

		public static Collection<BitNodeState> STATES =
				Arrays.asList(values());
	}

	private static class FactorNodeState {

		private BitNodeState[] bitStates;

		private FactorNodeState(
				BitNodeState b0,
				BitNodeState b1,
				BitNodeState b2,
				BitNodeState b3) {

			this.bitStates = new BitNodeState[] {
					b0, b1, b2, b3
			};
		}

		public BitNodeState[] getBitStates() {
			return this.bitStates;
		}

		private static Collection<FactorNodeState> STATES;

		static {
			FactorNodeState.STATES = new ArrayList<>();

			for (BitNodeState b0 : BitNodeState.STATES) {
				for (BitNodeState b1 : BitNodeState.STATES) {
					for (BitNodeState b2 : BitNodeState.STATES) {
						for (BitNodeState b3 : BitNodeState.STATES) {

							FactorNodeState.STATES.add(
									new FactorNodeState(b0, b1, b2, b3));
						}
					}
				}
			}
		}
	}

	private static class BitNode extends Node<BitNodeState> {

		private BitNodeState initialState;

		public BitNode(int value) {
			if (value == 0) {
				this.initialState = BitNodeState.ZERO;
			} else {
				this.initialState = BitNodeState.ONE;
			}
		}

		@Override
		public Iterable<BitNodeState> getStates() {
			return BitNodeState.STATES;
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

	private static class FactorNode extends Node<FactorNodeState> {

		@Override
		public Iterable<FactorNodeState> getStates() {
			return FactorNodeState.STATES;
		}

		@Override
		public double getPriorProbablility(FactorNodeState state) {
			BitNodeState[] bitStates = state.getBitStates();

			boolean xor = bitStates[0].getValue()
					^ bitStates[1].getValue()
					^ bitStates[2].getValue()
					^ bitStates[3].getValue();

			if (!xor) {
				return 0.99;
			} else {
				return 0.01;
			}
		}
	}

	private static class BitNodeCheckNodePotential extends Potential<BitNodeState, FactorNodeState> {

		private int position;

		public BitNodeCheckNodePotential(int position) {
			this.position = position;
		}

		@Override
		public double getValue(BitNodeState bitState, FactorNodeState checkNodeState) {
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
		private FactorNode[] factorNodes = new FactorNode[3];

		private List<Edge<?, ?>> edges = new ArrayList<>();

		public HammingCode(
				int dataBit1,
				int dataBit2,
				int dataBit3,
				int dataBit4,
				int parityBit1,
				int parityBit2,
				int parityBit3) {

			this.dataNodes[0] = new BitNode(dataBit1);
			this.dataNodes[1] = new BitNode(dataBit2);
			this.dataNodes[2] = new BitNode(dataBit3);
			this.dataNodes[3] = new BitNode(dataBit4);

			this.parityNodes[0] = new BitNode(parityBit1);
			this.parityNodes[1] = new BitNode(parityBit2);
			this.parityNodes[2] = new BitNode(parityBit3);

			this.factorNodes[0] = new FactorNode();
			this.factorNodes[1] = new FactorNode();
			this.factorNodes[2] = new FactorNode();

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
			System.out.println();
			for (int i = 0; i < this.dataNodes.length; i++) {
				System.out.println("Data bit " + i + "\t" + this.dataNodes[i].getPosteriorProbabilities());
			}
			for (int i = 0; i < this.parityNodes.length; i++) {
				System.out.println("Parity bit " + i + "\t" + this.parityNodes[i].getPosteriorProbabilities());
			}
			System.out.println();
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

	/**
	 * Encoding 4-vector, by appending 3 parity bits: <br/>
	 * <br/>
	 * Input: <br/>
	 * data_vector = (i1 i2 i3 i4) <br/>
	 * <br/>
	 * Using: <br/>
	 * GENERATOR_MATRIX = <br/>
	 * 1 0 0 0 1 0 1 <br/>
	 * 0 1 0 0 1 1 1 <br/>
	 * 0 0 1 0 1 1 0 <br/>
	 * 0 0 0 1 0 1 1 <br/>
	 * <br/>
	 * Result: <br/>
	 * encoded_vector = data_vector * GENERATOR_MATRIX = (i1 i2 i3 i4 r1 r2 r3) <br/>
	 * <br/>
	 * i1, i2, i3, i4 - data bits <br/>
	 * r1, r2, r3 - parity bits <br/>
	 * <br/>
	 * Parity bits satisfies following conditions: <br/>
	 * i1 xor i2 xor i3 xor r1 == 0 <br/>
	 * i2 xor i3 xor i4 xor r2 == 0 <br/>
	 * i1 xor i2 xor i4 xor r3 == 0 <br/>
	 *
	 * @return array [i1, i2, i3, i4, r1, r2, r3]
	 */
	public static int[] encode(
			int dataBit1,
			int dataBit2,
			int dataBit3,
			int dataBit4) {

		int[] input = { dataBit1, dataBit2, dataBit3, dataBit4 };

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

	private static final int[][] GENERATOR_MATRIX = {
			{ 1, 0, 0, 0, 1, 0, 1 },
			{ 0, 1, 0, 0, 1, 1, 1 },
			{ 0, 0, 1, 0, 1, 1, 0 },
			{ 0, 0, 0, 1, 0, 1, 1 }
	};
}
