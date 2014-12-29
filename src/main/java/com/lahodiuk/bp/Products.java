package com.lahodiuk.bp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Products {

	public static void main(String[] args) {
		Potential potential = new UserProductPotential();

		Map<Integer, User> userIdToUser = new HashMap<>();
		for (int i = 1; i <= 6; i++) {
			userIdToUser.put(i, new User());
		}

		Map<Integer, Product> productIdToProduct = new HashMap<>();
		for (int i = 1; i <= 4; i++) {
			productIdToProduct.put(i, new Product());
		}

		List<Edge> edges = new ArrayList<>();
		edges.add(Edge.connect(userIdToUser.get(1), productIdToProduct.get(1), UserProductPotential.POSITIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(1), productIdToProduct.get(3), UserProductPotential.NEGATIVE_VOTE, potential));

		edges.add(Edge.connect(userIdToUser.get(2), productIdToProduct.get(1), UserProductPotential.POSITIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(2), productIdToProduct.get(2), UserProductPotential.POSITIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(2), productIdToProduct.get(4), UserProductPotential.NEGATIVE_VOTE, potential));

		edges.add(Edge.connect(userIdToUser.get(3), productIdToProduct.get(1), UserProductPotential.POSITIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(3), productIdToProduct.get(2), UserProductPotential.POSITIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(3), productIdToProduct.get(3), UserProductPotential.NEGATIVE_VOTE, potential));

		edges.add(Edge.connect(userIdToUser.get(4), productIdToProduct.get(2), UserProductPotential.POSITIVE_VOTE, potential));

		edges.add(Edge.connect(userIdToUser.get(5), productIdToProduct.get(1), UserProductPotential.NEGATIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(5), productIdToProduct.get(3), UserProductPotential.POSITIVE_VOTE, potential));

		edges.add(Edge.connect(userIdToUser.get(6), productIdToProduct.get(2), UserProductPotential.POSITIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(6), productIdToProduct.get(3), UserProductPotential.POSITIVE_VOTE, potential));
		edges.add(Edge.connect(userIdToUser.get(6), productIdToProduct.get(4), UserProductPotential.POSITIVE_VOTE, potential));

		for (int i = 0; i < 30; i++) {
			for (Edge edge : edges) {
				edge.updateMessages();
			}
			for (Edge edge : edges) {
				edge.refreshMessages();
			}
		}

		for (int i = 1; i <= 6; i++) {
			System.out.println("User: " + i + "\t" + userIdToUser.get(i).getPosteriorProbabilities());
		}
		System.out.println();
		for (int i = 1; i <= 4; i++) {
			System.out.println("Product: " + i + "\t" + productIdToProduct.get(i).getPosteriorProbabilities());
		}
	}
}

class User extends Node {

	public static final String FRAUD = "fraud";

	public static final String HONEST = "honest";

	private static final Set<String> USER_STATES = new HashSet<>();

	static {
		USER_STATES.add(HONEST);
		USER_STATES.add(FRAUD);
	}

	@Override
	public Set<String> getStates() {
		return USER_STATES;
	}

	@Override
	public double getPriorProbablility(String state) {
		return 1.0 / USER_STATES.size();
	}
}

class Product extends Node {

	public static final String GOOD = "good";

	public static final String BAD = "bad";

	private static final Set<String> PRODUCT_STATES = new HashSet<>();

	static {
		PRODUCT_STATES.add(GOOD);
		PRODUCT_STATES.add(BAD);
	}

	@Override
	public Set<String> getStates() {
		return PRODUCT_STATES;
	}

	@Override
	public double getPriorProbablility(String state) {
		return 1.0 / PRODUCT_STATES.size();
	}
}

class UserProductPotential implements Potential {

	public static String POSITIVE_VOTE = "+";

	public static String NEGATIVE_VOTE = "-";

	private static double EPSILON = 0.1;

	@Override
	public double getValue(String userState, String productState, String edgeType) {
		if (edgeType == POSITIVE_VOTE) {
			if (userState == User.HONEST) {
				if (productState == Product.GOOD) {
					return 1.0 - EPSILON;
				}

				if (productState == Product.BAD) {
					return EPSILON;
				}
			}
			if (userState == User.FRAUD) {
				if (productState == Product.GOOD) {
					return 2 * EPSILON;
				}

				if (productState == Product.BAD) {
					return 1 - (2 * EPSILON);
				}
			}
		}

		if (edgeType == NEGATIVE_VOTE) {
			if (userState == User.HONEST) {
				if (productState == Product.GOOD) {
					return EPSILON;
				}

				if (productState == Product.BAD) {
					return 1 - EPSILON;
				}
			}
			if (userState == User.FRAUD) {
				if (productState == Product.GOOD) {
					return 1 - (2 * EPSILON);
				}

				if (productState == Product.BAD) {
					return 2 * EPSILON;
				}
			}
		}

		throw new RuntimeException();
	}

}