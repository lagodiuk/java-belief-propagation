package com.lahodiuk.bp.example;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.example.ChemicalReactionsNetwork.CompoundType;
import com.lahodiuk.bp.example.ChemicalReactionsNetwork.ReactionsNetwork;

public class ChemicalReactionsNetworkTest {

	private ReactionsNetwork reactionsNetwork;

	@Before
	public void init() {
		this.reactionsNetwork = ChemicalReactionsNetwork.configureReactionsNetwork();
	}

	@Test
	public void test() {
		ChemicalReactionsNetwork.inference(this.reactionsNetwork);

		assertEquals(CompoundType.WATER, this.reactionsNetwork.getMostProbableCompoundType("H2O"));

		assertEquals(CompoundType.ACIDIC_OXIDE, this.reactionsNetwork.getMostProbableCompoundType("CO2"));
		assertEquals(CompoundType.ACIDIC_OXIDE, this.reactionsNetwork.getMostProbableCompoundType("SO3"));

		assertEquals(CompoundType.BASIC_OXIDE, this.reactionsNetwork.getMostProbableCompoundType("K2O"));
		assertEquals(CompoundType.BASIC_OXIDE, this.reactionsNetwork.getMostProbableCompoundType("Li2O"));
		assertEquals(CompoundType.BASIC_OXIDE, this.reactionsNetwork.getMostProbableCompoundType("Na2O"));

		assertEquals(CompoundType.BASE, this.reactionsNetwork.getMostProbableCompoundType("KOH"));
		assertEquals(CompoundType.BASE, this.reactionsNetwork.getMostProbableCompoundType("LiOH"));
		assertEquals(CompoundType.BASE, this.reactionsNetwork.getMostProbableCompoundType("NaOH"));

		assertEquals(CompoundType.ACID, this.reactionsNetwork.getMostProbableCompoundType("H2CO3"));
		assertEquals(CompoundType.ACID, this.reactionsNetwork.getMostProbableCompoundType("H2SO4"));

		assertEquals(CompoundType.SALT, this.reactionsNetwork.getMostProbableCompoundType("Li2SO4"));
		assertEquals(CompoundType.SALT, this.reactionsNetwork.getMostProbableCompoundType("Na2SO4"));
		assertEquals(CompoundType.SALT, this.reactionsNetwork.getMostProbableCompoundType("Na2CO3"));
		assertEquals(CompoundType.SALT, this.reactionsNetwork.getMostProbableCompoundType("K2SO4"));
		assertEquals(CompoundType.SALT, this.reactionsNetwork.getMostProbableCompoundType("K2CO3"));

		assertEquals(CompoundType.ACID_SALT, this.reactionsNetwork.getMostProbableCompoundType("LiHSO4"));
		assertEquals(CompoundType.ACID_SALT, this.reactionsNetwork.getMostProbableCompoundType("NaHSO4"));
		assertEquals(CompoundType.ACID_SALT, this.reactionsNetwork.getMostProbableCompoundType("NaHCO3"));
		assertEquals(CompoundType.ACID_SALT, this.reactionsNetwork.getMostProbableCompoundType("KHSO4"));
		assertEquals(CompoundType.ACID_SALT, this.reactionsNetwork.getMostProbableCompoundType("KHCO3"));
	}
}
