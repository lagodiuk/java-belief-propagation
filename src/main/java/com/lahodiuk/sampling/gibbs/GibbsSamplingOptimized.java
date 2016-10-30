package com.lahodiuk.sampling.gibbs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class GibbsSamplingOptimized {

	public abstract int getDimension();

	public abstract int getAmountofValuesOfRandomVariable(int idx);

	public abstract double conditionalProbability(int indexOfValueOfRandomVariable, int idx, int[] vector);

	public List<int[]> doSampling(int warmUpCount, int samplesCount, Random rnd) {
		List<int[]> samples = new ArrayList<>();

		int maxCntOfRandVarValues = this.getMaxCntOfRndVarValues();
		double[] conditionalProbabilityDistribution = new double[maxCntOfRandVarValues];

		int[] vector = this.getInitialVector(rnd);
		for (int s = 0; s <= (samplesCount + warmUpCount); s++) {
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
}