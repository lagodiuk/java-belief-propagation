package com.lahodiuk.sampling.gibbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

public class GibbsSamplingMRF {

	public static void main(String[] args) {
		Map<Integer, User> userIdToUser =
				initializeUserIdsToUsers();

		Map<Integer, Product> productIdToProduct =
				initializeProductIdsToProducts();

		List<Edge<UserStates, ProductStates>> edges =
				initializeVotes(userIdToUser, productIdToProduct);

		List<Node<?>> nodes = new ArrayList<>(userIdToUser.values());
		nodes.addAll(new ArrayList<>(productIdToProduct.values()));
		infer(nodes);
	}

	public static List<Edge<UserStates, ProductStates>> initializeVotes(Map<Integer, User> userIdToUser, Map<Integer, Product> productIdToProduct) {

		Potential<UserStates, ProductStates> positiveVotePotential = new UserProductPositiveVotePotential();
		Potential<UserStates, ProductStates> negativeVotePotential = new UserProductNegativeVotePotential();

		List<Edge<UserStates, ProductStates>> edges = new ArrayList<>();

		edges.add(Edge.connect(userIdToUser.get(1), productIdToProduct.get(1), positiveVotePotential));
		edges.add(Edge.connect(userIdToUser.get(1), productIdToProduct.get(3), negativeVotePotential));

		edges.add(Edge.connect(userIdToUser.get(2), productIdToProduct.get(1), positiveVotePotential));
		edges.add(Edge.connect(userIdToUser.get(2), productIdToProduct.get(2), positiveVotePotential));
		edges.add(Edge.connect(userIdToUser.get(2), productIdToProduct.get(4), negativeVotePotential));

		edges.add(Edge.connect(userIdToUser.get(3), productIdToProduct.get(1), positiveVotePotential));
		edges.add(Edge.connect(userIdToUser.get(3), productIdToProduct.get(2), positiveVotePotential));
		edges.add(Edge.connect(userIdToUser.get(3), productIdToProduct.get(3), negativeVotePotential));

		edges.add(Edge.connect(userIdToUser.get(4), productIdToProduct.get(2), positiveVotePotential));

		edges.add(Edge.connect(userIdToUser.get(5), productIdToProduct.get(1), negativeVotePotential));
		edges.add(Edge.connect(userIdToUser.get(5), productIdToProduct.get(3), positiveVotePotential));

		edges.add(Edge.connect(userIdToUser.get(6), productIdToProduct.get(2), positiveVotePotential));
		edges.add(Edge.connect(userIdToUser.get(6), productIdToProduct.get(3), positiveVotePotential));
		edges.add(Edge.connect(userIdToUser.get(6), productIdToProduct.get(4), positiveVotePotential));

		return edges;
	}

	public static Map<Integer, Product> initializeProductIdsToProducts() {
		Map<Integer, Product> productIdToProduct = new HashMap<>();

		for (int i = 1; i <= 4; i++) {
			productIdToProduct.put(i, new Product());
		}

		return productIdToProduct;
	}

	public static Map<Integer, User> initializeUserIdsToUsers() {
		Map<Integer, User> userIdToUser = new HashMap<>();

		for (int i = 1; i <= 6; i++) {
			userIdToUser.put(i, new User());
		}

		return userIdToUser;
	}

	public enum UserStates {
		HONEST,
		FRAUD
	}

	public enum ProductStates {
		GOOD,
		BAD
	}

	private static final double EPSILON = 0.1;

	public static class User extends Node<UserStates> {

		private static final Set<UserStates> USER_STATES =
				new HashSet<>(Arrays.asList(UserStates.values()));

		@Override
		public Set<UserStates> getStates() {
			return USER_STATES;
		}

		@Override
		public double getPriorProbablility(UserStates state) {
			return 1.0 / USER_STATES.size();
		}
	}

	public static class Product extends Node<ProductStates> {

		private static final Set<ProductStates> PRODUCT_STATES =
				new HashSet<>(Arrays.asList(ProductStates.values()));

		@Override
		public Set<ProductStates> getStates() {
			return PRODUCT_STATES;
		}

		@Override
		public double getPriorProbablility(ProductStates state) {
			return 1.0 / PRODUCT_STATES.size();
		}
	}

	public static class UserProductPositiveVotePotential extends Potential<UserStates, ProductStates> {

