package com.specmate.modelgeneration.stages.processors;

import com.specmate.cause_effect_patterns.parse.wrapper.BinaryMatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.LeafTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode.RuleType;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchTreeVisitor;
import com.specmate.cause_effect_patterns.parse.wrapper.NegationTreeNode;

public class ConditionVariableNodeBuilder {

	public MatchResultTreeNode buildNodes(MatchResultTreeNode root) {
		NodeVisitor visitor = new NodeVisitor();
		root.acceptVisitor(visitor);

		MatchResultTreeNode newRoot = root;
		if (visitor.replacementNode != null) {
			newRoot = visitor.replacementNode;
		}
		return newRoot;
	}

	private class NodeVisitor extends MatchTreeVisitor {
		public ConditionVariableNode replacementNode;

		@Override
		public void visit(BinaryMatchResultTreeNode node) {
			if (node.getType().equals(RuleType.CONDITION_VARIABLE)) {
				String var = ((LeafTreeNode) node.getFirstArgument()).getContent();
				String cond = "";

				MatchResultTreeNode condition = node.getSecondArgument();
				if (condition.getType() != null && condition.getType().equals(RuleType.VERB_OBJECT)) {
					BinaryMatchResultTreeNode binCondition = (BinaryMatchResultTreeNode) condition;
					String object = ((LeafTreeNode) binCondition.getSecondArgument()).getContent();
					String verb = "";
					MatchResultTreeNode verbNode = binCondition.getFirstArgument();
					if (verbNode.getType() != null && verbNode.getType().equals(RuleType.VERB_PREPOSITION)) {
						BinaryMatchResultTreeNode verbBinNode = (BinaryMatchResultTreeNode) verbNode;
						String prep = ((LeafTreeNode) verbBinNode.getSecondArgument()).getContent();
						String vb = ((LeafTreeNode) verbBinNode.getFirstArgument()).getContent();
						cond = vb + " " + object + " " + prep;
					} else {
						verb = ((LeafTreeNode) verbNode).getContent();
						cond = verb + " " + object;
					}
				} else if (condition.getType() != null && condition.getType().equals(RuleType.VERB_PREPOSITION)) {
					BinaryMatchResultTreeNode binCondition = (BinaryMatchResultTreeNode) condition;
					String prep = ((LeafTreeNode) binCondition.getSecondArgument()).getContent();
					String vb = ((LeafTreeNode) binCondition.getFirstArgument()).getContent();
					cond = vb + " " + prep;
				} else {
					cond = ((LeafTreeNode) condition).getContent();
				}
				replacementNode = new ConditionVariableNode(cond, var);

			} else if (node.getType().equals(RuleType.VERB_OBJECT)) {
				String var = ((LeafTreeNode) node.getFirstArgument()).getContent();
				String cond = ((LeafTreeNode) node.getSecondArgument()).getContent();
				replacementNode = new ConditionVariableNode(cond, var);
			} else {
				node.getFirstArgument().acceptVisitor(this);
				if (replacementNode != null) {
					node.setFirstArguement(replacementNode);
					replacementNode = null;
				}

				node.getSecondArgument().acceptVisitor(this);
				if (replacementNode != null) {
					node.setSecondArguement(replacementNode);
					replacementNode = null;
				}
			}
		}

		@Override
		public void visit(LeafTreeNode node) {
			replacementNode = new ConditionVariableNode("", node.getContent());
		}

		@Override
		public void visit(NegationTreeNode node) {
			node.getClause().acceptVisitor(this);
			if (replacementNode != null) {
				node.setClause(replacementNode);
				replacementNode = null;
			}
		}

	}
}
