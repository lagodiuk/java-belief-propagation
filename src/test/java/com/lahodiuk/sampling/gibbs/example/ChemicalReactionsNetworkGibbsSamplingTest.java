package com.lahodiuk.sampling.gibbs.example;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.example.ChemicalReactionsNetwork;
import com.lahodiuk.bp.example.ChemicalReactionsNetwork.CompoundType;
import com.lahodiuk.bp.example.ChemicalReactionsNetwork.ReactionsNetwork;
import com.lahodiuk.bp.example.ChemicalReactionsNetwork.Rule;
import com.lahodiuk.sampling.gibbs.GibbsSamplingMRF;

public class ChemicalReactionsNetworkGibbsSamplingTest {

	private ReactionsNetwork reactionsNetwork;

	@Before
	public void init() {
		this.reactionsNetwork = ChemicalReactionsNetwork.configureReactionsNetwork();
		this.reactionsNetwork.buildNetwork();
	}

	@Test
	public void test() {

		GibbsSamplingMRF gibbsSampling = new<CompoundType, Rule> GibbsSamplingMRF(
				this.reactionsNetwork.getEdges(),
				this.reactionsNetwork.getCompoundToCompoundNode().values(),
				this.reactionsNetwork.getReactionToReactionNode().values());

		gibbsSampling.infer(1500, 2500, new Random(101));

		assertEquals(CompoundType.WATER, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("H2O")));

		assertEquals(CompoundType.ACIDIC_OXIDE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("CO2")));
		assertEquals(CompoundType.ACIDIC_OXIDE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("SO3")));

		assertEquals(CompoundType.BASIC_OXIDE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("K2O")));
		assertEquals(CompoundType.BASIC_OXIDE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("Li2O")));
		assertEquals(CompoundType.BASIC_OXIDE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("Na2O")));

		assertEquals(CompoundType.BASE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("KOH")));
		assertEquals(CompoundType.BASE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("LiOH")));
		assertEquals(CompoundType.BASE, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("NaOH")));

		assertEquals(CompoundType.ACID, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("H2CO3")));
		assertEquals(CompoundType.ACID, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("H2SO4")));

		assertEquals(CompoundType.SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("Li2CO3")));
		assertEquals(CompoundType.SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("Li2SO4")));
		assertEquals(CompoundType.SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("Na2SO4")));
		assertEquals(CompoundType.SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("Na2CO3")));
		assertEquals(CompoundType.SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("K2SO4")));
		assertEquals(CompoundType.SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("K2CO3")));

		assertEquals(CompoundType.ACID_SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("LiHSO4")));
		assertEquals(CompoundType.ACID_SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("LiHCO3")));
		assertEquals(CompoundType.ACID_SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("NaHSO4")));
		assertEquals(CompoundType.ACID_SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("NaHCO3")));
		assertEquals(CompoundType.ACID_SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("KHSO4")));
		assertEquals(CompoundType.ACID_SALT, gibbsSampling.getMostProbableState(this.reactionsNetwork.getCompoundToCompoundNode().get("KHCO3")));
	}
}
