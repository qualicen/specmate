package com.specmate.modelgeneration.stages;

import java.util.HashMap;

import com.specmate.model.requirements.CEGModel;
import com.specmate.model.requirements.CEGNode;
import com.specmate.model.requirements.RequirementsFactory;
import com.specmate.modelgeneration.CEGCreation;
import com.specmate.modelgeneration.stages.graph.Graph;
import com.specmate.modelgeneration.stages.graph.GraphEdge;
import com.specmate.modelgeneration.stages.graph.GraphNode;
import com.specmate.nlp.api.ELanguage;

public class GraphLayouter {
	private static final int XSTART = 225;
	private static final int YSTART = 225;

	private static final int XOFFSET = 300;
	private static final int YOFFSET = 150;

	private final ELanguage lang;
	private final CEGCreation creation;

	public GraphLayouter(ELanguage language, CEGCreation creation) {
		lang = language;
		this.creation = creation;
	}

	private String innerVariableString() {
		if (lang == ELanguage.DE) {
			return "Innerer Knoten";
		}
		return "Inner Node";
	}

	private String innerConditionString() {
		if (lang == ELanguage.DE) {
			return "Ist erfüllt";
		}
		return "Is fulfilled";
	}

	public CEGModel createModel(Graph graph) {
		CEGModel model = RequirementsFactory.eINSTANCE.createCEGModel();
		int graphDepth = graph.getDepth();
		int[] positionTable = new int[graphDepth + 1];

		HashMap<GraphNode, CEGNode> nodeMap = new HashMap<GraphNode, CEGNode>();
		for (GraphNode node : graph.nodes) {
			int xIndex = node.getHeight();
			int yIndex = positionTable[xIndex];

			int x = XSTART + xIndex * XOFFSET;
			int y = YSTART + yIndex * YOFFSET;

			String condition = node.getCondition();
			String variable = node.getVariable();

			if (graph.isInnerNode(node)) {
				condition = innerConditionString();
				variable = innerVariableString() + " " + xIndex + " - " + yIndex;
			}

			CEGNode cegNode = creation.createNode(model, variable, condition, x, y, node.getType());
			nodeMap.put(node, cegNode);
			positionTable[xIndex]++;
		}

		for (GraphEdge edge : graph.edges) {
			CEGNode from = nodeMap.get(edge.getFrom());
			CEGNode to = nodeMap.get(edge.getTo());
			creation.createConnection(model, from, to, edge.isNegated());
		}
		return model;

	}

}
