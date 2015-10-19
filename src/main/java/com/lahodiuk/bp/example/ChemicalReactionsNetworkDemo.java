package com.lahodiuk.bp.example;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
public class ChemicalReactionsNetworkDemo {

	public static final int ITERATIONS_NUMBER = 10;

	public static void main(String[] args) throws Exception {
		// String path = "src/main/resources/HammingCode.txt";
		String path = "src/main/resources/ChemicalReactionsNetwork.txt";

		ReactionsNetwork reactionsNetwork = configureReactionsNetwork(
				Files.readAllLines(Paths.get(path), Charset.forName("UTF-8")));

		inference(reactionsNetwork, ITERATIONS_NUMBER);

		reactionsNetwork.printResults();
	}

	public static void inference(ReactionsNetwork reactionsNetwork, int iterationsNumber) {
		reactionsNetwork.inference(iterationsNumber);
	}

	private static Rule parseRule(String input) {
		String[] parts = input.split("=");

		String reagentsPart = parts[0];
		List<String> reagents = new ArrayList<>();
		for (String reagent : reagentsPart.split("\\+")) {
			reagents.add(reagent.trim());
		}

		List<String> products = new ArrayList<>();
		if (parts.length > 1) {
			String productsPart = parts[1];
			for (String product : productsPart.split("\\+")) {
				products.add(product.trim());
			}
		}

		return new Rule().reagentTypes(reagents).productTypes(products);
	}

	private static Reaction parseReaction(String input) {
		String[] parts = input.split("=");
		String reagentsPart = parts[0];

		List<String> reagents = new ArrayList<>();
		for (String reagent : reagentsPart.split("\\+")) {
			reagents.add(reagent.trim());
		}

		List<String> products = new ArrayList<>();
		if (parts.length > 1) {
			String productsPart = parts[1];
			for (String product : productsPart.split("\\+")) {
				products.add(product.trim());
			}
		}

		return new Reaction().reagents(reagents).products(products);
	}

	private enum InputReaderState {
		NOTHING,
		READING_RULES,
		READING_COMPOUND_TYPES,
		READING_REACTIONS,
		READING_COMPOUND_TYPES_PROBABILITY
	}

	public static ReactionsNetwork configureReactionsNetwork(List<String> rawTextLines) {

		ReactionsNetwork reactionsNetwork = new ReactionsNetwork();

		InputReaderState currentState = InputReaderState.NOTHING;
		for (String s : rawTextLines) {
			if (s.isEmpty()) {
				continue;
			} else if ("#Rules".equals(s)) {
				currentState = InputReaderState.READING_RULES;
			} else if ("#CompoundTypes".equals(s)) {
				currentState = InputReaderState.READING_COMPOUND_TYPES;
			} else if ("#CompoundTypesProbability".equals(s)) {
				currentState = InputReaderState.READING_COMPOUND_TYPES_PROBABILITY;
			} else if ("#Reactions".equals(s)) {
				currentState = InputReaderState.READING_REACTIONS;
			} else if (s.startsWith("#")) {
				continue;
			} else {
				switch (currentState) {

				case READING_RULES:
					Rule rule = parseRule(s);
					reactionsNetwork.addRuleWithAllowedPermutations(rule);
					break;

				case READING_REACTIONS:
					Reaction reaction = parseReaction(s);
					reactionsNetwork.addReaction(reaction);
					break;

				case READING_COMPOUND_TYPES:
					String[] parts = s.split(":");
					String compound = parts[0].trim();
					String type = parts[1].trim();
					reactionsNetwork.setPriorCompoundState(compound, type);
					break;

				case READING_COMPOUND_TYPES_PROBABILITY:
					CompoundNode.EPSILON = 1.0 - Double.parseDouble(s);
					break;

				default:
					throw new RuntimeException("Unreachable code. Current line of text is: " + s);
				}
			}
		}

		return reactionsNetwork;
	}

	public static class Rule {
		private List<String> reagentTypes = new ArrayList<>();
		private List<String> productTypes = new ArrayList<>();

		public Rule reagentTypes(List<String> reagents) {
			this.reagentTypes.addAll(reagents);
			return this;
		}

		public Rule productTypes(List<String> products) {
			this.productTypes.addAll(products);
			return this;
		}

		public List<String> getProductTypes() {
			return this.productTypes;
		}

		public List<String> getReagentTypes() {
			return this.reagentTypes;
		}
	}

	public static class Reaction {
		private List<String> reagents = new ArrayList<>();
		private List<String> products = new ArrayList<>();

		public Reaction reagents(List<String> reagents) {
			this.reagents.addAll(reagents);
			return this;
		}

		public Reaction products(List<String> products) {
			this.products.addAll(products);
			return this;
		}

		public List<String> getProducts() {
			return this.products;
		}

