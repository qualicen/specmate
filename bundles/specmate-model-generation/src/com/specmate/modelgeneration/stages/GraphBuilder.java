package com.specmate.modelgeneration.stages;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.specmate.cause_effect_patterns.parse.wrapper.BinaryMatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode;
import com.specmate.cause_effect_patterns.parse.wrapper.MatchResultTreeNode.RuleType;
import com.specmate.cause_effect_patterns.parse.wrapper.NegationTreeNode;
import com.specmate.model.requirements.NodeType;
import com.specmate.modelgeneration.stages.graph.Graph;
import com.specmate.modelgeneration.stages.graph.GraphNode;
import com.specmate.modelgeneration.stages.processors.ConditionVariableNode;

public class GraphBuilder {

	private Graph currentGraph;

	public synchronized Graph buildGraph(BinaryMatchResultTreeNode root) {
		List<MatchResultTreeNode> clauses = getClauses(root);
		MatchResultTreeNode effect = clauses.remove(clauses.size() - 1);
		List<MatchResultTreeNode> causes = clauses;

		currentGraph = new Graph();
		DirectCause cause = resolveCauses(causes);
		resolveEffect(cause, effect);

		Graph result = currentGraph;
		currentGraph = null;
		return result;
	}

	/**
	 * Returns a list consisting of all causes and the effect as last element [cause
	 * 1, cause 2, ..., cause n, effect]
	 */
	private List<MatchResultTreeNode> getClauses(BinaryMatchResultTreeNode root) {

		Vector<MatchResultTreeNode> worklist = new Vector<MatchResultTreeNode>();
		worklist.add(root);
		MatchResultTreeNode effect = root;
		Vector<MatchResultTreeNode> result = new Vector<MatchResultTreeNode>();

		while (!worklist.isEmpty()) {
			MatchResultTreeNode current = worklist.remove(0);
			if (current.getType().isCondition()) {
				MatchResultTreeNode left = ((BinaryMatchResultTreeNode) current).getFirstArgument();
				MatchResultTreeNode right = ((BinaryMatchResultTreeNode) current).getSecondArgument();
				worklist.add(left);
				worklist.add(right);
				if (current == effect) {
					effect = right;
				}
			} else {
				if (current != effect) {
					result.add(current);
				}
			}
		}
		result.add(effect);
		return result;
	}

	private DirectCause resolveCauses(List<MatchResultTreeNode> causes) {
		final DirectCause result = new DirectCause();
		for (int i = 0; i < causes.size(); i++) {
			final MatchResultTreeNode cause = causes.get(i);
			// Resolve Single Cause
			final DirectCause dirCause = resolveCause(cause);

			final int dirCauseCauseSize = dirCause.positiveCauses.size() + dirCause.negativeCauses.size();

			// If there are multiple causes
			if (causes.size() > 1 && dirCauseCauseSize > 1) {
				// Generate Fake Effect Nodes
				final GraphNode node = currentGraph.createInnerNode(dirCause.effectType);
				fullyConnect(dirCause, node);
				// Add Fake Effect Node to DirectCause Positive Causes
				result.positiveCauses.add(node);
			} else {
				// Add Resolved Positive/Negative Causes to DirectCause
				result.effectType = dirCause.effectType;
				result.negativeCauses.addAll(dirCause.negativeCauses);
				result.positiveCauses.addAll(dirCause.positiveCauses);
			}
		}

		return result;
	}

