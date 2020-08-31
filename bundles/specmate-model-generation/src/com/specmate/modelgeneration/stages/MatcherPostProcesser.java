package com.specmate.modelgeneration.stages;

import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode;
import com.specmate.modelgeneration.stages.processors.ConditionVariableNodeBuilder;
import com.specmate.modelgeneration.stages.processors.ConditionVariableTextCleaner;
import com.specmate.modelgeneration.stages.processors.JoinedConditionSplitter;
import com.specmate.modelgeneration.stages.processors.OperationOrderFixer;
import com.specmate.nlp.api.ELanguage;

public class MatcherPostProcesser {

	private final OperationOrderFixer orderFixer;
	private final JoinedConditionSplitter conditionSplitter;
	private final ConditionVariableNodeBuilder cvNodeBuilder;
	private final ConditionVariableTextCleaner cvCleaner;

	public MatcherPostProcesser(ELanguage lang) {
		cvNodeBuilder = new ConditionVariableNodeBuilder();
		conditionSplitter = new JoinedConditionSplitter(lang);
		orderFixer = new OperationOrderFixer();
		cvCleaner = new ConditionVariableTextCleaner();
	}

	public MatchResultTreeNode process(MatchResultTreeNode node) {
		node = cvNodeBuilder.buildNodes(node);
		node = conditionSplitter.splitNodes(node);
		node.acceptVisitor(orderFixer);
		node.acceptVisitor(cvCleaner);
		return node;
	}
}
