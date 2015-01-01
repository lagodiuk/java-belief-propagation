package com.lahodiuk.bp.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;
import com.lahodiuk.bp.Potential;

/**
 * Classify chemical compounds, using network of chemical reactions: <br/>
 * Inference of chemical compound types, using network of chemical reactions
 */
public class ChemicalReactionsNetwork {

	public static void main(String[] args) {
		ReactionsNetwork reactionsNetwork = new ReactionsNetwork()
				.setPriorCompoundState("Na2O", CompoundNode.BASIC_OXIDE)
				.addReaction(new Reaction().reagents("Na2O", "H2O").products("NaOH"))
				.addReaction(new Reaction().reagents("K2O", "H2O").products("KOH"))
				.addReaction(new Reaction().reagents("SO3", "H2O").products("H2SO4"))
				.addReaction(new Reaction().reagents("NaOH", "H2SO4").products("Na2SO4", "H2O"))
				.addReaction(new Reaction().reagents("KOH", "H2SO4").products("K2SO4", "H2O"))
				.addReaction(new Reaction().reagents("CO2", "H2O").products("H2CO3"))
				.addReaction(new Reaction().reagents("NaOH", "H2CO3").products("Na2CO3", "H2O"))
				.addReaction(new Reaction().reagents("LiOH", "H2SO4").products("Li2SO4", "H2O"))
				.addReaction(new Reaction().reagents("Li2O", "H2O").products("LiOH"))
				.addReaction(new Reaction().reagents("KOH", "H2SO4").products("KHSO4", "H2O"))
				.addReaction(new Reaction().reagents("KHSO4", "KOH").products("K2SO4", "H2O"))
				.inference(10);

		reactionsNetwork.result();
	}

	private static class Reaction {
		private Set<String> reagents = new HashSet<>();
		private Set<String> products = new HashSet<>();

		public Reaction reagents(String... reagents) {
			for (String r : reagents) {
				this.reagents.add(r);
			}
			return this;
		}

		public Reaction products(String... products) {
			for (String p : products) {
				this.products.add(p);
			}
			return this;
		}

		public Set<String> getProducts() {
			return this.products;
		}

		public Set<String> getReagents() {
			return this.reagents;
		}
	}

	private static class ReactionsNetwork {

		private Map<Reaction, ReactionNode> reactionToReactionNode = new HashMap<>();

		private Map<String, CompoundNode> compoundToCompoundNode = new TreeMap<>();

		private List<Edge<String, String>> edges = new ArrayList<>();

		public ReactionsNetwork addReaction(Reaction reaction) {
			this.reactionToReactionNode.put(reaction, new ReactionNode());

			for (String reagent : reaction.getReagents()) {
				if (this.compoundToCompoundNode.get(reagent) == null) {
					this.compoundToCompoundNode.put(reagent, new CompoundNode());
				}
			}

			for (String product : reaction.getProducts()) {
				if (this.compoundToCompoundNode.get(product) == null) {
					this.compoundToCompoundNode.put(product, new CompoundNode());
				}
			}

			return this;
		}

		public ReactionsNetwork setPriorCompoundState(String compound, String priorState) {
			this.compoundToCompoundNode.put(compound, new CompoundNode(priorState));
			return this;
		}

		private void buildNetwork() {
			Potential<String, String> reagentReactionPotential = new ReagentReactionCompatibilityPotential();
			Potential<String, String> productReactionPotential = new ProductReactionCompatibilityPotential();
			Potential<String, String> differentCompoundsPotential = new DifferentCompoundsCompatibilityPotential();

			for (Reaction reaction : this.reactionToReactionNode.keySet()) {
				ReactionNode reactionNode = this.reactionToReactionNode.get(reaction);

				Set<String> reagents = reaction.getReagents();
				Set<String> products = reaction.getProducts();

				for (String reagent : reagents) {
					CompoundNode reagentNode = this.compoundToCompoundNode.get(reagent);
					this.edges.add(Edge.connect(reagentNode, reactionNode, reagentReactionPotential));
				}

				for (String product : products) {
					CompoundNode productNode = this.compoundToCompoundNode.get(product);
					this.edges.add(Edge.connect(productNode, reactionNode, productReactionPotential));
				}

				for (String reagent1 : new ArrayList<>(reagents)) {
					for (String reagent2 : new ArrayList<>(reagents)) {
						if (reagent1 == reagent2) {
							continue;
						}

						CompoundNode reagentNode1 = this.compoundToCompoundNode.get(reagent1);
						CompoundNode reagentNode2 = this.compoundToCompoundNode.get(reagent2);

						this.edges.add(Edge.connect(reagentNode1, reagentNode2, differentCompoundsPotential));
					}
				}

				for (String product1 : new ArrayList<>(products)) {
					for (String product2 : new ArrayList<>(products)) {
						if (product1 == product2) {
							continue;
						}

						CompoundNode productNode1 = this.compoundToCompoundNode.get(product1);
						CompoundNode productNode2 = this.compoundToCompoundNode.get(product2);

						this.edges.add(Edge.connect(productNode1, productNode2, differentCompoundsPotential));
					}
				}
			}
		}

