package com.specmate.modelgeneration.stages.processors;

import com.specmate.cause_effect_patterns.parse.wrapper.LeafTreeNode;

public class ConditionVariableNode extends LeafTreeNode {
	private String condition;
	private String variable;

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

	public void setCondition(String newCondition) {
		if(newCondition == null) {
			throw new NullPointerException("Condition can not be null");
		}
		condition = newCondition;
	}

	public void setVariable(String newVariable) {
		if(newVariable == null) {
			throw new NullPointerException("Variable can not be null");
		}
		variable = newVariable;
	}

	@Override
	public RuleType getType() {
		return RuleType.CONDITION_VARIABLE;
	}
}
