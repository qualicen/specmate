package com.specmate.modelgeneration.internal.cira;

import java.util.List;

public class PreCEGModel {

	List<PreCEGNode> nodes;
	List<PreCEGEdge> edges;

	public List<PreCEGNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<PreCEGNode> nodes) {
		this.nodes = nodes;
	}

	public List<PreCEGEdge> getEdges() {
		return edges;
	}

	public void setEdges(List<PreCEGEdge> edges) {
		this.edges = edges;
	}

}
