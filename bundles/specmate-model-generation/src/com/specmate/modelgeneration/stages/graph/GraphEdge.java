package com.specmate.modelgeneration.stages.graph;

public class GraphEdge {
	private final boolean negated;
	private final GraphNode from;
	private final GraphNode to;

	public GraphEdge(GraphNode from, GraphNode to) {
		this(from, to, false);
	}

	public GraphEdge(GraphNode from, GraphNode to, boolean isNegated) {
		negated = isNegated;
		this.from = from;
		this.to = to;
	}

	public boolean isNegated() {
		return negated;
	}

	public GraphNode getFrom() {
		return from;
	}

	public GraphNode getTo() {
		return to;
	}
}
