package com.specmate.modelgeneration.internal.cira;

public class PreCEGEdge {

	private PreCEGNode source;
	private PreCEGNode target;
	private boolean negate;

	public void setSource(PreCEGNode source) {
		this.source = source;
	}

	public void setTarget(PreCEGNode currentnode) {
		target = currentnode;
	}

	public void setNegate(boolean negate) {
		this.negate = negate;
	}

	public PreCEGNode getSource() {
		return source;
	}

	public PreCEGNode getTarget() {
		return target;
	}

	public boolean isNegate() {
		return negate;
	}

}
