package com.lahodiuk.sampling.gibbs.example;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.example.Products;
import com.lahodiuk.bp.example.Products.Product;
import com.lahodiuk.bp.example.Products.ProductStates;
import com.lahodiuk.bp.example.Products.User;
import com.lahodiuk.bp.example.Products.UserStates;
import com.lahodiuk.sampling.gibbs.GibbsSamplingMRF;

public class ProductsGibbsSamplingTest {

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

		GibbsSamplingMRF gibbsSampling = new GibbsSamplingMRF(
				this.edges, this.userIdToUser.values(), this.productIdToProduct.values());

		gibbsSampling.infer(200, 1501, new Random(1));

		assertEquals(UserStates.HONEST, gibbsSampling.getMostProbableState(this.userIdToUser.get(1)));
		assertEquals(UserStates.HONEST, gibbsSampling.getMostProbableState(this.userIdToUser.get(2)));
		assertEquals(UserStates.HONEST, gibbsSampling.getMostProbableState(this.userIdToUser.get(3)));
		assertEquals(UserStates.HONEST, gibbsSampling.getMostProbableState(this.userIdToUser.get(4)));
		assertEquals(UserStates.FRAUD, gibbsSampling.getMostProbableState(this.userIdToUser.get(5)));
		assertEquals(UserStates.FRAUD, gibbsSampling.getMostProbableState(this.userIdToUser.get(6)));

		assertEquals(ProductStates.GOOD, gibbsSampling.getMostProbableState(this.productIdToProduct.get(1)));
		assertEquals(ProductStates.GOOD, gibbsSampling.getMostProbableState(this.productIdToProduct.get(2)));
		assertEquals(ProductStates.BAD, gibbsSampling.getMostProbableState(this.productIdToProduct.get(3)));
		assertEquals(ProductStates.BAD, gibbsSampling.getMostProbableState(this.productIdToProduct.get(4)));
	}
}