		public List<String> getReagents() {
			return this.reagents;
		}
	}

	public static class ReactionsNetwork {

		private List<Rule> rules = new ArrayList<>();
		private List<Reaction> reactions = new ArrayList<>();

		private Map<Integer, Set<Rule>> reagentsCountToRules = new HashMap<>();
		private Map<Integer, Set<Rule>> productsCountToRules = new HashMap<>();

		private Map<Reaction, ReactionNode> reactionToReactionNode = new HashMap<>();
		private Map<String, CompoundNode> compoundToCompoundNode = new TreeMap<>();

		private List<Edge<?, ?>> edges = new ArrayList<>();

		public ReactionsNetwork addRule(Rule ruleVariant) {
			this.rules.add(ruleVariant);
			return this;
		}

		public ReactionsNetwork addRuleWithAllowedPermutations(Rule rule) {
			Set<List<String>> reagentsPermutations = permutations(rule.getReagentTypes());
			Set<List<String>> productsPermutations = permutations(rule.getProductTypes());

			for (List<String> reagents : reagentsPermutations) {
				for (List<String> products : productsPermutations) {
					Rule ruleVariant = new Rule().reagentTypes(reagents).productTypes(products);
					this.addRule(ruleVariant);
				}
			}

			return this;
		}

		public ReactionsNetwork addReaction(Reaction reaction) {
			this.reactions.add(reaction);
			return this;
		}

		public ReactionsNetwork setPriorCompoundState(String compound, String priorState) {
			this.compoundToCompoundNode.put(compound, new CompoundNode(priorState));
			return this;
		}

		private static <T> Set<List<T>> permutations(List<T> seq) {
			Set<List<T>> resultsCollector = new HashSet<>();
			permutations(new ArrayList<T>(seq),
					new LinkedList<>(seq),
					resultsCollector);
			return resultsCollector;
		}

		private static <T> void permutations(
				List<T> buffer,
				List<T> available,
				Set<List<T>> resultsCollector) {

			if (available.isEmpty()) {
				// No elements available
				// Store copy of buffer to results collector
				resultsCollector.add(new ArrayList<>(buffer));
			} else {
				// Calculate index of current position in buffer
				int position = buffer.size() - available.size();
				for (int i = 0; i < available.size(); i++) {
					// Pick element from list of available elements
					// (decreasing size of available variants by 1)
					T element = available.remove(i);
					// Write picked element to buffer into given position
					buffer.set(position, element);
					// Generate permutations for the rest of elements
					permutations(buffer, available, resultsCollector);
					// Return back picked element
					available.add(i, buffer.get(position));
					// And pick next element on next iteration
				}
			}
		}

		private void buildNetwork() {

			this.processRules();

			this.processReactions();

			for (Reaction reaction : this.reactionToReactionNode.keySet()) {
				ReactionNode reactionNode = this.reactionToReactionNode.get(reaction);

				List<String> reagents = reaction.getReagents();

				for (int position = 0; position < reagents.size(); position++) {
					String reagent = reagents.get(position);
					CompoundNode reagentNode = this.compoundToCompoundNode.get(reagent);
					this.edges.add(Edge.connect(reagentNode, reactionNode, new ReagentReactionCompatibilityPotential(position)));
				}

				List<String> products = reaction.getProducts();

				for (int position = 0; position < products.size(); position++) {
					String product = products.get(position);
					CompoundNode productNode = this.compoundToCompoundNode.get(product);
					this.edges.add(Edge.connect(productNode, reactionNode, new ProductReactionCompatibilityPotential(position)));
				}
			}
		}

