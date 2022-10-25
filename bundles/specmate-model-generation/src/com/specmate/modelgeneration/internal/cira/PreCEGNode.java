package com.specmate.modelgeneration.internal.cira;

import java.util.ArrayList;
import java.util.List;

public class PreCEGNode {
	String variable;
	String condition;
	List<Label> labels;
	List<PreCEGEdge> incomingConnections = new ArrayList<>();
	List<PreCEGEdge> outgoingConnections = new ArrayList<>();
	boolean variableAssumed;
	boolean conditionAssumed;
	boolean negate;
	String type;
	String _id;

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

	public List<Label> getLabels() {
		return labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	public List<PreCEGEdge> getIncomingConnections() {
		return incomingConnections;
	}

	public void setIncomingConnections(List<PreCEGEdge> incomingConnections) {
		this.incomingConnections = incomingConnections;
	}

	public List<PreCEGEdge> getOutgoingConnections() {
		return outgoingConnections;
	}

	public void setOutgoingConnections(List<PreCEGEdge> outgoingConnections) {
		this.outgoingConnections = outgoingConnections;
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