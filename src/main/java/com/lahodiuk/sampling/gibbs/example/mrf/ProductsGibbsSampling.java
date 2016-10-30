package com.lahodiuk.sampling.gibbs.example.mrf;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.example.Products;
import com.lahodiuk.bp.example.Products.Product;
import com.lahodiuk.bp.example.Products.ProductStates;
import com.lahodiuk.bp.example.Products.User;
import com.lahodiuk.bp.example.Products.UserStates;
import com.lahodiuk.sampling.gibbs.GibbsSamplingMRF;

public class ProductsGibbsSampling {

	public static void main(String... args) {
		Map<Integer, User> userIdToUser =
				Products.initializeUserIdsToUsers();

		Map<Integer, Product> productIdToProduct =
				Products.initializeProductIdsToProducts();

		List<Edge<UserStates, ProductStates>> edges =
				Products.initializeVotes(userIdToUser, productIdToProduct);

		GibbsSamplingMRF gibbsSamplingMRF = new GibbsSamplingMRF(
				edges, userIdToUser.values(), productIdToProduct.values());

		gibbsSamplingMRF.infer(300, 1000, new Random(1));

		for (int userId : userIdToUser.keySet()) {
			User user = userIdToUser.get(userId);
			System.out.println("User " + userId + ":" + gibbsSamplingMRF.getMostProbableState(user));
		}
		for (int productId : productIdToProduct.keySet()) {
			Product product = productIdToProduct.get(productId);
			System.out.println("Product " + productId + ":" + gibbsSamplingMRF.getMostProbableState(product));
		}
	}
}