		public ReactionsNetwork inference(int times) {
			this.buildNetwork();

			for (int i = 0; i < times; i++) {
				for (Edge<String, String> edge : this.edges) {
					edge.updateMessagesNode1ToNode2();
				}
				for (Edge<String, String> edge : this.edges) {
					edge.refreshMessagesNode1ToNode2();
				}
				for (Edge<String, String> edge : this.edges) {
					edge.updateMessagesNode2ToNode1();
				}
				for (Edge<String, String> edge : this.edges) {
					edge.refreshMessagesNode2ToNode1();
				}
			}
			return this;
		}

		public void result() {
			for (String compound : this.compoundToCompoundNode.keySet()) {
				System.out.println(compound + "\t" + this.compoundToCompoundNode.get(compound).getPosteriorProbabilities());
				System.out.println(compound + "\t" + this.compoundToCompoundNode.get(compound).getMostProbableState());
				System.out.println();
			}
		}
	}

	private static class CompoundNode extends Node<String> {

		public static final String WATER = "Water";
		public static final String BASIC_OXIDE = "Basic Oxide";
		public static final String BASE = "Base";
		public static final String ACIDIC_OXIDE = "Acidic oxide";
		public static final String ACID = "Acid";
		public static final String SALT = "Salt";

		private static final double EPSILON = 1e-5;

		private static final Set<String> STATES = new HashSet<>();

		private String mostProbableState;

		static {
			STATES.add(WATER);
			STATES.add(BASIC_OXIDE);
			STATES.add(BASE);
			STATES.add(ACIDIC_OXIDE);
			STATES.add(ACID);
			STATES.add(SALT);
		}

		public CompoundNode() {
		}

		public CompoundNode(String mostProbableState) {
			this.mostProbableState = mostProbableState;
		}

		@Override
		public Set<String> getStates() {
			return STATES;
		}

		@Override
		public double getPriorProbablility(String state) {
			if (this.mostProbableState == null) {
				return 1.0 / STATES.size();
			} else {
				if (state == this.mostProbableState) {
					return 1.0 - EPSILON;
				} else {
					return EPSILON;
				}
			}
		}
	}

	private static class ReactionNode extends Node<String> {

		public static final String BASIC_OXIDE_AND_WATER = "Basic Oxide + Water = Base";
		public static final String ACIDIC_OXIDE_AND_WATER = "Acidic Oxide + Water = Acid";
		public static final String BASIC_OXIDE_AND_ACIDIC_OXIDE = "Basic Oxide + Acidic Oxide = Salt";
		public static final String BASE_AND_ACID = "Base + Acid = Salt + Water";
		public static final String BASE_AND_ACIDIC_OXIDE = "Base + Acidic Oxide = Salt + Water";
		public static final String BASIC_OXIDE_AND_ACID = "Basic Oxide + Acid = Salt + Water";

		private static final Set<String> STATES = new HashSet<>();

		private static final Map<Integer, Set<String>> REAGENTS_COUNT_TO_STATES = new HashMap<>();
		private static final Map<Integer, Set<String>> PRODUCTS_COUNT_TO_STATES = new HashMap<>();

		private static final double EPSILON = 1e-5;

		static {
			STATES.add(BASIC_OXIDE_AND_WATER);
			STATES.add(ACIDIC_OXIDE_AND_WATER);
			STATES.add(BASIC_OXIDE_AND_ACIDIC_OXIDE);
			STATES.add(BASE_AND_ACID);
			STATES.add(BASE_AND_ACIDIC_OXIDE);
			STATES.add(BASIC_OXIDE_AND_ACID);

			REAGENTS_COUNT_TO_STATES.put(2, STATES);

			PRODUCTS_COUNT_TO_STATES.put(1, new HashSet<>(Arrays.asList(BASIC_OXIDE_AND_WATER, ACIDIC_OXIDE_AND_WATER, BASIC_OXIDE_AND_ACIDIC_OXIDE)));
			PRODUCTS_COUNT_TO_STATES.put(2, new HashSet<>(Arrays.asList(BASE_AND_ACID, BASE_AND_ACIDIC_OXIDE, BASIC_OXIDE_AND_ACID)));
		}

