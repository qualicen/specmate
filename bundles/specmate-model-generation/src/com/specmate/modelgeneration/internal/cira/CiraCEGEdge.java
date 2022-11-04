package com.specmate.modelgeneration.internal.cira;

public class CiraCEGEdge {

	private CiraCEGNode source;
	private CiraCEGNode target;
	private boolean negate;

	public void setSource(CiraCEGNode source) {
		this.source = source;
	}

	public void setTarget(CiraCEGNode currentnode) {
		target = currentnode;
	}

	public void setNegate(boolean negate) {
		this.negate = negate;
	}

	public CiraCEGNode getSource() {
		return source;
	}

	public CiraCEGNode getTarget() {
		return target;
	}

	public boolean isNegate() {
		return negate;
	}

}
