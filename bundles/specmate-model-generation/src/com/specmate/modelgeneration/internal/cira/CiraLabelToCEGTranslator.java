package com.specmate.modelgeneration.internal.cira;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.specmate.common.UUIDUtil;
import com.specmate.model.requirements.CEGConnection;
import com.specmate.model.requirements.CEGModel;
import com.specmate.model.requirements.CEGNode;
import com.specmate.model.requirements.NodeType;
import com.specmate.model.requirements.RequirementsFactory;

/**
 * Translates Cira Labels to a CEG. This class has been ported from the
 * javascript version here:
 * https://github.com/JulianFrattini/cira-app/blob/baa38b6e1ca75682101dfade7486fd31575353d6/util/scripts/converters/labelstograph.js
 *
 * @author junkerm
 *
 */
public class CiraLabelToCEGTranslator {
	/**
	 * Transforms a given sentence together with an appropriate set of labels into a
	 * graph.
	 *
	 * @param {Sentence} sentence Sentence object
	 * @param {[Label]}  labels List of all labels generated for the sentence
	 * @returns Graph representing the labels of the sentence
	 */
	public void transform(CEGModel ceg, String sentence, Collection<CiraLabel> labels) {
		// get all causal labels
		var causallabels = getCausalLabels(labels);

		// generate a CEG node for each label
		var nodes = generateNodes(causallabels, labels, sentence);

		// generate the edges between the nodes (and adapt the nodes in the process of
		// collapsing)
		var pceg = generatEdges(nodes, labels);

		toSpecmateCEG(ceg, pceg);

	}

	/**
	 * Returns a list of all cause and effect nodes
	 *
	 * @param {[Label]} alllabels List of label documents
	 * @returns List of label documents that represent either a cause or effect
	 */
	private Map<String, List<CiraLabel>> getCausalLabels(Collection<CiraLabel> allLabels) {
		// gather all labels that begin with Cause and Effect
		Collection<CiraLabel> allCausalLabels = getLabelsOfType(allLabels, "Cause");
		allCausalLabels.addAll(getLabelsOfType(allLabels, "Effect"));

		// group all labels, that represent the same event but are distinct (e.g.,
		// because a event is lexically split) in a map
		Map<String, List<CiraLabel>> labels = new LinkedHashMap<>();
		for (CiraLabel label : allCausalLabels) {
			if (!labels.containsKey(label.label)) {
				labels.put(label.label, new ArrayList<CiraLabel>());
			}
			labels.get(label.label).add(label);
		}

		return labels;
	}

	/**
	 * Returns a list of all labels with a specific type
	 *
	 * @param {[Label]} labels List of label documents
	 * @param {String}  type Identifier of the label (e.g., "Cause" or "Keyword")
	 * @returns List of labels where the "label" attribute starts with the given
	 *          type
	 */
	private List<CiraLabel> getLabelsOfType(Collection<CiraLabel> labels, String type) {
		List<CiraLabel> relevantLabels = new ArrayList<>();
		for (CiraLabel label : labels) {
			if (label.label.startsWith(type)) {
				relevantLabels.add(label);
			}
		}
		return relevantLabels;
	}

	/**
	 * Generate all nodes from the causal labels
	 *
	 * @param {[Labels]} causallabels List of "Causex" and "Effectx" Labels
	 * @param {[Labels]} alllabels List of all labels
	 * @param {String}   sentence Sentence text
	 * @returns List of causal nodes
	 */
	private Collection<CiraCEGNode> generateNodes(Map<String, List<CiraLabel>> causalLabels,
			Collection<CiraLabel> allLabels, String sentence) {
		var nodes = new ArrayList<CiraCEGNode>();
		for (String causalLabel : causalLabels.keySet()) {
			CiraCEGNode newnode = generateNode(causalLabel, causalLabels, allLabels, sentence);
			nodes.add(newnode);
		}
		return nodes;
	}

