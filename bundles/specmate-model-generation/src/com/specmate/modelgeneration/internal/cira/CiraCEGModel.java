package com.specmate.modelgeneration.internal.cira;

import java.util.List;

public class CiraCEGModel {

	List<CiraCEGNode> nodes;
	List<CiraCEGEdge> edges;

	public List<CiraCEGNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<CiraCEGNode> nodes) {
		this.nodes = nodes;
	}

	public List<CiraCEGEdge> getEdges() {
		return edges;
	}

	public void setEdges(List<CiraCEGEdge> edges) {
		this.edges = edges;
	}

}
