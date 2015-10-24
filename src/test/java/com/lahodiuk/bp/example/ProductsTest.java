package com.lahodiuk.bp.example;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.example.Products.Product;
import com.lahodiuk.bp.example.Products.ProductStates;
import com.lahodiuk.bp.example.Products.User;
import com.lahodiuk.bp.example.Products.UserStates;

public class ProductsTest {

	private Map<Integer, User> userIdToUser;

	private Map<Integer, Product> productIdToProduct;

	private List<Edge<UserStates, ProductStates>> edges;

	@Before
	public void init() {
		this.userIdToUser = Products.initializeUserIdsToUsers();
		this.productIdToProduct = Products.initializeProductIdsToProducts();
		this.edges = Products.initializeVotes(this.userIdToUser, this.productIdToProduct);
	}

	@Test
	public void test() {
		Products.inferenceOfUserAndProductStates(this.edges);

		assertEquals(UserStates.HONEST, this.userIdToUser.get(1).getMostProbableState());
		assertEquals(UserStates.HONEST, this.userIdToUser.get(2).getMostProbableState());
		assertEquals(UserStates.HONEST, this.userIdToUser.get(3).getMostProbableState());
		assertEquals(UserStates.HONEST, this.userIdToUser.get(4).getMostProbableState());
		assertEquals(UserStates.FRAUD, this.userIdToUser.get(5).getMostProbableState());
		assertEquals(UserStates.FRAUD, this.userIdToUser.get(6).getMostProbableState());

		assertEquals(ProductStates.GOOD, this.productIdToProduct.get(1).getMostProbableState());
		assertEquals(ProductStates.GOOD, this.productIdToProduct.get(2).getMostProbableState());
		assertEquals(ProductStates.BAD, this.productIdToProduct.get(3).getMostProbableState());
		assertEquals(ProductStates.BAD, this.productIdToProduct.get(4).getMostProbableState());
	}
}