	/**
	 * Generate a causal node from a label
	 *
	 * @param {Label}   eventlabel Label of the causal node
	 * @param {[Label]} causallabels Dict containing all events associated to all
	 *                  respective labels
	 * @param {[Label]} alllabels List of all labels
	 * @param {String}  sentence Sentence text
	 * @returns Causal node representing the causal label
	 */
	private CiraCEGNode generateNode(String label, Map<String, List<CiraLabel>> causalLabels,
			Collection<CiraLabel> labels, String sentence) {
		/* get the variable */
		String variable = "variable";
		List<CiraLabel> currentLabels = causalLabels.get(label);
		boolean variableFound = false;
		boolean completedBacktracking = false;

		// this flag represents whether the system had to make assumptions in order to
		// retrieve the variable
		boolean variableAssumed = false;
		while ((currentLabels != null) && !variableFound) {
			List<CiraLabel> variableLabels = getLabelsEncompassed(labels, "Variable", currentLabels);
			if (variableLabels.size() == 1) {
				// the causal node contains exactly one variable label: select this label
				variable = sentence.substring(variableLabels.get(0).begin, variableLabels.get(0).end);
				variableFound = true;
			} else if (variableLabels.size() > 1) {
				// the causal node contains multiple variable labels: concatenate the labels
				variable = sentence.substring(variableLabels.get(0).begin, variableLabels.get(0).end);
				for (CiraLabel vLabel : variableLabels) {
					variable = variable + " " + sentence.substring(vLabel.begin, vLabel.end);
				}
				variableFound = true;
			} else if (variableLabels.size() == 0) {
				variableAssumed = true;
				// the causal node contains no variable labels: try to get the variable from a
				// different causal node
				/* TODO prioritize events of the same type (Cause/Effect) over the others */
				if (!completedBacktracking) {
					// prioritize previous nodes, as this is where the variable is most likely
					// located
					currentLabels = getPreviousLabel(currentLabels.get(0).label, causalLabels);
					if (currentLabels == null) {
						// reached the first label
						currentLabels = causalLabels.get(label);
						completedBacktracking = true;
					}
				} else {
					currentLabels = getNextLabel(currentLabels.get(0).label, causalLabels);
					if (currentLabels == null) {
						// reached the last label
						variableFound = true;
					}
				}
			}
		}

		/* get the condition */
		String condition = "is present";
		currentLabels = causalLabels.get(label);
		boolean conditionFound = false;
		boolean completedFronttracking = false;
		boolean conditionAssumed = false;

		while ((currentLabels != null) && !conditionFound) {
			List<CiraLabel> conditionLabels = getLabelsEncompassed(labels, "Condition", currentLabels);
			if (conditionLabels.size() == 1) {
				condition = sentence.substring(conditionLabels.get(0).begin, conditionLabels.get(0).end);
				conditionFound = true;
			} else if (conditionLabels.size() > 1) {
				condition = sentence.substring(conditionLabels.get(0).begin, conditionLabels.get(0).end);
				for (CiraLabel cLabel : conditionLabels) {
					condition = condition + " " + sentence.substring(cLabel.begin, cLabel.end);
				}
				conditionFound = true;
			} else if (conditionLabels.size() == 0) {
				conditionAssumed = true;
				if (!completedFronttracking) {
					currentLabels = getNextLabel(currentLabels.get(0).label, causalLabels);
					if (currentLabels == null) {
						currentLabels = causalLabels.get(label);
						completedFronttracking = true;
					}
				} else {
					currentLabels = getPreviousLabel(currentLabels.get(0).label, causalLabels);
					if (currentLabels == null) {
						conditionFound = true;
					}
				}
			}
		}

		/* construct the node */

		CiraCEGNode node = new CiraCEGNode();
		node.setVariable(variable);
		node.setCondition(condition);
		node.setLabels(causalLabels.get(label));
		node.setVariableAssumed(variableAssumed);
		node.setConditionAssumed(conditionAssumed);

		node.setNegate(isNegated(node, labels, new ArrayList<CiraCEGNode>()));
		return node;
	}

	/**
	 * Similar to getlabelsinbetween, but now returns all labels contained by a list
	 * of labels
	 *
	 * @param {*} alllabels List of all label documents
	 * @param {*} type Identifier of the label
	 * @param {*} encompassings List of labels that represent the range in which is
	 *            to be searched
	 * @returns List of labels with the given type within the encompassing labels
	 */
	private List<CiraLabel> getLabelsEncompassed(Collection<CiraLabel> allLabels, String type,
			Collection<CiraLabel> encompassings) {
		List<CiraLabel> relevantlabels = new ArrayList<>();
		for (CiraLabel encompassing : encompassings) {
			relevantlabels.addAll(getLabelsInbetween(allLabels, type, encompassing.begin, encompassing.end));
		}
		return relevantlabels;
	}

