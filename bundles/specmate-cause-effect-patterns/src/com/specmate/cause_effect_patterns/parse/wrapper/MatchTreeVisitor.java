package com.specmate.cause_effect_patterns.parse.wrapper;

public abstract class MatchTreeVisitor {
	public abstract void visit(BinaryMatchResultTreeNode node);
	public abstract void visit(LeafTreeNode node);
	public abstract void visit(NegationTreeNode node);
}