		private void processRules() {
			for (Rule rule : this.rules) {
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
		}

		public void processReactions() {

			Set<String> possibleCompoundTypes = new HashSet<>();
			for (Rule rule : this.rules) {
				possibleCompoundTypes.addAll(rule.getReagentTypes());
				possibleCompoundTypes.addAll(rule.getProductTypes());
			}

			for (Reaction reaction : this.reactions) {
				int reagentsCount = reaction.getReagents().size();
				int productsCount = reaction.getProducts().size();

				Set<Rule> mostProbableRules = this.getMostProbableRulesByReagentsAndProductsCount(reagentsCount, productsCount);

				this.reactionToReactionNode.put(reaction, new ReactionNode(mostProbableRules));

				for (String reagent : reaction.getReagents()) {
					if (this.compoundToCompoundNode.get(reagent) == null) {
						this.compoundToCompoundNode.put(reagent, new CompoundNode(possibleCompoundTypes));
					} else {
						this.compoundToCompoundNode.get(reagent).setStates(possibleCompoundTypes);
					}
				}

				for (String product : reaction.getProducts()) {
					if (this.compoundToCompoundNode.get(product) == null) {
						this.compoundToCompoundNode.put(product, new CompoundNode(possibleCompoundTypes));
					} else {
						this.compoundToCompoundNode.get(product).setStates(possibleCompoundTypes);
					}
				}
			}
		}

		private Set<Rule> getMostProbableRulesByReagentsAndProductsCount(int reagentsCount, int productsCount) {
			Set<Rule> mostProbableStatesByReagentsCount = this.reagentsCountToRules.get(reagentsCount);
			Set<Rule> mostProbableStatesByProductsCount = this.productsCountToRules.get(productsCount);

			Set<Rule> mostProbableRules = new HashSet<>();

			// Intersection of sets
			if ((mostProbableStatesByProductsCount != null) && (mostProbableStatesByReagentsCount != null)) {
				for (Rule s : mostProbableStatesByProductsCount) {
					if (mostProbableStatesByReagentsCount.contains(s)) {
						mostProbableRules.add(s);
					}
				}
			}

			if (mostProbableRules.isEmpty()) {
				// throw new
				// RuntimeException("Can't find any reactions withs given numbers of products and reagents");
				mostProbableRules = new HashSet<>(this.rules);
			}

			return mostProbableRules;
		}

		public ReactionsNetwork inference(int times) {
			this.buildNetwork();
			System.out.println("Compound nodes: " + this.compoundToCompoundNode.size());
			System.out.println("Reaction nodes: " + this.reactionToReactionNode.size());

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

		public void printResults() {
			for (String compound : this.compoundToCompoundNode.keySet()) {
				// System.out.println(compound + "\t" +
				// this.compoundToCompoundNode.get(compound).getPosteriorProbabilities());
				System.out.println(compound + "\t" +
						this.compoundToCompoundNode.get(compound).getMostProbableState()
						+ "\t"
						+
						this.compoundToCompoundNode.get(compound).getPosteriorProbabilities().get(this.compoundToCompoundNode.get(compound).getMostProbableState())
						+ "\tCount: " +
						this.compoundToCompoundNode.get(compound).getEdges().size());
				// System.out.println(compound + "\t" +
				// this.compoundToCompoundNode.get(compound).getMostProbableState());
			}
			System.out.println("Total number of compounds: " + this.compoundToCompoundNode.keySet().size());
		}

		public String getMostProbableCompoundType(String compound) {
			return this.compoundToCompoundNode.get(compound).getMostProbableState();
		}
	}

	private static class CompoundNode extends Node<String> {

		private static double EPSILON = 1e-15;

		private Set<String> states;

		private String mostProbableState;

		public CompoundNode(String mostProbableState) {
			this.mostProbableState = mostProbableState;
		}

		public CompoundNode(Set<String> states) {
			this.states = states;
		}

		public void setStates(Set<String> states) {
			this.states = states;
		}

		@Override
		public Set<String> getStates() {
			return this.states;
		}

		@Override
		public double getPriorProbablility(String state) {
			if (this.mostProbableState == null) {
				return 1.0 / this.states.size();
			} else {
				if (this.mostProbableState.equals(state)) {
					return 1.0 - EPSILON;
				} else {
					return EPSILON;
				}
			}
		}
	}

	private static class ReactionNode extends Node<Rule> {

		private static final double EPSILON = 1e-5;

		private Set<Rule> mostProbableStates = null;

		public ReactionNode(Set<Rule> mostProbableStates) {
			this.mostProbableStates = mostProbableStates;
		}

		@Override
		public Set<Rule> getStates() {
			return this.mostProbableStates;
		}

		@Override
		public double getPriorProbablility(Rule state) {
			if (this.mostProbableStates.contains(state)) {
				return 1.0 / this.mostProbableStates.size();
			} else {
				return EPSILON;
			}
		}
	}

	private static class ReagentReactionCompatibilityPotential extends Potential<String, Rule> {

		private static final double EPSILON = 1e-5;

		private final int position;

		public ReagentReactionCompatibilityPotential(int position) {
			this.position = position;
		}

		@Override
		public double getValue(String compoundState, Rule reactionState) {
			if ((this.position < reactionState.getReagentTypes().size()) &&
					(reactionState.getReagentTypes().get(this.position).equals(compoundState))) {
				return 1 - EPSILON;
			} else {
				return EPSILON;
			}
		}
	}

	private static class ProductReactionCompatibilityPotential extends Potential<String, Rule> {

		private static final double EPSILON = 1e-5;

		private final int position;

		public ProductReactionCompatibilityPotential(int position) {
			this.position = position;
		}

		@Override
		public double getValue(String compoundState, Rule reactionState) {
			if ((this.position < reactionState.getProductTypes().size()) &&
					(reactionState.getProductTypes().get(this.position).equals(compoundState))) {
				return 1 - EPSILON;
			} else {
				return EPSILON;
			}
		}
	}
}
