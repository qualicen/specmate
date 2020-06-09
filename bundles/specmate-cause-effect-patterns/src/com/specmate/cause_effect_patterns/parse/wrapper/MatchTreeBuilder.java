package com.specmate.cause_effect_patterns.parse.wrapper;

import java.util.Optional;

import com.specmate.cause_effect_patterns.parse.matcher.MatchResult;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode.RuleType;

public class MatchTreeBuilder {
	private static class SubtreeNames {
		public static final String CONDITIONAL = "Conditional";
		public static final String LIMIT = "Limit";
		public static final String CAUSE = "Cause";
		public static final String EFFECT = "Effect";
		public static final String PART_A = "PartA";
		public static final String PART_B = "PartB";
		// public static final String TMP = "TMP";
		public static final String HEAD = "Head";
		public static final String CONDITION = "Condition";
		public static final String VARIABLE = "Variable";
		public static final String VERB = "Verb";
		public static final String OBJECT = "Object";
		public static final String PREPOSITION = "Preposition";
	}

	private static class RuleNames {
		public static final String LIMITED_CONDITION = "LimitedCondition";
		public static final String CONDITION = "Condition";
		public static final String CONJUNCTION = "Conjunction";
		public static final String NEGATION = "Negation";
		public static final String CONDITION_VARIABLE = "CondVar";
		public static final String VERB_OBJECT = "VerbObject";
		public static final String VERB_PREPOSITION = "VerbPreposition";
		public static final String XOR = "_XOR";
		public static final String NOR = "_NOR";
		public static final String OR = "_OR";
		public static final String AND = "_AND";
	}

	private boolean isCondition(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.CONDITION);
		boolean subMatches = result.hasSubmatch(SubtreeNames.CAUSE) && result.hasSubmatch(SubtreeNames.EFFECT);
		return name && subMatches;
	}

	private boolean isLimitedCondition(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.LIMITED_CONDITION);
		boolean subMatches = result.hasSubmatch(SubtreeNames.LIMIT) && result.hasSubmatch(SubtreeNames.CONDITIONAL);
		return name && subMatches;
	}

	private boolean isConjunction(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.CONJUNCTION);
		boolean subMatches = result.hasSubmatch(SubtreeNames.PART_A) && result.hasSubmatch(SubtreeNames.PART_B);
		return name && subMatches;
	}

	private boolean isAndConjunction(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.AND);
		return isConjunction(result) && name;
	}

	private boolean isOrConjunction(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.OR);
		return isConjunction(result) && name;
	}

	private boolean isXorConjunction(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.XOR);
		return isConjunction(result) && name;
	}

	private boolean isNorConjunction(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.NOR);
		return isConjunction(result) && name;
	}

	private boolean isNegation(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.NEGATION);
		boolean subMatches = result.hasSubmatch(SubtreeNames.HEAD);
		return name && subMatches;
	}

	private boolean isConditionVarible(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.CONDITION_VARIABLE);
		boolean subMatches = result.hasSubmatch(SubtreeNames.VARIABLE) && result.hasSubmatch(SubtreeNames.CONDITION);
		return name && subMatches;
	}

	private boolean isVerbObject(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.VERB_OBJECT);
		boolean subMatches = result.hasSubmatch(SubtreeNames.VERB) && result.hasSubmatch(SubtreeNames.OBJECT);
		return name && subMatches;
	}

	private boolean isVerbPreposition(MatchResult result) {
		boolean name = result.hasRuleName() && result.getRuleName().contains(RuleNames.VERB_PREPOSITION);
		boolean subMatches = result.hasSubmatch(SubtreeNames.VERB) && result.hasSubmatch(SubtreeNames.PREPOSITION);
		return name && subMatches;
	}

	private String getFirstArgumentName(MatchResult result) {
		if (isLimitedCondition(result)) {
			return SubtreeNames.LIMIT;
		} else if (isCondition(result)) {
			return SubtreeNames.CAUSE;
		} else if (isConjunction(result)) {
			return SubtreeNames.PART_A;
		} else if (isNegation(result)) {
			return SubtreeNames.HEAD;
		} else if (isConditionVarible(result)) {
			return SubtreeNames.VARIABLE;
		} else if (isVerbObject(result)) {
			return SubtreeNames.VERB;
		} else if (isVerbPreposition(result)) {
			return SubtreeNames.VERB;
		}

		return null;
	}

	public Optional<MatchResultTreeNode> getFirstArgument(MatchResult result) {
		String name = getFirstArgumentName(result);
		if (name != null) {
			return buildTree(result.getSubmatch(name));
		}
		return null;
	}

	private String getSecondArgumentName(MatchResult result) {
		if (isLimitedCondition(result)) {
			return SubtreeNames.CONDITIONAL;
		} else if (isCondition(result)) {
			return SubtreeNames.EFFECT;
		} else if (isConjunction(result)) {
			return SubtreeNames.PART_B;
		} else if (isConditionVarible(result)) {
			return SubtreeNames.CONDITION;
		} else if (isVerbObject(result)) {
			return SubtreeNames.OBJECT;
		} else if (isVerbPreposition(result)) {
			return SubtreeNames.PREPOSITION;
		}
		return null;
	}

	public Optional<MatchResultTreeNode> getSecondArgument(MatchResult result) {
		String name = getSecondArgumentName(result);
		if (name != null) {
			return buildTree(result.getSubmatch(name));
		}
		return null;
	}

	public RuleType getType(MatchResult result) {
		if (isLimitedCondition(result)) {
			return RuleType.LIMITED_CONDITION;
		} else if (isCondition(result)) {
			return RuleType.CONDITION;
		} else if (isAndConjunction(result)) {
			return RuleType.CONJUNCTION_AND;
		} else if (isOrConjunction(result)) {
			return RuleType.CONJUNCTION_OR;
		} else if (isXorConjunction(result)) {
			return RuleType.CONJUNCTION_XOR;
		} else if (isNorConjunction(result)) {
			return RuleType.CONJUNCTION_NOR;
		} else if (isNegation(result)) {
			return RuleType.NEGATION;
		} else if (isVerbObject(result)) {
			return RuleType.VERB_OBJECT;
		} else if (isConditionVarible(result)) {
			return RuleType.CONDITION_VARIABLE;
		} else if (isVerbPreposition(result)) {
			return RuleType.VERB_PREPOSITION;
		}

		return null;
	}

	public Optional<MatchResultTreeNode> buildTree(MatchResult result) {
		if (!result.isSuccessfulMatch()) {
			return Optional.empty();
		}

		// Unary
		if (isNegation(result)) {
			MatchResultTreeNode clause = getFirstArgument(result).get();
			return Optional.of(new NegationTreeNode(clause));
		}

		// Binary
		if (isConditionVarible(result) || isVerbObject(result) || isVerbPreposition(result) || isConjunction(result)
				|| isCondition(result) || isLimitedCondition(result)) {
			MatchResultTreeNode left = getFirstArgument(result).get();
			MatchResultTreeNode right = getSecondArgument(result).get();
			return Optional.of(new BinaryMatchResultTreeNode(left, right, getType(result)));
		}

		// Just Text
		LeafTreeNode leaf = new LeafTreeNode(result.getMatchTree().getRepresentationString(false));
		return Optional.of(leaf);
	}
}
