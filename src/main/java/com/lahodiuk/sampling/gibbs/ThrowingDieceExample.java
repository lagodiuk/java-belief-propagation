package com.lahodiuk.sampling.gibbs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ThrowingDieceExample {
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
		GibbsSampling gs = new GibbsSampling() {
			@Override
			public int getDimension() {
				return 2;
			}

			@Override
			public List<Object> getPossibleValuesOfRandomVariable(int idx) {
				switch (idx) {
				case 0:
					return Arrays.asList(1, 2, 3, 4, 5, 6);
				case 1:
					return Arrays.asList(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);

				default:
					throw new RuntimeException();
				}
			}

			@Override
			public double conditionalProbability(Object valueOfRandomVariable, int idx, List<Object> vector) {
				switch (idx) {
				case 0: {
					int sum = (Integer) vector.get(1);
					int first = (Integer) valueOfRandomVariable;
					if (((sum - first) <= 0) || ((sum - first) > 6)) {
						return 0;
					}
					return ((sum - 6) > 0) ? (1.0 / 6) : (1.0 / (sum - 1));
				}
				case 1: {
					int first = (Integer) vector.get(0);
					int sum = (Integer) valueOfRandomVariable;
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

		List<List<Object>> samples = gs.doSampling(400, 1500, rnd);
		Set<Integer> s = new TreeSet<>();
		Map<Integer, Double> sumCnt = new TreeMap<>();
		for (List<Object> v : samples) {
			System.out.println(v);
			Integer sum = (Integer) v.get(1);
			Integer first = (Integer) v.get(0);
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
