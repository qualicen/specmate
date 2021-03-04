package com.specmate.testspecification.internal.generators;

import static com.specmate.model.support.util.SpecmateEcoreUtil.getCondition;
import static com.specmate.model.support.util.SpecmateEcoreUtil.getVariable;

import java.util.Comparator;
import java.util.TreeMap;

import com.specmate.model.requirements.CEGNode;

@SuppressWarnings("serial")
class CEGNodeEvaluation extends TreeMap<CEGNode, TaggedBoolean> {

	private static Comparator<CEGNode> evaluationComperator = new Comparator<CEGNode>() {
		// Sort each evaluation, so that the order of nodes in an evaluation are
		// lexicographically sorted
		@Override
		public int compare(CEGNode c1, CEGNode c2) {

			int result = getVariable(c1).compareTo(getVariable(c2));
			if (result != 0) {
				return result;
			} else {
				return getCondition(c1).compareTo(getCondition(c2));
			}
		}
	};

	public CEGNodeEvaluation() {
		super(evaluationComperator);
	}
}