package com.specmate.cause_effect_patterns.parse.wrapper;


public class NegationTreeNode extends MatchResultTreeNode {
	private MatchResultTreeNode clause;
	
	public NegationTreeNode(MatchResultTreeNode clause) {
		this.clause = clause;
	}
	
	public MatchResultTreeNode getClause() {
		return this.clause;
	}
	
	public void setClause(MatchResultTreeNode clause) {
		this.clause = clause;
	}

	@Override
	public RuleType getType() {
		return RuleType.NEGATION;
	}
	
	@Override
	public void acceptVisitor(MatchTreeVisitor visitor) {
		visitor.visit(this);
	}
}
