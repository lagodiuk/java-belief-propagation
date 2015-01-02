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
		ReactionsNetwork reactionsNetwork = configureReactionsNetwork();

		inference(reactionsNetwork);

		reactionsNetwork.result();
	}

	public static void inference(ReactionsNetwork reactionsNetwork) {
		reactionsNetwork.inference(10);
	}

	public static ReactionsNetwork configureReactionsNetwork() {
		ReactionsNetwork reactionsNetwork = new ReactionsNetwork()
				.setRules(new Rule().reagentTypes(CompoundType.BASIC_OXIDE, CompoundType.WATER).productTypes(CompoundType.BASE),
						new Rule().reagentTypes(CompoundType.ACIDIC_OXIDE, CompoundType.WATER).productTypes(CompoundType.ACID),
						new Rule().reagentTypes(CompoundType.BASIC_OXIDE, CompoundType.ACIDIC_OXIDE).productTypes(CompoundType.SALT),
						new Rule().reagentTypes(CompoundType.BASE, CompoundType.ACID).productTypes(CompoundType.SALT, CompoundType.WATER),
						new Rule().reagentTypes(CompoundType.BASE, CompoundType.ACID).productTypes(CompoundType.ACID_SALT, CompoundType.WATER),
						new Rule().reagentTypes(CompoundType.ACID_SALT, CompoundType.BASE).productTypes(CompoundType.SALT, CompoundType.WATER),
						new Rule().reagentTypes(CompoundType.BASE, CompoundType.ACIDIC_OXIDE).productTypes(CompoundType.SALT, CompoundType.WATER),
						new Rule().reagentTypes(CompoundType.BASIC_OXIDE, CompoundType.ACID).productTypes(CompoundType.SALT, CompoundType.WATER))
				// Interesting: it is possible to infer chemical compound types,
				// even without prior knowledge about any of chemical compounds
				// (without "seed")
				// .setPriorCompoundState("Na2O", CompoundType.BASIC_OXIDE)
				.addReaction(new Reaction().reagents("Na2O", "H2O").products("NaOH"))
				.addReaction(new Reaction().reagents("K2O", "H2O").products("KOH"))
				.addReaction(new Reaction().reagents("SO3", "H2O").products("H2SO4"))
				.addReaction(new Reaction().reagents("NaOH", "H2SO4").products("NaHSO4", "H2O"))
				.addReaction(new Reaction().reagents("NaOH", "NaHSO4").products("Na2SO4", "H2O"))
				.addReaction(new Reaction().reagents("NaOH", "H2SO4").products("Na2SO4", "H2O"))
				.addReaction(new Reaction().reagents("KOH", "H2SO4").products("K2SO4", "H2O"))
				.addReaction(new Reaction().reagents("CO2", "H2O").products("H2CO3"))
				.addReaction(new Reaction().reagents("NaOH", "H2CO3").products("NaHCO3", "H2O"))
				.addReaction(new Reaction().reagents("NaOH", "NaHCO3").products("Na2CO3", "H2O"))
				.addReaction(new Reaction().reagents("NaOH", "H2CO3").products("Na2CO3", "H2O"))
				.addReaction(new Reaction().reagents("LiOH", "H2SO4").products("Li2SO4", "H2O"))
				.addReaction(new Reaction().reagents("LiOH", "H2SO4").products("LiHSO4", "H2O"))
				.addReaction(new Reaction().reagents("LiOH", "LiHSO4").products("Li2SO4", "H2O"))
				.addReaction(new Reaction().reagents("Li2O", "H2O").products("LiOH"))
				.addReaction(new Reaction().reagents("KOH", "H2SO4").products("KHSO4", "H2O"))
				.addReaction(new Reaction().reagents("KHSO4", "KOH").products("K2SO4", "H2O"))
				.addReaction(new Reaction().reagents("KOH", "H2CO3").products("K2CO3", "H2O"))
				.addReaction(new Reaction().reagents("KOH", "H2CO3").products("KHCO3", "H2O"))
				.addReaction(new Reaction().reagents("KOH", "KHCO3").products("K2CO3", "H2O"))
				.addReaction(new Reaction().reagents("LiOH", "H2CO3").products("LiHCO3", "H2O"))
				.addReaction(new Reaction().reagents("LiOH", "LiHCO3").products("Li2CO3", "H2O"))
				.addReaction(new Reaction().reagents("Li2O", "CO2").products("Li2CO3"));

		return reactionsNetwork;
	}

	public enum CompoundType {
		WATER,
		BASIC_OXIDE,
		ACIDIC_OXIDE,
		BASE,
		ACID,
		SALT,
		ACID_SALT
	}

	public static class Rule {
		private Set<CompoundType> reagentTypes = new HashSet<>();
		private Set<CompoundType> productTypes = new HashSet<>();

		public Rule reagentTypes(CompoundType... reagents) {
			for (CompoundType r : reagents) {
				this.reagentTypes.add(r);
			}
			return this;
		}

		public Rule productTypes(CompoundType... products) {
			for (CompoundType p : products) {
				this.productTypes.add(p);
			}
			return this;
		}

		public Set<CompoundType> getProductTypes() {
			return this.productTypes;
		}

		public Set<CompoundType> getReagentTypes() {
			return this.reagentTypes;
		}
	}

	public static class Reaction {
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

	public static class ReactionsNetwork {

		private List<Rule> rules = new ArrayList<>();

		private Map<Integer, Set<Rule>> reagentsCountToRules = new HashMap<>();

		private Map<Integer, Set<Rule>> productsCountToRules = new HashMap<>();

		private Map<Reaction, ReactionNode> reactionToReactionNode = new HashMap<>();

		private Map<String, CompoundNode> compoundToCompoundNode = new TreeMap<>();

		private List<Edge<?, ?>> edges = new ArrayList<>();

		public ReactionsNetwork addReaction(Reaction reaction) {
			this.reactionToReactionNode.put(reaction, new ReactionNode(reaction.getReagents().size(), reaction.getProducts().size(), this.rules, this.reagentsCountToRules,
					this.productsCountToRules));

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

		public ReactionsNetwork setPriorCompoundState(String compound, CompoundType priorState) {
			this.compoundToCompoundNode.put(compound, new CompoundNode(priorState));
			return this;
		}

		public ReactionsNetwork setRules(Rule... rules) {
			for (Rule rule : rules) {
				this.rules.add(rule);
			}
			for (Rule rule : rules) {
				int reagentsCount = rule.getReagentTypes().size();
				Set<Rule> rulesWithSameNumberOfReagents = this.reagentsCountToRules.get(reagentsCount);
				if (rulesWithSameNumberOfReagents == null) {
					rulesWithSameNumberOfReagents = new HashSet<>();
				}
				rulesWithSameNumberOfReagents.add(rule);
				this.reagentsCountToRules.put(reagentsCount, rulesWithSameNumberOfReagents);

				int productsCount = rule.getProductTypes().size();
				Set<Rule> rulesWithSameNumberOfProducts = this.productsCountToRules.get(productsCount);
				if (rulesWithSameNumberOfProducts == null) {
					rulesWithSameNumberOfProducts = new HashSet<>();
				}
				rulesWithSameNumberOfProducts.add(rule);
				this.productsCountToRules.put(productsCount, rulesWithSameNumberOfProducts);
			}
			return this;
		}

		private void buildNetwork() {
			Potential<CompoundType, Rule> reagentReactionPotential = new ReagentReactionCompatibilityPotential();
			Potential<CompoundType, Rule> productReactionPotential = new ProductReactionCompatibilityPotential();
			Potential<CompoundType, CompoundType> differentCompoundsPotential = new DifferentCompoundsCompatibilityPotential();

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
				for (Edge<?, ?> edge : this.edges) {
					edge.updateMessagesNode1ToNode2();
				}
				for (Edge<?, ?> edge : this.edges) {
					edge.refreshMessagesNode1ToNode2();
				}
				for (Edge<?, ?> edge : this.edges) {
					edge.updateMessagesNode2ToNode1();
				}
				for (Edge<?, ?> edge : this.edges) {
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

		public CompoundType getMostProbableCompoundType(String compound) {
			return this.compoundToCompoundNode.get(compound).getMostProbableState();
		}
	}

	private static class CompoundNode extends Node<CompoundType> {

		private static final double EPSILON = 1e-5;

		private static final List<CompoundType> STATES = Arrays.asList(CompoundType.values());

		private CompoundType mostProbableState;

		public CompoundNode() {
		}

		public CompoundNode(CompoundType mostProbableState) {
			this.mostProbableState = mostProbableState;
		}

		@Override
		public Iterable<CompoundType> getStates() {
			return STATES;
		}

		@Override
		public double getPriorProbablility(CompoundType state) {
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

	private static class ReactionNode extends Node<Rule> {

		private static final double EPSILON = 1e-5;

		private List<Rule> rules;

		private Map<Integer, Set<Rule>> reagentsCountToRules;

		private Map<Integer, Set<Rule>> productsCountToRules;

		private Set<Rule> mostProbableStatesByProductsAndReagentsCount = null;

		public ReactionNode(int reagentsCount, int productsCount, List<Rule> rules, Map<Integer, Set<Rule>> reagentsCountToRules, Map<Integer, Set<Rule>> productsCountToRules) {
			this.rules = rules;
			this.reagentsCountToRules = reagentsCountToRules;
			this.productsCountToRules = productsCountToRules;
			this.setMostProbableStatesByReagentsAndProductsCount(reagentsCount, productsCount);
		}

		@Override
		public Iterable<Rule> getStates() {
			return this.rules;
		}

		@Override
		public double getPriorProbablility(Rule state) {
			if (this.mostProbableStatesByProductsAndReagentsCount.contains(state)) {
				return 1.0 / this.mostProbableStatesByProductsAndReagentsCount.size();
			} else {
				return EPSILON;
			}
		}

		public void setMostProbableStatesByReagentsAndProductsCount(int reagentsCount, int productsCount) {
			Set<Rule> mostProbableStatesByReagentsCount = this.reagentsCountToRules.get(reagentsCount);
			Set<Rule> mostProbableStatesByProductsCount = this.productsCountToRules.get(productsCount);

			this.mostProbableStatesByProductsAndReagentsCount = new HashSet<>();

			if ((mostProbableStatesByProductsCount != null) && (mostProbableStatesByReagentsCount != null)) {
				for (Rule s : mostProbableStatesByProductsCount) {
					if (mostProbableStatesByReagentsCount.contains(s)) {
						this.mostProbableStatesByProductsAndReagentsCount.add(s);
					}
				}
			}

			if (this.mostProbableStatesByProductsAndReagentsCount.isEmpty()) {
				// TODO Warn
				System.out.println("Can't find any reactions withs given numbers of products and reagents");

				this.mostProbableStatesByProductsAndReagentsCount = new HashSet<>(this.rules);
			}
		}
	}

	private static class ReagentReactionCompatibilityPotential extends Potential<CompoundType, Rule> {

		private static final double EPSILON = 1e-5;

		@Override
		public double getValue(CompoundType compoundState, Rule reactionState) {
			if (reactionState.getReagentTypes().contains(compoundState)) {
				return 1 - EPSILON;
			} else {
				return EPSILON;
			}
		}
	}

	private static class ProductReactionCompatibilityPotential extends Potential<CompoundType, Rule> {

		private static final double EPSILON = 1e-5;

		@Override
		public double getValue(CompoundType compoundState, Rule reactionState) {
			if (reactionState.getProductTypes().contains(compoundState)) {
				return 1 - EPSILON;
			} else {
				return EPSILON;
			}
		}
	}

	private static class DifferentCompoundsCompatibilityPotential extends Potential<CompoundType, CompoundType> {

		private static final double EPSILON = 1e-5;

		@Override
		public double getValue(CompoundType compound1State, CompoundType compound2State) {
			if (compound1State != compound2State) {
				return 1.0 - EPSILON;
			}
			return EPSILON;
		}
	}
}