	/**
	 * Traverse all labels and find the label within the constraining index range
	 * [posbegin; posend] that has the given Type
	 *
	 * @param {[Label]} labels List of label documents
	 * @param {String}  type Identifier of the label (e.g., "Cause" or "Keyword")
	 * @param {Number}  posbegin Begin index of the constraining range
	 * @param {Number}  posend End index of the constraining range
	 * @returns Label with the given type within the constraining index range
	 */
	private List<CiraLabel> getLabelsInbetween(Collection<CiraLabel> labels, String type, int posbegin, int posend) {
		List<CiraLabel> relevantlabels = new ArrayList<>();
		for (CiraLabel label : labels) {
			if (label.begin >= posbegin && label.end <= posend && label.label.equals(type)) {
				relevantlabels.add(label);
			}
		}
		return relevantlabels;
	}

	/**
	 * Returns the previous Cause- or Effect-label that follows the given label
	 *
	 * @param {Object}   label The given label
	 * @param {Object[]} causallabels List of causal nodes
	 * @returns The next causal label after the given label, if there is one
	 */
	private List<CiraLabel> getNextLabel(String label, Map<String, List<CiraLabel>> causalLabels) {
		// Assumption: causalLabels is a map that preserves insertion order
		// (LinkedHashMap)
		// see getCausalLabels
		boolean foundLastLabel = false;
		for (Entry<String, List<CiraLabel>> entry : causalLabels.entrySet()) {
			if (foundLastLabel) {
				return entry.getValue();
			}
			if (entry.getKey().equals(label)) {
				foundLastLabel = true;
			}
		}
		return null;
	}

	/**
	 * Returns the previous Cause- or Effect-label that precedes the given label
	 *
	 * @param {Object}   label The given label
	 * @param {Object[]} causallabels List of causal nodes
	 * @returns The previous causal label before the given label, if there is one
	 */
	private List<CiraLabel> getPreviousLabel(String label, Map<String, List<CiraLabel>> causalLabels) {
		// Assumption: causalLabels is a map that preserves insertion order
		// (LinkedHashMap)
		// see getCausalLabels
		Entry<String, List<CiraLabel>> lastEntry = null;
		for (Entry<String, List<CiraLabel>> entry : causalLabels.entrySet()) {
			if (entry.getKey().equals(label)) {
				if (lastEntry != null) {
					return lastEntry.getValue();
				} else {
					return null;
				}
			} else {
				lastEntry = entry;
			}
		}
		return null;
	}

	/**
	 * Determine, if the given node is negated
	 *
	 * @param {Node}     node Node object in question
	 * @param {[Label]}  labels List of labels
	 * @param {Object[]} additionalnegations List of nodes that are additionally
	 *                   negated
	 * @returns True, if a negation label is encompassed by the node label
	 */
	private boolean isNegated(CiraCEGNode node, Collection<CiraLabel> labels, List<CiraCEGNode> additionalNegations) {
		List<CiraLabel> negationsWithin = getLabelsEncompassed(labels, "Negation", node.getLabels());
		if (additionalNegations.indexOf(node) == -1) {
			// if the node is not already negated from an outside node (e.g., exceptive
			// clause), return true if the number of negations is uneven (to deal with
			// double negatives)
			return negationsWithin.size() > 0 && negationsWithin.size() % 2 != 0;
		} else {
			// if the node is not already negated from an outside node (e.g., exceptive
			// clause), invert the result
			return !(negationsWithin.size() > 0 && negationsWithin.size() % 2 != 0);
		}
	}

