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
import com.specmate.model.requirements.RequirementsFactory;

public class CiraLabelToCEGTranslator {
	/**
	 * Transforms a given sentence together with an appropriate set of labels into a
	 * graph.
	 *
	 * @param {Sentence} sentence Sentence object
	 * @param {[Label]}  labels List of all labels generated for the sentence
	 * @returns Graph representing the labels of the sentence
	 */
	public void transform(CEGModel ceg, String sentence, Collection<Label> labels) {
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
	private Map<String, List<Label>> getCausalLabels(Collection<Label> allLabels) {
		// gather all labels that begin with Cause and Effect
		Collection<Label> allCausalLabels = getLabelsOfType(allLabels, "Cause");
		allCausalLabels.addAll(getLabelsOfType(allLabels, "Effect"));

		// group all labels, that represent the same event but are distinct (e.g.,
		// because a event is lexically split) in a map
		Map<String, List<Label>> labels = new LinkedHashMap<>();
		for (Label label : allCausalLabels) {
			if (!labels.containsKey(label.label)) {
				labels.put(label.label, new ArrayList<Label>());
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
	private List<Label> getLabelsOfType(Collection<Label> labels, String type) {
		List<Label> relevantLabels = new ArrayList<>();
		for (Label label : labels) {
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
	private Collection<PreCEGNode> generateNodes(Map<String, List<Label>> causalLabels, Collection<Label> allLabels,
			String sentence) {
		var nodes = new ArrayList<PreCEGNode>();
		for (String causalLabel : causalLabels.keySet()) {
			PreCEGNode newnode = generateNode(causalLabel, causalLabels, allLabels, sentence);
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
	private PreCEGNode generateNode(String label, Map<String, List<Label>> causalLabels, Collection<Label> labels,
			String sentence) {
		/* get the variable */
		String variable = "variable";
		List<Label> currentLabels = causalLabels.get(label);
		boolean variableFound = false;
		boolean completedBacktracking = false;

		// this flag represents whether the system had to make assumptions in order to
		// retrieve the variable
		boolean variableAssumed = false;
		while ((currentLabels != null) && !variableFound) {
			List<Label> variableLabels = getLabelsEncompassed(labels, "Variable", currentLabels);
			if (variableLabels.size() == 1) {
				// the causal node contains exactly one variable label: select this label
				variable = sentence.substring(variableLabels.get(0).begin, variableLabels.get(0).end);
				variableFound = true;
			} else if (variableLabels.size() > 1) {
				// the causal node contains multiple variable labels: concatenate the labels
				variable = sentence.substring(variableLabels.get(0).begin, variableLabels.get(0).end);
				for (Label vLabel : variableLabels) {
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
			List<Label> conditionLabels = getLabelsEncompassed(labels, "Condition", currentLabels);
			if (conditionLabels.size() == 1) {
				condition = sentence.substring(conditionLabels.get(0).begin, conditionLabels.get(0).end);
				conditionFound = true;
			} else if (conditionLabels.size() > 1) {
				condition = sentence.substring(conditionLabels.get(0).begin, conditionLabels.get(0).end);
				for (Label cLabel : conditionLabels) {
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

		PreCEGNode node = new PreCEGNode();
		node.setVariable(variable);
		node.setCondition(condition);
		node.setLabels(causalLabels.get(label));
		node.setVariableAssumed(variableAssumed);
		node.setConditionAssumed(conditionAssumed);

		node.negate = isNegated(node, labels, new ArrayList<PreCEGNode>());
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
	private List<Label> getLabelsEncompassed(Collection<Label> allLabels, String type,
			Collection<Label> encompassings) {
		List<Label> relevantlabels = new ArrayList<>();
		for (Label encompassing : encompassings) {
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
	private List<Label> getLabelsInbetween(Collection<Label> labels, String type, int posbegin, int posend) {
		List<Label> relevantlabels = new ArrayList<>();
		for (Label label : labels) {
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
	private List<Label> getNextLabel(String label, Map<String, List<Label>> causalLabels) {
		// Assumption: causalLabels is a map that preserves insertion order
		// (LinkedHashMap)
		// see getCausalLabels
		boolean foundLastLabel = false;
		for (Entry<String, List<Label>> entry : causalLabels.entrySet()) {
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
	private List<Label> getPreviousLabel(String label, Map<String, List<Label>> causalLabels) {
		// Assumption: causalLabels is a map that preserves insertion order
		// (LinkedHashMap)
		// see getCausalLabels
		Entry<String, List<Label>> lastEntry = null;
		for (Entry<String, List<Label>> entry : causalLabels.entrySet()) {
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
	private boolean isNegated(PreCEGNode node, Collection<Label> labels, List<PreCEGNode> additionalNegations) {
		List<Label> negationsWithin = getLabelsEncompassed(labels, "Negation", node.labels);
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
	PreCEGModel generatEdges(Collection<PreCEGNode> nodes, Collection<Label> labels) {
		List<PreCEGEdge> edges = new ArrayList<>();
		List<PreCEGNode> finalnodes = new ArrayList<>();

		List<PreCEGNode> causeNodes = nodes.stream().filter(node -> (node.labels.get(0).label.startsWith("Cause")))
				.collect(Collectors.toList());
		List<PreCEGNode> additionalnegations = new ArrayList<PreCEGNode>();

		PreCEGNode finalcausenode = null;
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
				List<Label> clabels = causeNodes.get(i).labels;
				int endOfCauseNode1 = clabels.get(clabels.size() - 1).end;
				int beginOfCauseNode2 = causeNodes.get(i + 1).labels.get(0).begin;

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

				PreCEGNode intermediatenode = new PreCEGNode();
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
		List<PreCEGNode> effectnodes = nodes.stream().filter(node -> (node.labels.get(0).label.startsWith("Effect")))
				.collect(Collectors.toList());
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
		for (PreCEGNode effectnode : effectnodes) {
			boolean isnodenegated = isNegated(effectnode, labels, additionalnegations);
			edges.add(createEdge(finalcausenode, effectnode, (resolvenegation ? !isnodenegated : isnodenegated)));
		}

		PreCEGModel ceg = new PreCEGModel();
		ceg.setNodes(finalnodes);
		ceg.setEdges(edges);

		return ceg;
	}

	private List<PreCEGNode> identifyAdditionalNegations(Collection<Label> labels, Collection<PreCEGNode> nodes,
			List<String> causejunctors) {
		List<Label> negations = getLabelsOfType(labels, "Negation");
		// filter, which of these labels are already within a cause or effect node
		List<PreCEGNode> causalnodes = nodes.stream().filter(
				node -> (node.labels.get(0).label.startsWith("Cause") || node.labels.get(0).label.startsWith("Effect")))
				.collect(Collectors.toList());
		List<Label> unhandlednegations = new ArrayList<>();
		for (Label negation : negations) {
			boolean negationcovered = false;
			for (PreCEGNode cnode : causalnodes) {
				for (Label label : cnode.labels) {
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
		var negatednodes = new ArrayList<PreCEGNode>();
		if (unhandlednegations.size() > 0) {
			List<PreCEGNode> causenodes = nodes.stream().filter(node -> node.labels.get(0).label.startsWith("Cause"))
					.collect(Collectors.toList());

			for (Label negation : unhandlednegations) {
				// starting from the next causal node after the unhandled negation, all causes
				// joined by conjunctions will be negated
				PreCEGNode nextcausalnode = getNextCausalNode(negation, causenodes);
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
	private PreCEGNode getNextCausalNode(Label label, Collection<PreCEGNode> causalNodes) {
		for (PreCEGNode causalLabel : causalNodes) {
			if (label.end <= causalLabel.labels.get(0).begin) {
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
	private PreCEGEdge createEdge(PreCEGNode source, PreCEGNode target, boolean negate) {
		PreCEGEdge edge = new PreCEGEdge();
		edge.setSource(source);
		edge.setTarget(target);
		edge.setNegate(negate);

		source.outgoingConnections.add(edge);
		target.incomingConnections.add(edge);

		return edge;
	}

	/**
	 * Determine, if the given node is an intermediate node or not
	 *
	 * @param {Node} node Node object
	 * @returns True, if the node has an empty variable and condition, but either
	 *          AND or OR as a type
	 */
	private boolean isIntermediateNode(PreCEGNode node) {
		return StringUtils.isEmpty(node.variable) && StringUtils.isEmpty(node.condition)
				&& (node.type.equals("AND") || node.type.equals("OR"));
	}

	/**
	 * Collapse all intermediate nodes below the current node that are equivalent
	 *
	 * @param {Node}   currentnode The current node below which to collapse
	 * @param {[Node]} nodes List of all nodes
	 * @param {[Edge]} edges List of all edges
	 * @returns The current, where all child nodes are maximally collapsed
	 */
	private PreCEGNode collapseIntermediateNodes(PreCEGNode currentnode, List<PreCEGNode> nodes,
			Collection<PreCEGEdge> edges) {
		if (isIntermediateNode(currentnode)) {
			List<PreCEGNode> childnodes = getChildNodes(currentnode, nodes, edges);

			// get all childnodes which are also intermediate nodes
			List<PreCEGNode> childintermediates = childnodes.stream().filter(node -> isIntermediateNode(node))
					.collect(Collectors.toList());

			if (childintermediates.size() > 0) {
				// recursively collapse child nodes first
				for (PreCEGNode childintermediate : childintermediates) {
					childintermediate = collapseIntermediateNodes(childintermediate, nodes, edges);

					// collapse the two intermediate nodes if they are of the same type
					if (childintermediate.type == currentnode.type) {
						// rewire the edges between the grandchildren and the child to the parent node
						for (var grandchild : getChildNodes(childintermediate, nodes, edges)) {
							PreCEGEdge edge = getEdgeBetween(grandchild, childintermediate, edges);
							edge.setTarget(currentnode);
							currentnode.incomingConnections.add(edge);
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
	private List<PreCEGNode> getChildNodes(PreCEGNode parent, Collection<PreCEGNode> nodes,
			Collection<PreCEGEdge> edges) {
		List<PreCEGNode> children = new ArrayList<>();
		for (PreCEGNode node : nodes) {
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
	private PreCEGEdge getEdgeBetween(PreCEGNode source, PreCEGNode target, Collection<PreCEGEdge> edges) {
		if (source.outgoingConnections.size() == 0 || target.incomingConnections.size() == 0) {
			return null;
		}
		if (source.outgoingConnections.get(0).getTarget() != null) {
			// test case, where edges are always referenced as an object
			for (PreCEGEdge edge : source.outgoingConnections) {
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
	private Collection<PreCEGEdge> removeEdge(PreCEGEdge edge, Collection<PreCEGEdge> edges) {
		edge.getSource().outgoingConnections.remove(edge);
		edge.getTarget().incomingConnections.remove(edge);
		edges.remove(edge);

		return edges;
	}

	private void toSpecmateCEG(CEGModel ceg, PreCEGModel preCegModel) {
		Map<PreCEGNode, CEGNode> nodeMap = new HashMap<>();
		for (PreCEGNode pnode : preCegModel.nodes) {
			CEGNode node = RequirementsFactory.eINSTANCE.createCEGNode();
			node.setVariable(pnode.variable);
			node.setCondition(pnode.condition);
			node.setId(UUIDUtil.generateUUID());
			node.setName(node.getId());
			nodeMap.put(pnode, node);
			ceg.getContents().add(node);
		}
		for (PreCEGEdge pedge : preCegModel.edges) {
			CEGConnection conn = RequirementsFactory.eINSTANCE.createCEGConnection();
			conn.setSource(nodeMap.get(pedge.getSource()));
			conn.setTarget(nodeMap.get(pedge.getTarget()));
			conn.setId(UUIDUtil.generateUUID());
			conn.setName(conn.getId());
			ceg.getContents().add(conn);
		}
	}
}
