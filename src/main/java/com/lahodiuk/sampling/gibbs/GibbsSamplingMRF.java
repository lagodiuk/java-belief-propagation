package com.lahodiuk.sampling.gibbs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.lahodiuk.bp.Edge;
import com.lahodiuk.bp.Node;

public class GibbsSamplingMRF extends GibbsSamplingOptimized {

	@SuppressWarnings("rawtypes")
	private final List<Node> nodes;

	private final Object[][] nodeIdxToStates;

	private List<int[]> samples;

	private List<double[]> marginalProbabilities;

	@SafeVarargs
	@SuppressWarnings("rawtypes")
	public <A, B> GibbsSamplingMRF(
			List<Edge<A, B>> edges,
			Collection<? extends Node>... nodeContainers) {

		List<Node> allNodesList = new ArrayList<>();
		for (Collection<? extends Node> nodeContainer : nodeContainers) {
			allNodesList.addAll(nodeContainer);
		}
		int nodesCnt = allNodesList.size();
		this.nodeIdxToStates = new Object[nodesCnt][];
		for (int i = 0; i < nodesCnt; i++) {
			Node<?> node = allNodesList.get(i);
			this.nodeIdxToStates[i] = node.getStates().toArray();
			node.setIndex(i);
		}

		this.nodes = allNodesList;
	}

	@Override
	public int getDimension() {
		return this.nodes.size();
	}

	@Override
	public int getAmountofValuesOfRandomVariable(int idx) {
		return this.nodeIdxToStates[idx].length;
	}

	@Override
	public double conditionalProbability(int indexOfValueOfRandomVariable, int idx, int[] vector) {
		Node<?> curr = this.nodes.get(idx);
		Object currState = this.nodeIdxToStates[idx][indexOfValueOfRandomVariable];
		double result = curr.getPriorProbablilityNoTypeCheck(currState);
		for (Edge<?, ?> e : curr.getEdges()) {
			if (curr == e.getNode1()) {
				Node<?> other = e.getNode2();
				int otherIdx = other.getIndex();
				Object node2State = this.nodeIdxToStates[otherIdx][vector[otherIdx]];
				result *= e.getPotential().getValueNoTypeCheck(currState, node2State);
			} else {
				Node<?> other = e.getNode1();
				int otherIdx = other.getIndex();
				Object node1State = this.nodeIdxToStates[otherIdx][vector[otherIdx]];
				result *= e.getPotential().getValueNoTypeCheck(node1State, currState);
			}
		}
		return result;
	}

	public void infer(int warmUpCount, int samplesCount, Random rnd) {
		this.infer(warmUpCount, samplesCount, rnd, false);
	}

	public void infer(int warmUpCount, int samplesCount, Random rnd, boolean displayDebugInfo) {
		this.samples = this.doSampling(warmUpCount, samplesCount, rnd);
		this.calculateMarginalProbabilitiesFromSamples();
		if (displayDebugInfo) {
			this.displayDebugInfo();
		}
	}

	public void displayDebugInfo() {
		for (int i = 0; i < this.nodes.size(); i++) {
			Node<?> curr = this.nodes.get(i);
			List<?> states = new ArrayList<>(curr.getStates());
			System.out.print(curr.getClass().getSimpleName() + " ");
			int amount = this.getAmountofValuesOfRandomVariable(i);
			for (int randomVariableIdx = 0; randomVariableIdx < amount; randomVariableIdx++) {
				System.out.print(states.get(randomVariableIdx) + ":"
						+ this.marginalProbabilities.get(i)[randomVariableIdx] + " ");
			}
			System.out.println();
		}
	}

	public void calculateMarginalProbabilitiesFromSamples() {
		this.marginalProbabilities = new ArrayList<>();
		for (int i = 0; i < this.getDimension(); i++) {
			this.marginalProbabilities.add(new double[this.getAmountofValuesOfRandomVariable(i)]);
		}
		for (int[] v : this.samples) {
			for (int i = 0; i < this.getDimension(); i++) {
				double[] margProbRandVar = this.marginalProbabilities.get(i);
				margProbRandVar[v[i]] = this.marginalProbabilities.get(i)[v[i]] + 1;
			}
		}
		for (int i = 0; i < this.getDimension(); i++) {
			double[] margProbRandVar = this.marginalProbabilities.get(i);
			int amount = this.getAmountofValuesOfRandomVariable(i);
			for (int randomVariableIdx = 0; randomVariableIdx < amount; randomVariableIdx++) {
				margProbRandVar[randomVariableIdx] /= this.samples.size();
			}
		}
	}

	public Object getMostProbableState(@SuppressWarnings("rawtypes") Node node) {
		int idx = node.getIndex();
		double[] nodeMarginalProbabilities = this.marginalProbabilities.get(idx);
		double maxProb = -1;
		int maxProbIdx = -1;
		for (int i = 0; i < nodeMarginalProbabilities.length; i++) {
			if (nodeMarginalProbabilities[i] > maxProb) {
				maxProb = nodeMarginalProbabilities[i];
				maxProbIdx = i;
			}
		}
		return this.nodeIdxToStates[idx][maxProbIdx];
	}
}
