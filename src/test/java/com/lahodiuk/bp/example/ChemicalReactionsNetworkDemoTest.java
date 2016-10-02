package com.lahodiuk.bp.example;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.lahodiuk.bp.example.ChemicalReactionsNetwork.CompoundType;
import com.lahodiuk.bp.example.ChemicalReactionsNetworkDemo.ReactionsNetwork;

public class ChemicalReactionsNetworkDemoTest {

	private ReactionsNetwork reactionsNetwork;

	@Before
	public void init() throws IOException {

		List<String> configuration = new BufferedReader(new InputStreamReader(
				ChemicalReactionsNetworkDemo.class.getResourceAsStream("/ChemicalReactionsNetwork.txt"),
				StandardCharsets.UTF_8)).lines().collect(Collectors.toList());

		this.reactionsNetwork = ChemicalReactionsNetworkDemo.configureReactionsNetwork(configuration);
	}

	@Test
	public void test() {
		ChemicalReactionsNetworkDemo.inference(this.reactionsNetwork, ChemicalReactionsNetworkDemo.ITERATIONS_NUMBER);

		assertEquals(CompoundType.WATER.name(), this.reactionsNetwork.getMostProbableCompoundType("H2O"));

		assertEquals(CompoundType.ACIDIC_OXIDE.name(), this.reactionsNetwork.getMostProbableCompoundType("CO2"));
		assertEquals(CompoundType.ACIDIC_OXIDE.name(), this.reactionsNetwork.getMostProbableCompoundType("SO3"));

		assertEquals(CompoundType.BASIC_OXIDE.name(), this.reactionsNetwork.getMostProbableCompoundType("K2O"));
		assertEquals(CompoundType.BASIC_OXIDE.name(), this.reactionsNetwork.getMostProbableCompoundType("Li2O"));
		assertEquals(CompoundType.BASIC_OXIDE.name(), this.reactionsNetwork.getMostProbableCompoundType("Na2O"));

		assertEquals(CompoundType.BASE.name(), this.reactionsNetwork.getMostProbableCompoundType("KOH"));
		assertEquals(CompoundType.BASE.name(), this.reactionsNetwork.getMostProbableCompoundType("LiOH"));
		assertEquals(CompoundType.BASE.name(), this.reactionsNetwork.getMostProbableCompoundType("NaOH"));

		assertEquals(CompoundType.ACID.name(), this.reactionsNetwork.getMostProbableCompoundType("H2CO3"));
		assertEquals(CompoundType.ACID.name(), this.reactionsNetwork.getMostProbableCompoundType("H2SO4"));

		assertEquals(CompoundType.SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("Li2CO3"));
		assertEquals(CompoundType.SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("Li2SO4"));
		assertEquals(CompoundType.SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("Na2SO4"));
		assertEquals(CompoundType.SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("Na2CO3"));
		assertEquals(CompoundType.SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("K2SO4"));
		assertEquals(CompoundType.SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("K2CO3"));

		assertEquals(CompoundType.ACID_SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("LiHSO4"));
		assertEquals(CompoundType.ACID_SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("LiHCO3"));
		assertEquals(CompoundType.ACID_SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("NaHSO4"));
		assertEquals(CompoundType.ACID_SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("NaHCO3"));
		assertEquals(CompoundType.ACID_SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("KHSO4"));
		assertEquals(CompoundType.ACID_SALT.name(), this.reactionsNetwork.getMostProbableCompoundType("KHCO3"));
	}
}
