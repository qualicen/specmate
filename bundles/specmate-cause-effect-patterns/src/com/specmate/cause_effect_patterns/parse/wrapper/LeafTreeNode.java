package com.specmate.cause_effect_patterns.parse.wrapper;


public class LeafTreeNode extends MatchResultTreeNode {
	
	private String content;
	public LeafTreeNode(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}

	@Override
	public RuleType getType() {
		return null;
	}

	@Override
	public void acceptVisitor(MatchTreeVisitor visitor) {
		visitor.visit(this);
	}

}
