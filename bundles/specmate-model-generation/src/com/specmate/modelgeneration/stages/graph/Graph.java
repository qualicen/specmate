package com.specmate.modelgeneration.stages.graph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.specmate.model.requirements.NodeType;

public class Graph {
	public final List<GraphEdge> edges;
	public final List<GraphNode> nodes;
	private final Set<GraphNode> innerNodes;

	public Graph() {
		edges = new Vector<>();
		nodes = new Vector<>();
		innerNodes = new HashSet<>();
	}

	public GraphNode createNode(String condition, String variable, NodeType type) {
		GraphNode result = new GraphNode(this, condition, variable, type);
		nodes.add(result);
		return result;
	}

	public GraphNode createInnerNode( NodeType type) {
		GraphNode result = new GraphNode(this, null, null, type);
		nodes.add(result);
		innerNodes.add(result);
		return result;
	}

	public int getDepth() {
		return nodes.stream()
				.filter(GraphNode::isLeaf)
				.mapToInt(GraphNode::getDepth)
				.max()
				.getAsInt();

	}

	public boolean isInnerNode(GraphNode node) {
		return innerNodes.contains(node);
	}
}
