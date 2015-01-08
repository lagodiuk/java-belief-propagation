package com.lahodiuk.bp.example;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

import com.lahodiuk.bp.example.HammingCodeNetwork.HammingCode;

public class HammingCodeNetworkTest {

	@Test
	public void test() {
		for (int b1 = 0; b1 < 2; b1++) {
			for (int b2 = 0; b2 < 2; b2++) {
				for (int b3 = 0; b3 < 2; b3++) {
					for (int b4 = 0; b4 < 2; b4++) {

						int[] encoded = HammingCodeNetwork.encode(b1, b2, b3, b4);
						// encoded transmitted without errors
						int[] transmitted = encoded;
						assertArrayEquals(encoded, this.inferenceMostProbableCode(transmitted));

						for (int i = 0; i < encoded.length; i++) {
							// transmit encoded bitstring and flip bit in
							// specific position
							transmitted = this.transmitAndFlipBit(encoded, i);
							assertArrayEquals(encoded, this.inferenceMostProbableCode(transmitted));
						}
					}
				}
			}
		}
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