	/**
	 * Constructs a cause-effect-graph (CEG) by generating intermediate nodes and
	 * connecting all nodes via edges
	 *
	 * @param {[Node]}  nodes List of all nodes
	 * @param {[Label]} labels List of all labels
	 * @returns Cause-effect-graph representing the sentence
	 */
	CiraCEGModel generatEdges(Collection<CiraCEGNode> nodes, Collection<CiraLabel> labels) {
		List<CiraCEGEdge> edges = new ArrayList<>();
		List<CiraCEGNode> finalnodes = new ArrayList<>();

		List<CiraCEGNode> causeNodes = nodes.stream()
				.filter(node -> (node.getLabels().get(0).label.startsWith("Cause"))).collect(Collectors.toList());
		List<CiraCEGNode> additionalnegations = new ArrayList<CiraCEGNode>();

		CiraCEGNode finalcausenode = null;
		// resolve junctors between causal nodes
		if (causeNodes.size() > 1) {
			// find all explicit junctors
			ArrayList<String> causejunctors = new ArrayList<>();
			// keep track of priorizations that break the standard precedence rule (e.g.: in
			// "e1 and either e2 and e3", the disjunction has higher precedence than the
			// conjunction)
			LinkedList<Integer> priorityjunctors = new LinkedList<>();
			String priorityjunctor = "";

			for (var i = 0; i < causeNodes.size() - 1; i++) {
				// get the indices of the space between the two adjacent cause nodes
				List<CiraLabel> clabels = causeNodes.get(i).getLabels();
				int endOfCauseNode1 = clabels.get(clabels.size() - 1).end;
				int beginOfCauseNode2 = causeNodes.get(i + 1).getLabels().get(0).begin;

				// count the occurrences of conjunctions and disjunctions in this space
				int nconjunctions = getLabelsInbetween(labels, "Conjunction", endOfCauseNode1, beginOfCauseNode2)
						.size();
				int ndisjunctions = getLabelsInbetween(labels, "Disjunction", endOfCauseNode1, beginOfCauseNode2)
						.size();

				// determine the junctor at the index position based on the occurrences
				if (nconjunctions > 0 && ndisjunctions == 0) {
					causejunctors.add(i, "AND");
					// reset the priority propagation
					if (priorityjunctor.equals("OR")) {
						priorityjunctor = "";
					}
				} else if (nconjunctions == 0 && ndisjunctions > 0) {
					causejunctors.add(i, "OR");
					// propagate the priority if it exists
					if (priorityjunctor.equals("OR")) {
						priorityjunctors.add(i);
					}
				} else if (nconjunctions > 0 && ndisjunctions > 0) {
					causejunctors.add(i, "AND");
					priorityjunctor = "OR";
				} else {
					causejunctors.add(i, "missing");
					if (priorityjunctor.equals("OR")) {
						priorityjunctors.add(i);
					}
				}
			}

			// if the last junctor between causes is missing, it is likely that all junctors
			// are implicit, hence assume a conjunction
			if (causejunctors.get(causejunctors.size() - 1).equals("missing")) {
				causejunctors.set(causejunctors.size() - 1, "AND");
			}
			// fill all implicit junctors by assuming the subsequent junctor
			for (var i = causejunctors.size() - 2; i >= 0; i--) {
				if (causejunctors.get(i).equals("missing")) {
					causejunctors.set(i, causejunctors.get(i + 1));
				}
			}

			// handle unhandled negations
			additionalnegations = identifyAdditionalNegations(labels, nodes, causejunctors);

			// construct intermediate nodes
			while (causejunctors.size() > 0) {
				// follow precedence: conjunctions usually bind stronger than disjunctions,
				// except in the special case (see above)
				String currentjunctor = (causejunctors.indexOf("AND") != -1) ? "AND" : "OR";
				int index = causejunctors.indexOf(currentjunctor);
				// in case of prioritized junctors, select those
				if (priorityjunctors.size() > 0) {
					index = priorityjunctors.pop();
					currentjunctor = causejunctors.get(index);
				}

				// create an intermediate node with the current junctor

				CiraCEGNode intermediatenode = new CiraCEGNode();
				intermediatenode.setVariable("");
				intermediatenode.setCondition("");
				intermediatenode.setType(currentjunctor);

				// create an edge from the two adjacent nodes to the new intermediate node
				boolean negated = false;
				if (!isIntermediateNode(causeNodes.get(index))) {
					negated = isNegated(causeNodes.get(index), labels, additionalnegations);
				}
				edges.add(createEdge(causeNodes.get(index), intermediatenode, negated));
				if (!isIntermediateNode(causeNodes.get(index + 1))) {
					negated = isNegated(causeNodes.get(index + 1), labels, additionalnegations);
				}
				edges.add(createEdge(causeNodes.get(index + 1), intermediatenode, negated));

				// remove the two nodes and store them in the final set of nodes
				finalnodes.add(causeNodes.get(index));
				finalnodes.add(causeNodes.get(index + 1));

				// replace the two nodes by the intermediate node
				causeNodes.remove(index);
				causeNodes.remove(index);
				causeNodes.add(intermediatenode);

				// remove the junctor
				causejunctors.remove(index);
			}

			// add the final node of the tree to the list of final nodes
			finalcausenode = causeNodes.get(0);
			finalnodes.add(causeNodes.get(0));

			// collapse equivalent nodes
			finalcausenode = collapseIntermediateNodes(finalcausenode, finalnodes, edges);
		} else {
			finalcausenode = causeNodes.get(0);
			finalnodes.add(causeNodes.get(0));
		}

		// add the effects to the final nodes
		List<CiraCEGNode> effectnodes = nodes.stream()
				.filter(node -> (node.getLabels().get(0).label.startsWith("Effect"))).collect(Collectors.toList());
		finalnodes.addAll(effectnodes);

		// if the cause nodes consist of only one single cause node, there will not be
		// any intermediate nodes hence double negations must be resolved manually
		var resolvenegation = false;
		if (!isIntermediateNode(finalcausenode)) {
			if (isNegated(finalcausenode, labels, additionalnegations)) {
				// the resolvenegation flag indicates, that the edge between the final causal
				// node and the effect nodes has to be negated
				resolvenegation = true;
			}
		}
		// create edges from the final causal node to all effect nodes
		for (CiraCEGNode effectnode : effectnodes) {
			boolean isnodenegated = isNegated(effectnode, labels, additionalnegations);
			edges.add(createEdge(finalcausenode, effectnode, (resolvenegation ? !isnodenegated : isnodenegated)));
		}

		CiraCEGModel ceg = new CiraCEGModel();
		ceg.setNodes(finalnodes);
		ceg.setEdges(edges);

		return ceg;
	}