		private Set<String> statesMatchedByProductsAndReagentsCount = null;

		@Override
		public Set<String> getStates() {
			return STATES;
		}

		@Override
		public double getPriorProbablility(String state) {
			if (this.statesMatchedByProductsAndReagentsCount == null) {
				this.getMostProbableStatesByReagentsAndProductsCount();
			}

			if (this.statesMatchedByProductsAndReagentsCount.isEmpty()) {
				return 1.0 / STATES.size();
			} else {
				if (this.statesMatchedByProductsAndReagentsCount.contains(state)) {
					return 1.0 / this.statesMatchedByProductsAndReagentsCount.size();
				} else {
					return EPSILON;
				}
			}
		}

		public void getMostProbableStatesByReagentsAndProductsCount() {
			int reagentsCount = 0;
			int productsCount = 0;
			for (Edge<?, ?> edge : this.getEdges()) {
				if (edge.getPotential() instanceof ReagentReactionCompatibilityPotential) {
					reagentsCount++;
					continue;
				}

				if (edge.getPotential() instanceof ProductReactionCompatibilityPotential) {
					productsCount++;
					continue;
				}

				throw new RuntimeException();
			}

			Set<String> mostProbableStatesByReagentsCount = REAGENTS_COUNT_TO_STATES.get(reagentsCount);
			Set<String> mostProbableStatesByProductsCount = PRODUCTS_COUNT_TO_STATES.get(productsCount);

			this.statesMatchedByProductsAndReagentsCount = new HashSet<>();

			if ((mostProbableStatesByProductsCount != null) && (mostProbableStatesByReagentsCount != null)) {
				for (String s : mostProbableStatesByProductsCount) {
					if (mostProbableStatesByReagentsCount.contains(s)) {
						this.statesMatchedByProductsAndReagentsCount.add(s);
					}
				}
			}
		}
	}

	private static class ReagentReactionCompatibilityPotential extends Potential<String, String> {

		private static final double EPSILON = 1e-5;

		@Override
		public double getValue(String compoundState, String reactionState) {
			if (reactionState == ReactionNode.BASIC_OXIDE_AND_WATER) {
				if ((compoundState == CompoundNode.BASIC_OXIDE) || (compoundState == CompoundNode.WATER)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.ACIDIC_OXIDE_AND_WATER) {
				if ((compoundState == CompoundNode.ACIDIC_OXIDE) || (compoundState == CompoundNode.WATER)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASIC_OXIDE_AND_ACIDIC_OXIDE) {
				if ((compoundState == CompoundNode.ACIDIC_OXIDE) || (compoundState == CompoundNode.BASIC_OXIDE)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASE_AND_ACID) {
				if ((compoundState == CompoundNode.BASE) || (compoundState == CompoundNode.ACID)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASE_AND_ACIDIC_OXIDE) {
				if ((compoundState == CompoundNode.BASE) || (compoundState == CompoundNode.ACIDIC_OXIDE)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASIC_OXIDE_AND_ACID) {
				if ((compoundState == CompoundNode.BASIC_OXIDE) || (compoundState == CompoundNode.ACID)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			throw new RuntimeException();
		}
	}

	private static class ProductReactionCompatibilityPotential extends Potential<String, String> {

		private static final double EPSILON = 1e-5;

		@Override
		public double getValue(String compoundState, String reactionState) {
			if (reactionState == ReactionNode.BASIC_OXIDE_AND_WATER) {
				if (compoundState == CompoundNode.BASE) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.ACIDIC_OXIDE_AND_WATER) {
				if (compoundState == CompoundNode.ACID) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASIC_OXIDE_AND_ACIDIC_OXIDE) {
				if (compoundState == CompoundNode.SALT) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASE_AND_ACID) {
				if ((compoundState == CompoundNode.SALT) || (compoundState == CompoundNode.WATER)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASE_AND_ACIDIC_OXIDE) {
				if ((compoundState == CompoundNode.SALT) || (compoundState == CompoundNode.WATER)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			if (reactionState == ReactionNode.BASIC_OXIDE_AND_ACID) {
				if ((compoundState == CompoundNode.SALT) || (compoundState == CompoundNode.WATER)) {
					return 1.0 - EPSILON;
				}
				return EPSILON;
			}

			throw new RuntimeException();
		}
	}

	private static class DifferentCompoundsCompatibilityPotential extends Potential<String, String> {

		private static final double EPSILON = 1e-5;

		@Override
		public double getValue(String compound1State, String compound2State) {
			if (compound1State != compound2State) {
				return 1.0 - EPSILON;
			}
			return EPSILON;
		}
	}
}
