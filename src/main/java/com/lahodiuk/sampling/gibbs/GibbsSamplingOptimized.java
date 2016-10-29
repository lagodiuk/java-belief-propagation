package com.lahodiuk.sampling.gibbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class GibbsSamplingOptimized {

	public abstract int getDimension();

	public abstract int getAmountofValuesOfRandomVariable(int idx);

	public abstract double conditionalProbability(int indexOfValueOfRandomVariable, int idx, int[] vector);

	public List<int[]> doSampling(int warmUpCount, int samplesCount, Random rnd) {
		List<int[]> samples = new ArrayList<>();

		int maxCntOfRandVarValues = this.getMaxCntOfRndVarValues();
		double[] conditionalProbabilityDistribution = new double[maxCntOfRandVarValues];

		int[] vector = this.getInitialVector(rnd);
		for (int s = 0; s < (samplesCount + warmUpCount); s++) {
			if (s == 8) {
				System.out.println();
			}
			for (int idx = 0; idx < this.getDimension(); idx++) {
				int rndVariableValuesCnt = this.getAmountofValuesOfRandomVariable(idx);
				this.calcCondProbDistr(vector, idx, rndVariableValuesCnt, conditionalProbabilityDistribution);
				int randomVariableValueIdx = this.sampleFromCondProbDistr(rnd, rndVariableValuesCnt, conditionalProbabilityDistribution);
				vector[idx] = randomVariableValueIdx;
			}
			if (s > warmUpCount) {
				samples.add(vector.clone());
			}
		}

		return samples;
	}

	public int getMaxCntOfRndVarValues() {
		int maxCntOfRandVarValues = -1;
		for (int i = 0; i < this.getDimension(); i++) {
			maxCntOfRandVarValues = Math.max(maxCntOfRandVarValues, this.getAmountofValuesOfRandomVariable(i));
		}
		return maxCntOfRandVarValues;
	}

	public int sampleFromCondProbDistr(Random rnd, int rndVariableValuesCnt, double[] conditionalProbabilityDistribution) {
		while (true) {
			int randomVariableValueIdx = rnd.nextInt(rndVariableValuesCnt);
			double nextDouble = rnd.nextDouble();
			if (nextDouble < conditionalProbabilityDistribution[randomVariableValueIdx]) {
				return randomVariableValueIdx;
			}
		}
	}

	public void calcCondProbDistr(int[] vector, int idx, int rndVariableValuesCnt, double[] conditionalProbabilityDistribution) {
		double sum = 0;
		for (int i = 0; i < rndVariableValuesCnt; i++) {
			double probability = this.conditionalProbability(i, idx, vector);
			conditionalProbabilityDistribution[i] = probability;
			sum += probability;
		}
		for (int i = 0; i < rndVariableValuesCnt; i++) {
			conditionalProbabilityDistribution[i] /= sum;
		}
	}

	private int[] getInitialVector(Random rnd) {
		int[] vector = new int[this.getDimension()];
		for (int idx = 0; idx < this.getDimension(); idx++) {
			vector[idx] = rnd.nextInt(this.getAmountofValuesOfRandomVariable(idx));
		}
		return vector;
	}

	// // Example

	public static void main(String... args) {
		Random rnd = new Random(1);

		Map<Integer, Double> sumCnt2 = directSampling(rnd);

		Map<Integer, Double> sumCnt = gibbsSampling(rnd);
		System.out.println(sumCnt);
		System.out.println(sumCnt2);

		Map<Integer, Double> diff = new TreeMap<>();
		for (Integer key : sumCnt.keySet()) {
			diff.put(key, sumCnt.get(key) - sumCnt2.get(key));
		}
		System.out.println(diff);
	}

	public static Map<Integer, Double> gibbsSampling(Random rnd) {
		GibbsSamplingOptimized gs = new GibbsSamplingOptimized() {
			@Override
			public int getDimension() {
				return 2;
			}

			@Override
			public int getAmountofValuesOfRandomVariable(int idx) {
				switch (idx) {
				case 0:
					return 6;
				case 1:
					return 11;

				default:
					throw new RuntimeException();
				}
			}

			@Override
			public double conditionalProbability(int indexOfValueOfRandomVariable, int idx, int[] vector) {
				switch (idx) {
				case 0: {
					int first = indexOfValueOfRandomVariable + 1;
					int sum = vector[1] + 2;
					if (((sum - first) <= 0) || ((sum - first) > 6)) {
						return 0;
					}
					return ((sum - 6) > 0) ? (1.0 / 6) : (1.0 / (sum - 1));
				}
				case 1: {
					int first = vector[0] + 1;
					int sum = indexOfValueOfRandomVariable + 2;
					if (((sum - first) <= 0) || ((sum - first) > 6)) {
						return 0;
					}
					return 1.0 / 6;
				}
				default:
					throw new RuntimeException();
				}
			}
		};

		List<int[]> samples = gs.doSampling(400, 1500, rnd);
		Set<Integer> s = new TreeSet<>();
		Map<Integer, Double> sumCnt = new TreeMap<>();
		for (int[] v : samples) {
			System.out.println(Arrays.toString(v));
			Integer first = v[0] + 1;
			Integer sum = v[1] + 2;
			s.add(sum - first);
			sumCnt.put(sum, sumCnt.getOrDefault(sum, 0.0) + 1);
		}
		System.out.println(s);

		double normalization_const = 0;
		for (Double d : sumCnt.values()) {
			normalization_const += d;
		}
		for (Integer k : sumCnt.keySet()) {
			sumCnt.put(k, sumCnt.get(k) / normalization_const);
		}
		return sumCnt;
	}

	public static Map<Integer, Double> directSampling(Random rnd) {
		Map<Integer, Double> sumCnt2 = new TreeMap<>();
		for (int i = 0; i < 1500; i++) {
			int first = rnd.nextInt(6) + 1;
			int sum = first + rnd.nextInt(6) + 1;
			sumCnt2.put(sum, sumCnt2.getOrDefault(sum, 0.0) + 1);
		}
		double normalization_const2 = 0;
		for (Double d : sumCnt2.values()) {
			normalization_const2 += d;
		}
		for (Integer k : sumCnt2.keySet()) {
			sumCnt2.put(k, sumCnt2.get(k) / normalization_const2);
		}
		return sumCnt2;
	}
}