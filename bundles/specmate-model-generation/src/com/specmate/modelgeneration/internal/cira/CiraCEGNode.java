package com.specmate.modelgeneration.internal.cira;

import java.util.ArrayList;
import java.util.List;

public class CiraCEGNode {
	private String variable;
	private String condition;
	private List<CiraLabel> labels;
	private List<CiraCEGEdge> incomingConnections = new ArrayList<>();
	private List<CiraCEGEdge> outgoingConnections = new ArrayList<>();
	private boolean variableAssumed;
	private boolean conditionAssumed;
	private boolean negate;
	private String type;

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public List<CiraLabel> getLabels() {
		return labels;
	}

	public void setLabels(List<CiraLabel> labels) {
		this.labels = labels;
	}

	public List<CiraCEGEdge> getIncomingConnections() {
		return incomingConnections;
	}

	public List<CiraCEGEdge> getOutgoingConnections() {
		return outgoingConnections;
	}

	public boolean getVariableAssumed() {
		return variableAssumed;
	}

	public void setVariableAssumed(boolean variableAssumed) {
		this.variableAssumed = variableAssumed;
	}

	public boolean getConditionAssumed() {
		return conditionAssumed;
	}

	public void setConditionAssumed(boolean conditionAssumed) {
		this.conditionAssumed = conditionAssumed;
	}

	public boolean isNegate() {
		return negate;
	}

	public void setNegate(boolean negate) {
		this.negate = negate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}