	private DirectCause resolveCause(MatchResultTreeNode cause) {
		final DirectCause result = new DirectCause();

		if (cause.getType().isConjunction()) {
			BinaryMatchResultTreeNode biCause = (BinaryMatchResultTreeNode) cause;
			final List<MatchResultTreeNode> worklist = new LinkedList<MatchResultTreeNode>(
					Arrays.asList(biCause.getFirstArgument(), biCause.getSecondArgument()));
			final Vector<DirectCause> arguments = new Vector<DirectCause>();

			final RuleType type = cause.getType();
			while (!worklist.isEmpty()) {
				final MatchResultTreeNode wrap = worklist.remove(0);
				final RuleType typeWrap = wrap.getType();
				if (type.equals(typeWrap)) {
					worklist.add(((BinaryMatchResultTreeNode) wrap).getFirstArgument());
					worklist.add(((BinaryMatchResultTreeNode) wrap).getSecondArgument());
				} else {
					final DirectCause argument = resolveCause(wrap);
					final int argCauseCount = argument.positiveCauses.size() + argument.negativeCauses.size();
					if (argCauseCount > 1) {
						// Create Inner Node
						final GraphNode node = currentGraph.createInnerNode(argument.effectType);
						fullyConnect(argument, node);
						argument.positiveCauses.clear();
						argument.negativeCauses.clear();
						argument.positiveCauses.add(node);
					}
					arguments.add(argument);
				}
			}

			final DirectCause merge = DirectCause.mergeCauses(arguments.toArray(new DirectCause[arguments.size()]));

			if (cause.getType().isXorConjunction()) {
				// Create inner XOR Nodes use them as new positive set
				// Create XOR Node
				// Directly connect all other nodes normally
				// Connect one node negated
				// Add XOR Node to positiveCauses

				for (GraphNode pNode : merge.positiveCauses) {
					GraphNode xorNode = currentGraph.createInnerNode(NodeType.AND);
					xorConnect(merge, xorNode, pNode);
					result.positiveCauses.add(xorNode);
				}

				for (GraphNode nNode : merge.negativeCauses) {
					GraphNode xorNode = currentGraph.createInnerNode(NodeType.AND);
					xorConnect(merge, xorNode, nNode);
					result.positiveCauses.add(xorNode);
				}
				result.effectType = NodeType.OR;
			} else if (cause.getType().isNorConjunction()) {
				// Swap positive & negative causes, then AND
				// XXX Technically NOR is not associative so a nested NOR would turn up with a
				// not equivalent result, however since nested NORs usually don't happen we
				// ignore this case.
				final List<GraphNode> tmp = merge.negativeCauses;
				merge.negativeCauses = merge.positiveCauses;
				merge.positiveCauses = tmp;
			} else if (cause.getType().isOrConjunction()) {
				result.effectType = NodeType.OR;
				// Swap result type, then AND
			}
			// Default / AND
			// Combine positive & negative causes of dA & dB in result
			result.addCauses(merge);
		} else if (cause.getType().isNegation()) {
			NegationTreeNode negNode = (NegationTreeNode) cause;
			final DirectCause dHead = resolveCause(negNode.getClause());
			result.positiveCauses = dHead.negativeCauses;
			result.negativeCauses = dHead.positiveCauses;
		} else {
			// Create Direct Node
			ConditionVariableNode cvNode = (ConditionVariableNode) cause;

			GraphNode node = currentGraph.createNode(cvNode.getCondition(), cvNode.getVariable(), NodeType.AND);
			result.positiveCauses.add(node);
		}
		return result;
	}

	private void xorConnect(DirectCause dirCause, GraphNode node, GraphNode invertedNode) {
		for (final GraphNode pCause : dirCause.positiveCauses) {
			boolean inverted = false;
			if (pCause.equals(invertedNode)) {
				inverted = true;
			}
			pCause.connectTo(node, inverted);
		}
		for (final GraphNode nCause : dirCause.negativeCauses) {
			boolean inverted = true;
			if (nCause.equals(invertedNode)) {
				inverted = false;
			}

			nCause.connectTo(node, inverted);
		}
	}

	private void resolveEffect(DirectCause cause, MatchResultTreeNode effect) {
		if (effect.getType().isConjunction()) {
			// Resolve Conjunctions
			resolveEffect(cause, ((BinaryMatchResultTreeNode) effect).getFirstArgument());
			resolveEffect(cause, ((BinaryMatchResultTreeNode) effect).getSecondArgument());
		} else if (effect.getType().isNegation()) {
			// Resolve Negations
			resolveEffect(cause.swapPosNegCauses(), ((NegationTreeNode) effect).getClause());
		} else {
			ConditionVariableNode cvNode = (ConditionVariableNode) effect;
			GraphNode node = currentGraph.createNode(cvNode.getCondition(), cvNode.getVariable(), cause.effectType);
			fullyConnect(cause, node);
		}
	}

	private void fullyConnect(DirectCause dirCause, GraphNode node) {
		for (final GraphNode pCause : dirCause.positiveCauses) {
			pCause.connectTo(node, false);
		}
		for (final GraphNode nCause : dirCause.negativeCauses) {
			nCause.connectTo(node, true);
		}
	}

	private static class DirectCause {
		public List<GraphNode> positiveCauses;
		public List<GraphNode> negativeCauses;
		public NodeType effectType;

		public DirectCause() {
			positiveCauses = new Vector<GraphNode>();
			negativeCauses = new Vector<GraphNode>();
			effectType = NodeType.AND;
		}

		public void addCauses(DirectCause cause) {
			negativeCauses.addAll(cause.negativeCauses);
			positiveCauses.addAll(cause.positiveCauses);
		}

		public static DirectCause mergeCauses(DirectCause... causes) {
			final DirectCause result = new DirectCause();
			for (final DirectCause subCause : causes) {
				result.addCauses(subCause);
			}
			return result;
		}

		public DirectCause swapPosNegCauses() {
			final DirectCause result = new DirectCause();
			result.effectType = effectType == NodeType.AND ? NodeType.OR : NodeType.AND;
			result.negativeCauses = positiveCauses;
			result.positiveCauses = negativeCauses;
			return result;
		}
	}
}
