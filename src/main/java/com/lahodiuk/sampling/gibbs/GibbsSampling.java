package com.lahodiuk.sampling.gibbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class GibbsSampling {

	public abstract int getDimension();

	public abstract List<Object> getPossibleValuesOfRandomVariable(int idx);

	public abstract double conditionalProbability(Object valueOfRandomVariable, int idx, List<Object> vector);

	public List<List<Object>> doSampling(int warmUpCount, int samplesCount, Random rnd) {
		List<List<Object>> samples = new ArrayList<>();

		PersistentTreeList<Object> vector = this.getInitialVector(rnd);
		for (int s = 0; s < (samplesCount + warmUpCount); s++) {
			for (int idx = 0; idx < this.getDimension(); idx++) {
				Map<Object, Double> conditionalProbabilityDistribution = this.calculateConditionalProbabilityDistribution(vector, idx);
				while (true) {
					Object newValueOfRandomVariable = this.getPossibleValueOfRandomVariable(rnd, idx);
					if (rnd.nextDouble() < conditionalProbabilityDistribution.get(newValueOfRandomVariable)) {
						PersistentTreeList<Object> newVector = vector.updateItem(idx, newValueOfRandomVariable);
						vector = newVector;
						break;
					}
				}
			}
			if (s > warmUpCount) {
				samples.add(vector);
			}
		}

		return samples;
	}

	public Map<Object, Double> calculateConditionalProbabilityDistribution(List<Object> vector, int idx) {
		List<Object> possibleValuesOfRandomVariable = this.getPossibleValuesOfRandomVariable(idx);
		Map<Object, Double> conditionalProbabilityDistribution = new HashMap<>();
		double sum = 0;
		for (Object o : possibleValuesOfRandomVariable) {
			double probability = this.conditionalProbability(o, idx, vector);
			conditionalProbabilityDistribution.put(o, probability);
			sum += probability;
		}
		for (Object o : possibleValuesOfRandomVariable) {
			conditionalProbabilityDistribution.put(o, conditionalProbabilityDistribution.get(o) / sum);
		}
		return conditionalProbabilityDistribution;
	}

	private PersistentTreeList<Object> getInitialVector(Random rnd) {
		List<Object> vector = new ArrayList<>(this.getDimension());
		for (int idx = 0; idx < this.getDimension(); idx++) {
			Object val = this.getPossibleValueOfRandomVariable(rnd, idx);
			vector.add(val);
		}
		return new PersistentTreeList<>(vector);
	}

	private Object getPossibleValueOfRandomVariable(Random rnd, int idx) {
		List<Object> possibleValuesOfRandomVariable = this.getPossibleValuesOfRandomVariable(idx);
		return possibleValuesOfRandomVariable.get(rnd.nextInt(possibleValuesOfRandomVariable.size()));
	}
}