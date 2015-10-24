package com.lahodiuk.bp.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

public class Products {

	public static void main(String[] args) {
		Map<Integer, User> userIdToUser =
				initializeUserIdsToUsers();

		Map<Integer, Product> productIdToProduct =
				initializeProductIdsToProducts();

		List<Edge<UserStates, ProductStates>> edges =
				initializeVotes(userIdToUser, productIdToProduct);

		inferenceOfUserAndProductStates(edges);

		displayInferredProbabilitiesOfStates(userIdToUser, productIdToProduct);
	}

	public static void displayInferredProbabilitiesOfStates(Map<Integer, User> userIdToUser, Map<Integer, Product> productIdToProduct) {
		for (int i = 1; i <= 6; i++) {
			System.out.println("User: " + i + "\t" + userIdToUser.get(i).getPosteriorProbabilities());
		}

		System.out.println();

		for (int i = 1; i <= 4; i++) {
			System.out.println("Product: " + i + "\t" + productIdToProduct.get(i).getPosteriorProbabilities());
		}
	}

	public static void inferenceOfUserAndProductStates(List<Edge<UserStates, ProductStates>> edges) {
		int iterationsNumber = 100;
		for (int i = 0; i < iterationsNumber; i++) {
			// Scheduling schema, which described in article:
			// 1) User -> Product
			// 2) User <- Product

			// User -> Product
			for (Edge<UserStates, ProductStates> edge : edges) {
				edge.updateMessagesNode1ToNode2();
			}
			for (Edge<UserStates, ProductStates> edge : edges) {
				edge.refreshMessagesNode1ToNode2();
			}

			// User <- Product
			for (Edge<UserStates, ProductStates> edge : edges) {
				edge.updateMessagesNode2ToNode1();
			}
			for (Edge<UserStates, ProductStates> edge : edges) {
				edge.refreshMessagesNode2ToNode1();
			}

			// Alternative scheduling schema:
			// Simultaneous passing of messages:
			// User -> Product
			// User <- Product
			//
			// for (Edge edge : edges) {
			// edge.updateMessagesNode1ToNode2();
			// edge.updateMessagesNode2ToNode1();
			// }
			// for (Edge edge : edges) {
			// edge.refreshMessagesNode1ToNode2();
			// edge.refreshMessagesNode2ToNode1();
			// }
		}
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
}