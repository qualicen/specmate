package com.specmate.modelgeneration.stages.processors;

import com.specmate.cause_effect_patterns.parse.wrapper.BinaryMatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.LeafTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchTreeVisitor;
import com.specmate.cause_effect_patterns.parse.wrapper.NegationTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode.RuleType;

public class OperationOrderFixer extends MatchTreeVisitor {

	@Override
	public void visit(BinaryMatchResultTreeNode node) {
		node.getFirstArgument().acceptVisitor(this);
		node.getSecondArgument().acceptVisitor(this);
		
		RuleType type 		= node.getType();
		RuleType typeChild  = node.getFirstArgument().getType();
		if(typeChild != null && type.getPriority() > typeChild.getPriority() && !typeChild.isNegation()) {
			// Left Swap
			node.leftSwap();
			node.getFirstArgument().acceptVisitor(this);
		}
		
		typeChild = node.getSecondArgument().getType();
		if(typeChild != null && type.getPriority() > typeChild.getPriority() && !typeChild.isNegation()) {
			// Right Swap
			node.rightSwap();
			node.acceptVisitor(this);
		}
	}

	@Override
	public void visit(LeafTreeNode node) {}

	@Override
	public void visit(NegationTreeNode node) {
		node.getClause().acceptVisitor(this);
	}

}
