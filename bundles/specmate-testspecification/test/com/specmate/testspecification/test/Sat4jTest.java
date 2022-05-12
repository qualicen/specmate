package com.specmate.testspecification.test;

import org.junit.Test;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.GateTranslator;

public class Sat4jTest {

	@Test
	public void testSat4j() throws ContradictionException, TimeoutException {
		// root: 1
		// nodes: 2-7
		// eval1: 8

		IPBSolver solver = org.sat4j.pb.SolverFactory.newResolution();
		GateTranslator translator = new GateTranslator(solver);
		WeightedMaxSatDecorator maxSat = new WeightedMaxSatDecorator(solver);

		maxSat.newVar(25);
		translator.and(1, new VecInt(new int[] { 3, 2 }));
		translator.or(2, new VecInt(new int[] { 4, 6 }));
		translator.or(3, new VecInt(new int[] { 4, 5 }));

		translator.addExactly(new VecInt(new int[] { 5, 4, 6 }), 1);

		translator.addClause(new VecInt(new int[] { -7, 1 }));
		translator.addClause(new VecInt(new int[] { -7, 2 }));
		translator.addClause(new VecInt(new int[] { -7, 3 }));
		translator.addClause(new VecInt(new int[] { -7, -4 }));
		translator.addClause(new VecInt(new int[] { -7, 5 }));

		translator.addClause(new VecInt(new int[] { -8, -1 }));
		translator.addClause(new VecInt(new int[] { -8, 2 }));
		translator.addClause(new VecInt(new int[] { -8, -3 }));
		translator.addClause(new VecInt(new int[] { -8, -4 }));
		translator.addClause(new VecInt(new int[] { -8, -5 }));

		translator.addClause(new VecInt(new int[] { -9, -1 }));
		translator.addClause(new VecInt(new int[] { -9, 2 }));
		translator.addClause(new VecInt(new int[] { -9, -3 }));
		translator.addClause(new VecInt(new int[] { -9, -4 }));
		translator.addClause(new VecInt(new int[] { -9, -5 }));

		translator.addClause(new VecInt(new int[] { -10, 1 }));
		translator.addClause(new VecInt(new int[] { -10, 2 }));
		translator.addClause(new VecInt(new int[] { -10, 3 }));
		translator.addClause(new VecInt(new int[] { -10, -4 }));
		translator.addClause(new VecInt(new int[] { -10, -5 }));

//		maxSat.addSoftClause(new VecInt(new int[] { 7 }));
		maxSat.addSoftClause(new VecInt(new int[] { 8 }));
		maxSat.addSoftClause(new VecInt(new int[] { 9 }));
		maxSat.addSoftClause(new VecInt(new int[] { 10 }));

//		maxSat.addLiteralsToMinimize(new VecInt(new int[] { -7, -8 }));

		int[] model = maxSat.findModel();
		System.out.println(model);
	}
}