	private List<CiraCEGNode> identifyAdditionalNegations(Collection<CiraLabel> labels, Collection<CiraCEGNode> nodes,
			List<String> causejunctors) {
		List<CiraLabel> negations = getLabelsOfType(labels, "Negation");
		// filter, which of these labels are already within a cause or effect node
		List<CiraCEGNode> causalnodes = nodes.stream().filter(node -> (node.getLabels().get(0).label.startsWith("Cause")
				|| node.getLabels().get(0).label.startsWith("Effect"))).collect(Collectors.toList());
		List<CiraLabel> unhandlednegations = new ArrayList<>();
		for (CiraLabel negation : negations) {
			boolean negationcovered = false;
			for (CiraCEGNode cnode : causalnodes) {
				for (CiraLabel label : cnode.getLabels()) {
					if (label.begin <= negation.begin && label.end >= negation.end) {
						negationcovered = true;
						break;
					}
				}
			}

			if (!negationcovered) {
				unhandlednegations.add(negation);
			}
		}

		// determine, which of the causal nodes are additionally negated by the
		// unhandled negations
		var negatednodes = new ArrayList<CiraCEGNode>();
		if (unhandlednegations.size() > 0) {
			List<CiraCEGNode> causenodes = nodes.stream()
					.filter(node -> node.getLabels().get(0).label.startsWith("Cause")).collect(Collectors.toList());

			for (CiraLabel negation : unhandlednegations) {
				// starting from the next causal node after the unhandled negation, all causes
				// joined by conjunctions will be negated
				CiraCEGNode nextcausalnode = getNextCausalNode(negation, causenodes);
				var negatedindex = causenodes.indexOf(nextcausalnode);

				negatednodes.add(nextcausalnode);
				while (causejunctors.get(negatedindex).equals("AND")) {
					negatedindex++;
					negatednodes.add(causalnodes.get(negatedindex));
				}
			}
		}
		return negatednodes;
	}

