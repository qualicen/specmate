package com.specmate.modelgeneration.stages.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.specmate.cause_effect_patterns.parse.wrapper.BinaryMatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.LeafTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode.RuleType;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchTreeVisitor;
import com.specmate.cause_effect_patterns.parse.wrapper.NegationTreeNode;
import com.specmate.nlp.api.ELanguage;

/**
 * Splits Nodes of the type "if A<=x<=B" into two nodes "if A<=x" and "if x<=B"
 */
public class JoinedConditionSplitter {

	private static final Pattern CONDITION_PATTERN;
	static {
		String lt = "(?:(<\\s*=?)(.+)(<\\s*=?)(.+))";
		String gt = "(?:(>\\s*=?)(.+)(>\\s*=?)(.+))";
		CONDITION_PATTERN = Pattern.compile(lt + "|" + gt);
	}

	private final ELanguage lang;

	public JoinedConditionSplitter(ELanguage language) {
		lang = language;
	}

	public MatchResultTreeNode splitNodes(MatchResultTreeNode root) {
		final NodeVisitor visitor = new NodeVisitor();
		root.acceptVisitor(visitor);

		MatchResultTreeNode newRoot = root;
		if (visitor.replacementNode != null) {
			newRoot = visitor.replacementNode;
		}
		return newRoot;
	}

	private class NodeVisitor extends MatchTreeVisitor {
		public BinaryMatchResultTreeNode replacementNode;

		@Override
		public void visit(BinaryMatchResultTreeNode node) {
			node.getFirstArgument().acceptVisitor(this);
			if(replacementNode!=null) {
				node.setFirstArguement(replacementNode);
				replacementNode = null;
			}
			node.getSecondArgument().acceptVisitor(this);
			if(replacementNode!=null) {
				node.setSecondArguement(replacementNode);
				replacementNode = null;
			}
		}

		@Override
		public void visit(NegationTreeNode node) {
			node.getClause().acceptVisitor(this);
			if(replacementNode!=null) {
				node.setClause(replacementNode);
				replacementNode = null;
			}
		}

		@Override
		public void visit(LeafTreeNode node) {
			if (node instanceof ConditionVariableNode) {
				final ConditionVariableNode cvNode = (ConditionVariableNode) node;

				Matcher matcher = CONDITION_PATTERN.matcher(cvNode.getCondition());
				if(matcher.matches()) {
					String firstBound  = cvNode.getVariable();
					String firstOp	   = cleanOperator(matcher.group(1));
					firstOp 	   	   = pseudoInvertOperator(firstOp);
					String variable    = matcher.group(2);
					String secondOp    = cleanOperator(matcher.group(3));
					String secondBound = matcher.group(4);

					String firstCondition = getCondition(firstOp, firstBound);
					String secondCondition = getCondition(secondOp, secondBound);

					ConditionVariableNode first = new ConditionVariableNode(firstCondition, variable);
					ConditionVariableNode second = new ConditionVariableNode(secondCondition, variable);
					replacementNode = new BinaryMatchResultTreeNode(first, second, RuleType.CONJUNCTION_AND);
				}
			}
		}

		private String cleanOperator(String operator) {
			return operator.replaceAll("\\s", "");
		}


		/**
		 * This simply flips < >
		 * Note: this is not a mathmatical inversion! < will not be inverted to >= but to >.
		 * @param operator
		 * @return
		 */
		private String pseudoInvertOperator(String operator) {
			if(operator.contains("<")) {
				return operator.replaceAll("<", ">");
			}
			return operator.replaceAll(">", "<");
		}

		private String getCondition(String operator, String bound) {
			if(lang.equals(ELanguage.DE)) {
				return "ist "+operator+" "+bound;
			}
			return "is "+operator+" "+bound;
		}
	}

}
