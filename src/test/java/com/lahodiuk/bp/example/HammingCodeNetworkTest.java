package com.lahodiuk.bp.example;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.lahodiuk.bp.example.HammingCodeNetwork.HammingCode;

public class HammingCodeNetworkTest {

	private static final int[] BIT_VALUES = new int[] { 0, 1 };

	@Test
	public void test() {
		for (int dataBit1 : BIT_VALUES) {
			for (int dataBit2 : BIT_VALUES) {
				for (int dataBit3 : BIT_VALUES) {
					for (int dataBit4 : BIT_VALUES) {

						int[] encoded = HammingCodeNetwork.encode(
								dataBit1, dataBit2, dataBit3, dataBit4);

						// encoded transmitted without errors
						int[] transmitted = encoded;

						int[] decoded = this.inferenceMostProbableCode(transmitted);

						assertEquals(this.payload(encoded, 4), this.payload(decoded, 4));

						for (int flipPosition = 0; flipPosition < encoded.length; flipPosition++) {
							// transmit encoded bitstring and flip bit in
							// specific position
							transmitted = this.transmitAndFlipBit(encoded, flipPosition);

							decoded = this.inferenceMostProbableCode(transmitted);

							// TODO: check comment inside method
							// com.lahodiuk.bp.Node.getPosteriorProbabilities()
							//
							// Assert.assertArrayEquals(encoded, decoded);
							assertEquals(this.payload(encoded, 4), this.payload(decoded, 4));
						}
					}
				}
			}
		}
	}

	private String payload(int[] arr, int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(arr[i]);
		}
		return sb.toString();
	}

	public int[] transmitAndFlipBit(int[] encoded, int flippedBitPosition) {
		int[] transmitted = Arrays.copyOf(encoded, encoded.length);
		// flip bit
		transmitted[flippedBitPosition] = 1 - transmitted[flippedBitPosition];
		return transmitted;
	}

	public int[] inferenceMostProbableCode(int[] transmitted) {
		HammingCode hammingCode = new HammingCode(
				transmitted[0], // data bit
				transmitted[1], // data bit
				transmitted[2], // data bit
				transmitted[3], // data bit
				transmitted[4], // parity bit
				transmitted[5], // parity bit
				transmitted[6]);// parity bit
		hammingCode.inference();
		int[] mostProbableCode = hammingCode.getMostProbableCode();
		return mostProbableCode;
	}

}