	/**
	 * Returns the next Cause- or Effect-node that follows the given label
	 *
	 * @param {Object}   label The given label
	 * @param {Object[]} causalnodes List of causal nodes
	 * @returns The next causal node after the given label, if there is one
	 */
	private CiraCEGNode getNextCausalNode(CiraLabel label, Collection<CiraCEGNode> causalNodes) {
		for (CiraCEGNode causalLabel : causalNodes) {
			if (label.end <= causalLabel.getLabels().get(0).begin) {
				return causalLabel;
			}
		}
		return null;
	}

	/**
	 * Create an edge between the source and target node
	 *
	 * @param {Node}   source Start of the edge
	 * @param {Node}   target End of the edge
	 * @param {Boolen} negate True, if the edge is to be negated
	 * @returns The created edge
	 */
	private CiraCEGEdge createEdge(CiraCEGNode source, CiraCEGNode target, boolean negate) {
		CiraCEGEdge edge = new CiraCEGEdge();
		edge.setSource(source);
		edge.setTarget(target);
		edge.setNegate(negate);

		source.getOutgoingConnections().add(edge);
		target.getIncomingConnections().add(edge);

		return edge;
	}

	/**
	 * Determine, if the given node is an intermediate node or not
	 *
	 * @param {Node} node Node object
	 * @returns True, if the node has an empty variable and condition, but either
	 *          AND or OR as a type
	 */
	private boolean isIntermediateNode(CiraCEGNode node) {
		return StringUtils.isEmpty(node.getVariable()) && StringUtils.isEmpty(node.getCondition())
				&& (node.getType().equals("AND") || node.getType().equals("OR"));
	}

	/**
	 * Collapse all intermediate nodes below the current node that are equivalent
	 *
	 * @param {Node}   currentnode The current node below which to collapse
	 * @param {[Node]} nodes List of all nodes
	 * @param {[Edge]} edges List of all edges
	 * @returns The current, where all child nodes are maximally collapsed
	 */
	private CiraCEGNode collapseIntermediateNodes(CiraCEGNode currentnode, List<CiraCEGNode> nodes,
			Collection<CiraCEGEdge> edges) {
		if (isIntermediateNode(currentnode)) {
			List<CiraCEGNode> childnodes = getChildNodes(currentnode, nodes, edges);

			// get all childnodes which are also intermediate nodes
			List<CiraCEGNode> childintermediates = childnodes.stream().filter(node -> isIntermediateNode(node))
					.collect(Collectors.toList());

			if (childintermediates.size() > 0) {
				// recursively collapse child nodes first
				for (CiraCEGNode childintermediate : childintermediates) {
					childintermediate = collapseIntermediateNodes(childintermediate, nodes, edges);

					// collapse the two intermediate nodes if they are of the same type
					if (childintermediate.getType() == currentnode.getType()) {
						// rewire the edges between the grandchildren and the child to the parent node
						for (var grandchild : getChildNodes(childintermediate, nodes, edges)) {
							CiraCEGEdge edge = getEdgeBetween(grandchild, childintermediate, edges);
							edge.setTarget(currentnode);
							currentnode.getIncomingConnections().add(edge);
						}

						// delete the obsolete edge from the child node to the parent node
						edges = removeEdge(getEdgeBetween(childintermediate, currentnode, edges), edges);

						// delete the obsolete intermediate node
						nodes.remove(childintermediate);
					}
				}
			}
			return currentnode;
		} else {
			return currentnode;
		}
	}

	/**
	 * Retrieve all child nodes of the given parent node
	 *
	 * @param {Node}   parent Parent node, of which the child nodes are of interest
	 * @param {[Node]} nodes List of all nodes
	 * @param {[Edge]} edges List of all edges
	 * @returns List of all nodes, which are the source node of an edge where the
	 *          target node is the parent
	 */
	private List<CiraCEGNode> getChildNodes(CiraCEGNode parent, Collection<CiraCEGNode> nodes,
			Collection<CiraCEGEdge> edges) {
		List<CiraCEGNode> children = new ArrayList<>();
		for (CiraCEGNode node : nodes) {
			if (getEdgeBetween(node, parent, edges) != null) {
				children.add(node);
			}
		}
		return children;
	}

