package com.specmate.modelgeneration.stages.processors;

import com.specmate.cause_effect_patterns.parse.wrapper.LeafTreeNode;

public class ConditionVariableNode extends LeafTreeNode {
	private final String condition;
	private final String variable;

	public ConditionVariableNode(String condition, String variable) {
		super(variable + " " + condition);
		this.condition = condition;
		this.variable = variable;
	}

	public String getCondition() {
		return condition;
	}

	public String getVariable() {
		return variable;
	}

	@Override
	public RuleType getType() {
		return RuleType.CONDITION_VARIABLE;
	}
}