		private static final double[][] POTENTIAL_VALUE =
				new double[UserStates.values().length][ProductStates.values().length];

		static {
			POTENTIAL_VALUE[UserStates.HONEST.ordinal()][ProductStates.GOOD.ordinal()] = 1 - EPSILON;
			POTENTIAL_VALUE[UserStates.HONEST.ordinal()][ProductStates.BAD.ordinal()] = EPSILON;
			POTENTIAL_VALUE[UserStates.FRAUD.ordinal()][ProductStates.GOOD.ordinal()] = 2 * EPSILON;
			POTENTIAL_VALUE[UserStates.FRAUD.ordinal()][ProductStates.BAD.ordinal()] = 1 - (2 * EPSILON);
		}

		@Override
		public double getValue(UserStates userState, ProductStates productState) {
			return POTENTIAL_VALUE[userState.ordinal()][productState.ordinal()];
		}
	}

	public static class UserProductNegativeVotePotential extends Potential<UserStates, ProductStates> {

		private static final double[][] POTENTIAL_VALUE =
				new double[UserStates.values().length][ProductStates.values().length];

		static {
			POTENTIAL_VALUE[UserStates.HONEST.ordinal()][ProductStates.GOOD.ordinal()] = EPSILON;
			POTENTIAL_VALUE[UserStates.HONEST.ordinal()][ProductStates.BAD.ordinal()] = 1 - EPSILON;
			POTENTIAL_VALUE[UserStates.FRAUD.ordinal()][ProductStates.GOOD.ordinal()] = 1 - (2 * EPSILON);
			POTENTIAL_VALUE[UserStates.FRAUD.ordinal()][ProductStates.BAD.ordinal()] = 2 * EPSILON;
		}

		@Override
		public double getValue(UserStates userState, ProductStates productState) {
			return POTENTIAL_VALUE[userState.ordinal()][productState.ordinal()];
		}
	}

	// ///////////////////////

	public static void infer(final List<Node<?>> nodes) {
		final Map<Node<?>, Integer> node2Idx = new HashMap<>();
		for (int i = 0; i < nodes.size(); i++) {
			node2Idx.put(nodes.get(i), i);
		}
		GibbsSamplingOptimized gso = new GibbsSamplingOptimized() {

			@Override
			public int getDimension() {
				return nodes.size();
			}

			@Override
			public int getAmountofValuesOfRandomVariable(int idx) {
				return nodes.get(idx).getStates().size();
			}

			@Override
			public double conditionalProbability(int indexOfValueOfRandomVariable, int idx, int[] vector) {
				Node<?> curr = nodes.get(idx);
				Object currState = new ArrayList<>(curr.getStates()).get(indexOfValueOfRandomVariable);
				double result = 1;
				for (Edge<?, ?> e : curr.getEdges()) {
					if (curr == e.getNode1()) {
						Node<?> other = e.getNode2();
						Object node2State = new ArrayList<>(other.getStates()).get(vector[node2Idx.get(other)]);
						result *= e.getPotential().getValueNoTypeCheck(currState, node2State);
					} else {
						Node<?> other = e.getNode1();
						Object node1State = new ArrayList<>(other.getStates()).get(vector[node2Idx.get(other)]);
						result *= e.getPotential().getValueNoTypeCheck(node1State, currState);
					}
				}
				return result;
			}
		};
		List<int[]> samples = gso.doSampling(100, 1000, new Random(1));
		for (int[] v : samples) {
			System.out.println(Arrays.toString(v));
		}

		List<TreeMap<Integer, Integer>> counts = new ArrayList<>();
		for (int i = 0; i < samples.iterator().next().length; i++) {
			counts.add(new TreeMap<>());
		}
		for (int[] v : samples) {
			for (int i = 0; i < v.length; i++) {
				counts.get(i).put(v[i], counts.get(i).getOrDefault(v[i], 0) + 1);
			}
		}
		System.out.println(counts);

		for (int i = 0; i < nodes.size(); i++) {
			Node<?> curr = nodes.get(i);
			List<?> states = new ArrayList<>(curr.getStates());
			System.out.print(curr.getClass().getSimpleName() + " ");
			for (int randomVariableIdx : counts.get(i).keySet()) {
				System.out.print(states.get(randomVariableIdx) + ":" + counts.get(i).get(randomVariableIdx) + " ");
			}
			System.out.println();
		}
	}

}