	/**
	 * Retrieve the edge between the source and target node
	 *
	 * @param {Node} source Source node, where the edge starts
	 * @param {Node} target Target node, where the edge ends
	 * @returns Edge from the source to the target node, if it exists
	 */
	private CiraCEGEdge getEdgeBetween(CiraCEGNode source, CiraCEGNode target, Collection<CiraCEGEdge> edges) {
		if (source.getOutgoingConnections().size() == 0 || target.getIncomingConnections().size() == 0) {
			return null;
		}
		if (source.getOutgoingConnections().get(0).getTarget() != null) {
			// test case, where edges are always referenced as an object
			for (CiraCEGEdge edge : source.getOutgoingConnections()) {
				if (edge.getTarget().equals(target)) {
					return edge;
				}
			}
			return null;
		} else {
			return edges.stream().filter(edge -> edge.getSource().equals(source) && edge.getTarget().equals(target))
					.findFirst().get();
		}
	}

	/**
	 * Remove an edge between the source and target node
	 *
	 * @param {Edge}  edge Edge object
	 * @param {Edges} edges List of all edge objects
	 * @returns Updated list of edge objects
	 */
	private Collection<CiraCEGEdge> removeEdge(CiraCEGEdge edge, Collection<CiraCEGEdge> edges) {
		edge.getSource().getOutgoingConnections().remove(edge);
		edge.getTarget().getIncomingConnections().remove(edge);
		edges.remove(edge);

		return edges;
	}

	private void toSpecmateCEG(CEGModel ceg, CiraCEGModel preCegModel) {
		Map<CiraCEGNode, CEGNode> nodeMap = new HashMap<>();
		removeSuperfluousNodes(preCegModel);
		int intermediateNodeCounter = 1;
		for (CiraCEGNode pnode : preCegModel.nodes) {
			CEGNode node = RequirementsFactory.eINSTANCE.createCEGNode();
			String var = pnode.getVariable();
			String cond = pnode.getCondition();
			if (StringUtils.isEmpty(var) && StringUtils.isEmpty(cond)) {
				var = "IN";
				cond = "" + intermediateNodeCounter++;
			}
			node.setVariable(var);
			node.setCondition(cond);
			node.setId(UUIDUtil.generateUUID());
			node.setName(node.getId());
			node.setType(pnode.getType() == null || pnode.getType().equals("AND") ? NodeType.AND : NodeType.OR);
			nodeMap.put(pnode, node);
			ceg.getContents().add(node);
		}
		for (CiraCEGEdge pedge : preCegModel.edges) {
			CEGConnection conn = RequirementsFactory.eINSTANCE.createCEGConnection();
			conn.setSource(nodeMap.get(pedge.getSource()));
			conn.setTarget(nodeMap.get(pedge.getTarget()));
			conn.setId(UUIDUtil.generateUUID());
			conn.setName(conn.getId());
			conn.setNegate(pedge.isNegate());
			ceg.getContents().add(conn);
		}

	}

	/**
	 * Removes intermediate node with no variable and no condition that have a
	 * single outgoing edge to node with only one incoming edge
	 *
	 * @param preCegModel
	 */
	private void removeSuperfluousNodes(CiraCEGModel preCegModel) {
		List<CiraCEGNode> toRemove = preCegModel.nodes.stream().filter(n -> true &&
		// only one outgoing connection
				n.getOutgoingConnections().size() == 1 &&
				// target node has only one incoming connection
				n.getOutgoingConnections().get(0).getTarget().getIncomingConnections().size() == 1 &&
				// condition empty
				StringUtils.isEmpty(n.getCondition()) &&
				// variable empty
				StringUtils.isEmpty(n.getVariable())).collect(Collectors.toList());

		for (CiraCEGNode n : toRemove) {
			preCegModel.nodes.remove(n);
			CiraCEGEdge outEdge = n.getOutgoingConnections().get(0);
			preCegModel.edges.remove(outEdge);
			CiraCEGNode target = outEdge.getTarget();
			target.setType(n.getType());
			target.getIncomingConnections().clear();
			target.getIncomingConnections().addAll(n.getIncomingConnections());
			for (CiraCEGEdge e : n.getIncomingConnections()) {
				e.setTarget(target);
			}
		}
	}
}
