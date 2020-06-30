package com.specmate.modelgeneration.stages.graph;

import java.util.List;
import java.util.Vector;

import com.specmate.model.requirements.NodeType;

public class GraphNode {
	private String condition;
	private String variable;
	private NodeType type;
	private Graph graph;

	private List<GraphEdge> parentEdges;
	private List<GraphEdge> childEdges;

	GraphNode(Graph graph) {
		this(graph, NodeType.AND);
	}

	GraphNode(Graph graph, NodeType type) {
		this(graph, null, null, type);
	}

	GraphNode(Graph graph, String condition, String variable, NodeType type) {
		this.type = type;
		this.condition = condition;
		this.variable = variable;
		this.graph = graph;
		childEdges = new Vector<GraphEdge>();
		parentEdges = new Vector<GraphEdge>();
	}


	public void connectTo(GraphNode node, boolean negateEdge) {
		GraphEdge edge = new GraphEdge(this, node, negateEdge);
		childEdges.add(edge);
		node.parentEdges.add(edge);
		graph.edges.add(edge);
	}

	public NodeType getType() {
		return type;
	}

	public String getCondition() {
		return condition;
	}

	public String getVariable() {
		return variable;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public boolean isRoot() {
		return parentEdges.isEmpty();
	}

	public boolean isLeaf() {
		return childEdges.isEmpty();
	}


	int getDepth() {
		if(isRoot()) {
			return 0;
		}

		int maxDepth = parentEdges.get(0).getFrom().getDepth();
		for(GraphEdge edge: parentEdges) {
			int depth =  edge.getFrom().getDepth();
			maxDepth = Math.max(maxDepth, depth);
		}

		return 1 + maxDepth;
	}

	public int getHeight() {
		if(isLeaf()) {
			return getDepth();
		}

		int minHeight = childEdges.get(0).getTo().getHeight();
		for(GraphEdge edge: childEdges) {
			int depth =  edge.getTo().getHeight();
			minHeight = Math.min(minHeight, depth);
		}
		return minHeight - 1;
	}
}
