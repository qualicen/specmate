package com.specmate.testspecification.internal.generators;

import static com.specmate.model.support.util.SpecmateEcoreUtil.getCondition;
import static com.specmate.model.support.util.SpecmateEcoreUtil.getIncomingConnections;
import static com.specmate.model.support.util.SpecmateEcoreUtil.getType;
import static com.specmate.model.support.util.SpecmateEcoreUtil.getVariable;
import static com.specmate.model.support.util.SpecmateEcoreUtil.pickInstancesOf;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.osgi.service.log.Logger;
import org.sat4j.core.VecInt;
import org.sat4j.maxsat.SolverFactory;
import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.pb.IPBSolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.GateTranslator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BoundType;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.specmate.common.AssertUtil;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.base.BasePackage;
import com.specmate.model.base.IContainer;
import com.specmate.model.base.IModelConnection;
import com.specmate.model.base.IModelNode;
import com.specmate.model.requirements.CEGConnection;
import com.specmate.model.requirements.CEGLinkedNode;
import com.specmate.model.requirements.CEGModel;
import com.specmate.model.requirements.CEGNode;
import com.specmate.model.requirements.NodeType;
import com.specmate.model.requirements.RequirementsFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.ParameterAssignment;
import com.specmate.model.testspecification.ParameterType;
import com.specmate.model.testspecification.TestCase;
import com.specmate.model.testspecification.TestParameter;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestspecificationFactory;
import com.specmate.testspecification.internal.generators.TaggedBoolean.ETag;

public class CEGTestCaseGenerator extends TestCaseGeneratorBase<CEGModel, CEGNode> {

	private static final String HIDDEN = "___HIDDEN___";
	private Comparator<CEGNodeEvaluation> nodeEvalSetComparator;
	private boolean considerLinks;
	private Logger logger;
	private boolean germanLanguage;

	private final String boundaryRegex = "\\<=\\s*(?<leq>\\d+([,\\.]\\d+)?)|" // x <= 5
			+ "\\<\\s*(?<lt>\\d+([,\\.]\\d+)?)|" // x < 5
			+ "\\>=\\s*(?<geq>\\d+([,\\.]\\d+)?)|" // x >= 5
			+ "\\>\\s*(?<gt>\\d+([,\\.]\\d+)?)|" // x >5
			+ "(?<geq2>\\d+([,\\.]\\d+)?)\\s*\\<=|" // 5 <= x
			+ "(?<gt2>\\d+([,\\.]\\d+)?)\\s*\\<|" // 5 < x
			+ "(?<leq2>\\d+([,\\.]\\d+)?)\\s*\\>=|" // 5 >= x
			+ "(?<lt2>\\d+([,\\.]\\d+)?)\\s*\\>"; // 5 > x
	private final Pattern boundaryPattern = Pattern.compile(boundaryRegex);
	private Map<String, EObject> tempObjects = new HashMap<>();
	private DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
	private DecimalFormat valueFormat = new DecimalFormat("0.#####", symbols);

	private Multimap<CEGNode, Range<Double>> rangeMap = ArrayListMultimap.create();
	private List<Pair<String, Double>> valueNodes = new ArrayList<>();

	private enum ECompOperator {
		LEQ, LT, GEQ, GT
	}

	public CEGTestCaseGenerator(TestSpecification specification, boolean considerLinks, boolean boundaryAnalysis,
			boolean germanLanguage, Logger logger) {
		super(specification, CEGModel.class, CEGNode.class);
		this.considerLinks = considerLinks;
		this.germanLanguage = germanLanguage;
		this.logger = logger;
		if (considerLinks) {
			addLinkedNodes();
		}
		if (boundaryAnalysis) {
			addBoundaryNodes();
		}
	}

	/**
	 * Enlarges the node set by the nodes (from other model) reachable via linked
	 * nodes
	 */
	private void addLinkedNodes() {
		// fetch the linked nodes in the original model, this will be our worklist
		LinkedList<CEGLinkedNode> linkedNodes = new LinkedList<>(pickInstancesOf(nodes, CEGLinkedNode.class));

		// these are the linked nodes we already handled (needed to avoid cycles)
		Set<CEGLinkedNode> handledLinkedNodes = new HashSet<CEGLinkedNode>();

		while (!linkedNodes.isEmpty()) {
			// take the first linked node from the work list
			CEGLinkedNode linkedNode = linkedNodes.pop();
			// add to handled list to signal that we don't need to handle this one again
			handledLinkedNodes.add(linkedNode);
			if (linkedNode.getLinkTo() != null) {
				// get all nodes reachable from the linked node via the incomingConnections
				// relation
				Set<CEGNode> newNodes = getReachableNodes(linkedNode.getLinkTo());
				// remove the linked node from the collection (it is represented by the linking
				// node)
				newNodes.remove(linkedNode.getLinkTo());
				// add all the reachable nodes to our global nodes list (used for generating
				// tests)
				nodes.addAll(newNodes);
				// there might be more linked nodes in the collections of reachable nodes ...
				List<CEGLinkedNode> newlinkedNodes = pickInstancesOf(List.copyOf(newNodes), CEGLinkedNode.class);
				// ... remove the ones that we already handled ...
				newlinkedNodes.removeAll(handledLinkedNodes);
				// ... and add them to the worklist
				linkedNodes.addAll(newlinkedNodes);
			}
		}
	}

	private void addBoundaryNodes() {
		for (IModelNode node : nodes) {
			CEGNode cegNode = (CEGNode) node;
			if (!isCause(cegNode)) {
				continue;
			}
			addBoundaryNodesForNode(cegNode);
			cegNode.setType(NodeType.OR);
		}
		updateModelWithBoundaryNodes();
	}

	private void updateModelWithBoundaryNodes() {
		for (Pair<String, Double> entry : valueNodes) {
			CEGNode valueNode = createValueNode(entry.getKey(), entry.getValue());
			for (CEGNode rangeNode : rangeMap.keySet()) {
				if (entry.getKey().equals(rangeNode.getVariable())) {
					Collection<Range<Double>> ranges = rangeMap.get(rangeNode);
					if (ranges.stream().allMatch(r -> r.contains(entry.getValue()))) {
						connect(valueNode, rangeNode);
					}
				}
			}

			CEGNode hidden = createHiddenNode(entry.getKey());
			connect(valueNode, hidden);

		}
	}

	private void addBoundaryNodesForNode(CEGNode cegNode) {
		String cond = cegNode.getCondition();
		Matcher matcher = boundaryPattern.matcher(cond);
		while (matcher.find()) {
			String leqCondition = matcher.group("leq");
			if (leqCondition != null) {
				handleCondition(cegNode, ECompOperator.LEQ, leqCondition);
			}

			String leqCondition2 = matcher.group("leq2");
			if (leqCondition2 != null) {
				handleCondition(cegNode, ECompOperator.LEQ, leqCondition2);
			}

			String ltCondition = matcher.group("lt");
			if (ltCondition != null) {
				handleCondition(cegNode, ECompOperator.LT, ltCondition);
			}

			String ltCondition2 = matcher.group("lt2");
			if (ltCondition2 != null) {
				handleCondition(cegNode, ECompOperator.LT, ltCondition2);
			}

			String geqCondition = matcher.group("geq");
			if (geqCondition != null) {
				handleCondition(cegNode, ECompOperator.GEQ, geqCondition);
			}

			String geqCondition2 = matcher.group("geq2");
			if (geqCondition2 != null) {
				handleCondition(cegNode, ECompOperator.GEQ, geqCondition2);
			}

			String gtCondition = matcher.group("gt");
			if (gtCondition != null) {
				handleCondition(cegNode, ECompOperator.GT, gtCondition);
			}

			String gtCondition2 = matcher.group("gt2");
			if (gtCondition2 != null) {
				handleCondition(cegNode, ECompOperator.GT, gtCondition2);
			}
		}
	}

	private void handleCondition(CEGNode cegNode, ECompOperator op, String condition) {
		double leqValue;
		try {
			leqValue = Double.parseDouble(condition);
		} catch (Exception e) {
			return;
		}

		Boundaries boundaries = getBoundaryValues(leqValue);
		String nodeVar = cegNode.getVariable();

//		createValueNode(nodeVar, boundaries.min);
//		createValueNode(nodeVar, boundaries.eq);
//		createValueNode(nodeVar, boundaries.max);

		valueNodes.add(Pair.of(nodeVar, boundaries.min));
		valueNodes.add(Pair.of(nodeVar, boundaries.eq));
		valueNodes.add(Pair.of(nodeVar, boundaries.max));
		Range<Double> range;

		switch (op) {
		case LEQ:
			range = Range.upTo(leqValue, BoundType.CLOSED);
			rangeMap.put(cegNode, range);
			break;
		case LT:
			range = Range.upTo(leqValue, BoundType.OPEN);
			rangeMap.put(cegNode, range);
			break;
		case GEQ:
			range = Range.downTo(leqValue, BoundType.CLOSED);
			rangeMap.put(cegNode, range);
			break;
		case GT:
			range = Range.downTo(leqValue, BoundType.OPEN);
			rangeMap.put(cegNode, range);
			break;
		}
	}

	private CEGConnection connect(CEGNode node1, CEGNode node2) {
		String key = node1.getVariable() + "_" + node1.getCondition() + "_" + node2.getVariable() + "_"
				+ node2.getCondition();
		CEGConnection conn = (CEGConnection) tempObjects.get(key);
		if (conn == null) {
			conn = SpecmateEcoreUtil.connect(node1, node2);
			tempObjects.put(key, conn);
		}
		return conn;
	}

	private CEGNode createValueNode(String var, double val) {
		String key = var + "_" + val;
		CEGNode node = (CEGNode) tempObjects.get(key);
		if (node == null) {
			node = RequirementsFactory.eINSTANCE.createCEGNode();
			node.setVariable(var);
			node.setCondition("= " + valueFormat.format(val));
			tempObjects.put(key, node);
			nodes.add(node);
		}
		return node;
	}

	private CEGNode createHiddenNode(String var) {
		String key = HIDDEN + var;
		CEGNode node = (CEGNode) tempObjects.get(key);
		if (node == null) {
			node = RequirementsFactory.eINSTANCE.createCEGNode();
			node.setVariable(HIDDEN + var);
			node.setCondition("is present");
			node.setType(NodeType.OR);
			tempObjects.put(key, node);
			nodes.add(node);
		}
		return node;
	}

	private Boundaries getBoundaryValues(double value) {
		double min = Math.floor(value);
		if (min == value) {
			min = value - 1;
		}
		double max = Math.ceil(value);
		if (max == value) {
			max = value + 1;
		}
		return new Boundaries(min, value, max);
	}

	/**
	 *
	 * @return The nodes reachable via the incomingConnection relationship
	 */
	private Set<CEGNode> getReachableNodes(CEGNode node) {
		LinkedList<CEGNode> workList = new LinkedList<CEGNode>();
		Set<CEGNode> reached = new HashSet<>();
		workList.add(node);
		while (!workList.isEmpty()) {
			CEGNode currentNode = workList.pop();
			if (!reached.contains(currentNode)) {
				reached.add(currentNode);
				currentNode.getIncomingConnections().stream().forEach(conn -> workList.add((CEGNode) conn.getSource()));
			} else {
				logger.warn("Found a cicle in the CEG when generating test cases.");
			}
		}
		return reached;
	}

	@Override
	protected void generateParameters() {
		for (IModelNode node : nodes) {
			String name = getVariable(node);
			if (name.startsWith(HIDDEN)) {
				continue;
			}
			ParameterType type = determineParameterTypeForNode(node);
			if (type != null && !parameterExists(specification, name, type)) {
				TestParameter parameter = createTestParameter(name, type);
				specification.getContents().add(parameter);
			}
		}
	}

	/** Generates test cases for the nodes of a CEG. */
	@Override
	protected void generateTestCases() throws SpecmateException {
		Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> evaluations = computeEvaluations();
		SortedSet<CEGNodeEvaluation> consistent = evaluations.getLeft();
		SortedSet<CEGNodeEvaluation> inconsistent = evaluations.getRight();
		int position = 0;
		for (CEGNodeEvaluation evaluation : consistent) {
			TestCase testCase = createTestCase(evaluation, specification, true);
			testCase.setPosition(position++);
			specification.getContents().add(testCase);
		}
		List<TestCase> inconsistentTestCases = new ArrayList<TestCase>();
		for (CEGNodeEvaluation evaluation : inconsistent) {
			TestCase testCase = createTestCase(evaluation, specification, false);
			boolean newTc = !inconsistentTestCases.stream().anyMatch(tc -> {
				EqualityHelper helper = new IdNamePositionIgnoreEqualityHelper();
				return helper.equals(tc, testCase);
			});
			if (newTc) {
				inconsistentTestCases.add(testCase);
				testCase.setPosition(position++);
				specification.getContents().add(testCase);
			} else {
				SpecmateEcoreUtil.detach(testCase);
			}
		}
		removeTemporaryObject();
	}

	private void removeTemporaryObject() {
		for (EObject obj : tempObjects.values()) {
			SpecmateEcoreUtil.detach(obj);
		}
	}

	/**
	 * Determines if a node is an input, output or intermediate node.
	 *
	 * @param node
	 * @return ParameterType.INPUT, if the nodes is an input node,
	 *         ParameterType.OUTPUT, if the node is an output node,
	 *         <code>null</code> if the node is an intermediate node.
	 */
	private ParameterType determineParameterTypeForNode(IModelNode node) {
		if (getIncomingConnections(node, considerLinks).isEmpty()) {
			return ParameterType.INPUT;
		} else if (node.getOutgoingConnections().isEmpty()) {
			return ParameterType.OUTPUT;
		} else {
			return null;
		}
	}

	/** Checks if a parameter already exists in a specification. */
	private boolean parameterExists(TestSpecification specification, String name, ParameterType type) {
		List<TestParameter> parameters = SpecmateEcoreUtil.pickInstancesOf(specification.getContents(),
				TestParameter.class);
		for (TestParameter parameter : parameters) {
			if (parameter.getName().equals(name) && parameter.getType().equals(type)) {
				return true;
			}
		}
		return false;
	}

	/** Creates a test case for a single node evaluation. */
	private TestCase createTestCase(CEGNodeEvaluation evaluation, TestSpecification specification,
			boolean isConsistent) {
		TestCase testCase = super.createTestCase(specification);
		testCase.setConsistent(isConsistent);
		List<TestParameter> parameters = SpecmateEcoreUtil.pickInstancesOf(specification.getContents(),
				TestParameter.class);
		Multimap<String, IContainer> variableToNodeMap = ArrayListMultimap.create();
		evaluation.keySet().stream().forEach(n -> variableToNodeMap.put(getVariable(n), n));
		for (TestParameter parameter : parameters) {
			List<String> constraints = new ArrayList<>();
			Collection<CEGNode> relevantNodes = getRelevantNodes(evaluation, parameter.getName());
			for (IContainer node : relevantNodes) {
				TaggedBoolean nodeEval = evaluation.get(node);
				String condition = getCondition((CEGNode) node);
				if (nodeEval != null) {
					String parameterValue = buildParameterValue(condition, nodeEval);
					constraints.add(parameterValue);
				}
			}
			String parameterValue = StringUtils.join(constraints, ",");
			ParameterAssignment assignment = TestspecificationFactory.eINSTANCE.createParameterAssignment();
			assignment.setId(SpecmateEcoreUtil.getIdForChild());
			assignment.setName(SpecmateEcoreUtil.getNameForChild(testCase, assignment.eClass()));
			assignment.setParameter(parameter);
			assignment.setCondition(parameterValue);
			assignment.setValue("");
			testCase.getContents().add(assignment);
		}
		return testCase;
	}

	private Collection<CEGNode> getRelevantNodes(CEGNodeEvaluation evaluation, String name) {
		Multimap<String, CEGNode> variableToNodeMap = ArrayListMultimap.create();
		evaluation.keySet().stream().filter(n -> isCause(n) || isEffect(n))
				.forEach(n -> variableToNodeMap.put(getVariable(n), n));
		Collection<CEGNode> allnodes = variableToNodeMap.get(name);

		boolean allMutex = allnodes.stream().allMatch(c -> {
			String condition = getCondition(c).trim();
			return condition.startsWith("=") || condition.startsWith(getTranslatedNegationWord() + " =");
		});

		List<CEGNode> positives = allnodes.stream().filter(c -> {
			return evaluation.get(c).value;
		}).collect(Collectors.toList());

		if (allMutex && positives.size() == 1) {
			return positives;
		}
		return allnodes;
	}

	private boolean isCause(CEGNode node) {
		return !(node instanceof CEGLinkedNode) && node.getIncomingConnections().isEmpty();
	}

	private boolean isEffect(CEGNode node) {
		return node.getOutgoingConnections().isEmpty();
	}

	/**
	 * Creates the string representation of an operator and a value. Negates the
	 * operator if necessary.
	 */
	private String buildParameterValue(String condition, TaggedBoolean nodeEval) {
		if (!nodeEval.value) {
			condition = negateCondition(condition);
		}
		if (nodeEval.tag != ETag.ALL) {
			condition = "[ " + condition + " ]";
		}
		return condition;
	}

	/** Negates a condition. */
	private String negateCondition(String condition) {
		return getTranslatedNegationWord() + " " + condition;
	}

	private String getTranslatedNegationWord() {
		return germanLanguage ? "nicht" : "not";
	}

	/**
	 * Node evaluations are a precursor to test cases. This method computes the node
	 * evaluations according to the rules in the Specmate systems requirements
	 * documentation.
	 *
	 * @param nodes
	 * @return
	 * @throws SpecmateException
	 */
	private Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> computeEvaluations()
			throws SpecmateException {
		SortedSet<CEGNodeEvaluation> consistentEvaluations = getInitialEvaluations();
		SortedSet<CEGNodeEvaluation> inconsistentEvaluations = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		SortedSet<CEGNodeEvaluation> intermediateEvaluations = getIntermediateEvaluations(consistentEvaluations);
		while (!intermediateEvaluations.isEmpty()) {
			for (CEGNodeEvaluation evaluation : intermediateEvaluations) {
				consistentEvaluations.remove(evaluation);
				Optional<IModelNode> intermediateNodeOpt = getAnyIntermediateNode(evaluation);
				AssertUtil.assertTrue(intermediateNodeOpt.isPresent());
				IModelNode node = intermediateNodeOpt.get();
				Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> iterationResult = iterateEvaluation(
						evaluation, node);
				consistentEvaluations.addAll(iterationResult.getLeft());
				inconsistentEvaluations.addAll(iterationResult.getRight());
			}
			intermediateEvaluations = getIntermediateEvaluations(consistentEvaluations);
		}

		Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> refinedEvaluations = refineEvaluations(
				consistentEvaluations);
		refinedEvaluations.getRight().addAll(inconsistentEvaluations);
		return refinedEvaluations;
	}

	private Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> refineEvaluations(
			SortedSet<CEGNodeEvaluation> evaluationList) throws SpecmateException {
		SortedSet<CEGNodeEvaluation> consistent = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		SortedSet<CEGNodeEvaluation> inconsistent = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);

		for (CEGNodeEvaluation e : evaluationList) {

			CEGNodeEvaluation relaxed = (CEGNodeEvaluation) e.clone();
			if (relaxConstraints(relaxed)) {
				consistent.add(relaxed);
			} else {
				inconsistent.add(relaxed);
			}

		}

		Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> mergedEvals = mergeCompatibleEvaluations(
				consistent);
		consistent = mergedEvals.getLeft();
		inconsistent.addAll(mergedEvals.getRight());
		SortedSet<CEGNodeEvaluation> filled = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		for (CEGNodeEvaluation eval : consistent) {
			filled.add(fill(eval));
		}
		return Pair.of(filled, inconsistent);
	}

	/**
	 * Returns the inital evaluations for the CEG model, where all output nodes are
	 * set one time true and one time false.
	 */
	private SortedSet<CEGNodeEvaluation> getInitialEvaluations() {
		initComparator();
		SortedSet<CEGNodeEvaluation> evaluations = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		nodes.stream().filter(node -> (determineParameterTypeForNode(node) == ParameterType.OUTPUT)).forEach(node -> {
			CEGNodeEvaluation positiveEvaluation = new CEGNodeEvaluation();
			CEGNode cegNode = (CEGNode) node;
			positiveEvaluation.put(cegNode, new TaggedBoolean(true, TaggedBoolean.ETag.ALL));
			evaluations.add(positiveEvaluation);
			CEGNodeEvaluation negativeEvaluation = new CEGNodeEvaluation();
			negativeEvaluation.put(cegNode, new TaggedBoolean(false, TaggedBoolean.ETag.ALL));
			evaluations.add(negativeEvaluation);
		});
		return evaluations;
	}

	/**
	 * Initializes a comparator which defines the ordering of the SortedSets
	 * containing the different evaluations
	 */
	private void initComparator() {
		nodeEvalSetComparator = new CEGNodeEvaluationComparator();
	}

	/** Retrieves a node that has predecessors with out a set value */
	private Optional<IModelNode> getAnyIntermediateNode(CEGNodeEvaluation evaluation) {
		for (Entry<CEGNode, TaggedBoolean> entry : evaluation.entrySet()) {
			if (entry.getValue().tag == ETag.ANY) {
				continue;
			}
			IModelNode node = entry.getKey();
			if (determineParameterTypeForNode(node) != ParameterType.INPUT) {
				boolean handled = getIncomingConnections(node, considerLinks).stream().map(conn -> conn.getSource())
						.allMatch(n -> evaluation.containsKey(n));
				if (!handled) {
					return Optional.of(node);
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Returns evaluations that have intermediate nodes (i.e. nodes that have to be
	 * evaluated)
	 */
	private SortedSet<CEGNodeEvaluation> getIntermediateEvaluations(SortedSet<CEGNodeEvaluation> evaluations) {
		SortedSet<CEGNodeEvaluation> intermediate = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		for (CEGNodeEvaluation evaluation : evaluations) {
			if (getAnyIntermediateNode(evaluation).isPresent()) {
				intermediate.add(evaluation);
			}
		}
		return intermediate;
	}

	/**
	 * Takes evaluation and a node and computes the evaluations of the nodes
	 * predecessors
	 */
	private Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> iterateEvaluation(
			CEGNodeEvaluation evaluation, IModelNode node) throws SpecmateException {
		SortedSet<CEGNodeEvaluation> consistent = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		SortedSet<CEGNodeEvaluation> inconsistent = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		AssertUtil.assertEquals(evaluation.get(node).tag, ETag.ALL);
		switch (getType(node)) {
		case AND:
			handleAllCase(true, evaluation, node, consistent, inconsistent);
			break;
		case OR:
			handleAllCase(false, evaluation, node, consistent, inconsistent);
			break;
		}
		return Pair.of(consistent, inconsistent);
	}

	private void handleAllCase(boolean isAnd, CEGNodeEvaluation evaluation, IModelNode node,
			SortedSet<CEGNodeEvaluation> consistent, SortedSet<CEGNodeEvaluation> inconsistent)
			throws SpecmateException {
		boolean nodeValue = evaluation.get(node).value;
		boolean failure;
		// case where node is true in AND case or node is false in OR case
		if ((isAnd && nodeValue) || (!isAnd && !nodeValue)) {
			for (IModelConnection selectedConn : getIncomingConnections(node, considerLinks)) {
				CEGNodeEvaluation newEvaluation = (CEGNodeEvaluation) evaluation.clone();
				failure = false;
				for (IModelConnection conn : getIncomingConnections(node, considerLinks)) {
					boolean value = isAnd ^ ((CEGConnection) conn).isNegate();
					ETag tag = conn == selectedConn ? ETag.ALL : ETag.ANY;
					failure = failure
							|| !checkAndSet(newEvaluation, (CEGNode) conn.getSource(), new TaggedBoolean(value, tag));
				}
				if (!failure) {
					consistent.add(newEvaluation);
				} else {
					inconsistent.add(newEvaluation);
				}
			}
			// case where node is false in AND case or node is true in OR case
		} else {
			for (IModelConnection selectedConn : getIncomingConnections(node, considerLinks)) {
				CEGNodeEvaluation newEvaluation = (CEGNodeEvaluation) evaluation.clone();
				failure = false;
				for (IModelConnection conn : getIncomingConnections(node, considerLinks)) {
					boolean value = ((conn == selectedConn) ^ (isAnd ^ ((CEGConnection) conn).isNegate()));
					ETag tag = conn == selectedConn ? ETag.ALL : ETag.ANY;
					failure = failure
							|| !checkAndSet(newEvaluation, (CEGNode) conn.getSource(), new TaggedBoolean(value, tag));
				}
				if (!failure) {
					consistent.add(newEvaluation);
				} else {
					inconsistent.add(newEvaluation);
				}
			}
		}
	}

	/**
	 * Sets the value of a node in an evaluation but checks first if it is already
	 * set with a different value
	 *
	 * @return false if an inconsistent value would be set in the node
	 */
	private boolean checkAndSet(CEGNodeEvaluation evaluation, CEGNode node, TaggedBoolean effectiveValue)
			throws SpecmateException {
		if (evaluation.containsKey(node) && evaluation.get(node).value != effectiveValue.value) {
			return false;
		} else {
			evaluation.put(node, effectiveValue);
			return true;
		}
	}

	/**
	 * Runs through the list of evaluations and merges the ones that can be merged.
	 * Identify inconsistent evaluations
	 *
	 * @throws SpecmateException
	 */
	private Pair<SortedSet<CEGNodeEvaluation>, SortedSet<CEGNodeEvaluation>> mergeCompatibleEvaluations(
			SortedSet<CEGNodeEvaluation> evaluations) throws SpecmateException {
		SortedSet<CEGNodeEvaluation> result = new TreeSet<CEGNodeEvaluation>(nodeEvalSetComparator);
		while (evaluations.size() > 0) {
			SortedSet<CEGNodeEvaluation> candidates = getMergeCandiate(evaluations);

			if (candidates.isEmpty()) {
				// There is no merge candidate:
				// The model has contradictory constraints e.g. (A ==> X) & (A
				// ==> !X)
				// Remaining evaluations are inconsistent
				return Pair.of(result, evaluations);
			}

			evaluations.removeAll(candidates);
			CEGNodeEvaluation merged = mergeAllEvaluations(candidates);
			result.add(merged);
		}
		return Pair.of(result, new TreeSet<>(nodeEvalSetComparator));
	}

	/**
	 * Checks if a node evaluation is satisfiable in the CEG and relaxes the
	 * constraints if not. For exaple, it allows multiple true values as
	 * predecessors of an OR node.
	 *
	 * @param eval
	 * @throws SpecmateInternalException
	 */
	private boolean relaxConstraints(CEGNodeEvaluation eval) throws SpecmateInternalException {
		// Inititalize solver infrastructure
		IPBSolver solver = org.sat4j.pb.SolverFactory.newResolution();
		GateTranslator translator = new GateTranslator(solver);
		WeightedMaxSatDecorator maxSat = new WeightedMaxSatDecorator(solver);
		maxSat.newVar(eval.size());

		try {
			pushCEGStructure(translator);
			for (Entry<CEGNode, TaggedBoolean> entry : eval.entrySet()) {
				TaggedBoolean value = entry.getValue();
				int nodeVar = (value.value ? 1 : -1) * getVarForNode(entry.getKey());
				if (value.tag == ETag.ALL) {
					// hard constraint
					maxSat.addHardClause(getVectorForVariables(nodeVar));
				} else if (value.tag == ETag.ANY) {
					// soft constraint, may be relaxed
					maxSat.addSoftClause(getVectorForVariables(nodeVar));
				}
			}
			int[] foundModel = maxSat.findModel();
			if (foundModel != null) {
				for (int i = 0; i < foundModel.length; i++) {
					CEGNode node = getNodeForVar(Math.abs(foundModel[i]));
					if (node != null) {
						// variable in the model related to a node
						TaggedBoolean tag = eval.get(node);
						if (tag != null) {
							// the node is part of the evaluation
							tag.value = foundModel[i] > 0;
						}
					}
				}
				return true;
			}
		} catch (ContradictionException | TimeoutException c) {
			return false;
		}
		return false;
	}

	private SortedSet<CEGNodeEvaluation> getMergeCandiate(SortedSet<CEGNodeEvaluation> evaluations)
			throws SpecmateException {
		// Map to track between logical variables and evaluations
		TreeMap<Integer, CEGNodeEvaluation> var2EvalMap = new TreeMap<Integer, CEGNodeEvaluation>(
				new Comparator<Integer>() {
					@Override
					public int compare(Integer i1, Integer i2) {
						return Integer.compare(i1, i2);
					}
				});

		// Inititalize solver infrastructure
		IPBSolver solver = org.sat4j.pb.SolverFactory.newResolution();
		GateTranslator translator = new GateTranslator(solver);
		WeightedMaxSatDecorator maxSat = new WeightedMaxSatDecorator(solver);

		int maxVar = getAdditionalVar(evaluations.size());
		maxSat.newVar(maxVar);

		try {
			pushCEGStructure(translator);
			var2EvalMap = pushEvaluations(evaluations, translator, maxSat);
		} catch (ContradictionException c) {
			throw new SpecmateInternalException(ErrorCode.TESTGENERATION, c);
		}
		try {
			int[] model = maxSat.findModel();
			if (model == null) {
				throw new SpecmateInternalException(ErrorCode.TESTGENERATION, "The provided model is inconsistent.");
			}
			return extractEnabledEvaluations(var2EvalMap, model);
		} catch (TimeoutException e) {
			throw new SpecmateInternalException(ErrorCode.TESTGENERATION, e);
		}
	}

	private SortedSet<CEGNodeEvaluation> extractEnabledEvaluations(TreeMap<Integer, CEGNodeEvaluation> var2EvalMap,
			int[] model) {
		SortedSet<CEGNodeEvaluation> toMerge = new TreeSet<>(nodeEvalSetComparator);
		for (int i = 0; i < model.length; i++) {
			int var = model[i];
			if (var <= 0) {
				continue;
			}
			CEGNodeEvaluation eval = var2EvalMap.get(var);
			if (eval != null) {
				toMerge.add(eval);
			}
		}
		return toMerge;
	}

	private TreeMap<Integer, CEGNodeEvaluation> pushEvaluations(SortedSet<CEGNodeEvaluation> evaluations,
			GateTranslator translator, WeightedMaxSatDecorator maxSat) throws ContradictionException {
		TreeMap<Integer, CEGNodeEvaluation> var2EvalMap = new TreeMap<Integer, CEGNodeEvaluation>(
				new Comparator<Integer>() {
					@Override
					public int compare(Integer i1, Integer i2) {
						return Integer.compare(i1, i2);
					}
				});

		int nextVar = 1;

		IVecInt vector;

		for (CEGNodeEvaluation evaluation : evaluations) {
			int varForEval = getAdditionalVar(nextVar);
			var2EvalMap.put(varForEval, evaluation);
			nextVar++;
			for (IModelNode node : nodes) {
				int varForNode = getVarForNode(node);
				// maxSat.newVar(varForNode);
				TaggedBoolean value = evaluation.get(node);
				if (value != null) {
					if (value.value) {
						vector = getVectorForVariables(-varForEval, varForNode);
						translator.addClause(vector);
					} else {
						vector = getVectorForVariables(-varForEval, -varForNode);
						translator.addClause(vector);
					}
				}
			}
			vector = getVectorForVariables(varForEval);
			maxSat.addSoftClause(1, vector);
		}
		return var2EvalMap;
	}

	private int getAdditionalVar(int i) {
		return nodes.size() + i;
	}

	private IVecInt getVectorForVariables(Integer... vars) {
		IVecInt vector = new VecInt(vars.length + 1);
		for (int i = 0; i < vars.length; i++) {
			vector.push(vars[i]);
		}
		return vector;
	}

	private CEGNodeEvaluation mergeAllEvaluations(Set<CEGNodeEvaluation> clique) {
		CEGNodeEvaluation evaluation = new CEGNodeEvaluation();
		for (CEGNodeEvaluation toMerge : clique) {
			for (Entry<CEGNode, TaggedBoolean> entry : toMerge.entrySet()) {
				if (evaluation.containsKey(entry.getKey())) {
					// Do not overwrite values tagged with ALL
					if (evaluation.get(entry.getKey()).tag == ETag.ALL) {
						continue;
					}
				}
				evaluation.put(entry.getKey(), entry.getValue());
			}
		}
		return evaluation;
	}

	/** Fills out all unset nodes in the given node evaluation */
	private CEGNodeEvaluation fill(CEGNodeEvaluation evaluation) throws SpecmateException {
		ISolver solver = initSolver(evaluation);
		try {
			CEGNodeEvaluation filled = new CEGNodeEvaluation();
			int[] model = solver.findModel();
			if (model == null) {
				throw new SpecmateInternalException(ErrorCode.TESTGENERATION,
						"Could not determine consistent test values.");
			}
			for (int v : model) {
				setModelValue(evaluation, filled, v);
			}
			return filled;
		} catch (TimeoutException e) {
			throw new SpecmateInternalException(ErrorCode.TESTGENERATION, e);
		}
	}

	/**
	 * Sets the value in an evaluation based on an original evaluation and a model
	 * value.
	 */
	private void setModelValue(CEGNodeEvaluation originalEvaluation, CEGNodeEvaluation targetEvaluation,
			int varNameValue) {
		boolean value = varNameValue > 0;
		int varName = (value ? 1 : -1) * varNameValue;
		CEGNode node = getNodeForVar(varName);
		TaggedBoolean originalValue = originalEvaluation.get(node);
		if (originalValue != null) {
			targetEvaluation.put(node, originalValue);
		} else {
			targetEvaluation.put(node, new TaggedBoolean(value, ETag.AUTO));
		}
	}

	/** Initializes the SAT4J solver. */
	private GateTranslator initSolver(CEGNodeEvaluation evaluation) throws SpecmateException {
		GateTranslator translator = new GateTranslator(SolverFactory.newLight());
		try {
			pushCEGStructure(translator);
			pushEvaluation(evaluation, translator);
		} catch (ContradictionException e) {
			throw new SpecmateInternalException(ErrorCode.TESTGENERATION, e);
		}
		return translator;
	}

	private void pushEvaluation(CEGNodeEvaluation evaluation, GateTranslator translator) throws ContradictionException {
		for (IModelNode node : nodes) {
			int varForNode = getVarForNode(node);
			TaggedBoolean value = evaluation.get(node);
			if (value != null) {
				if (value.value) {
					translator.gateTrue(varForNode);
				} else {
					translator.gateFalse(varForNode);
				}
			}
		}
	}

	/** Feeds constraints representing the CEG structure to the solver */
	private void pushCEGStructure(GateTranslator translator) throws ContradictionException {
		for (IModelNode node : nodes) {
			int varForNode = getVarForNode(node);
			IVecInt vector = getPredecessorVector(node);
			if (vector.size() > 0) {
				if (getType(node) == NodeType.AND) {
					translator.and(varForNode, vector);
				} else {
					translator.or(varForNode, vector);
				}
			}
		}
		pushMutualExclusiveConstraints(translator);
	}

	private void pushMutualExclusiveConstraints(GateTranslator translator) throws ContradictionException {
		Collection<Collection<CEGNode>> mutualExclusiveNodeSets = getMutualExclusiveNodeSets();
		for (Collection<CEGNode> mutexSet : mutualExclusiveNodeSets) {
			Integer[] variables = mutexSet.stream().map(node -> getVarForNode(node)).collect(Collectors.toList())
					.toArray(new Integer[0]);
			IVecInt vector = getVectorForVariables(variables);
			translator.addExactly(vector, 1);
		}
	}

	private Collection<Collection<CEGNode>> getMutualExclusiveNodeSets() {
		Collection<Collection<CEGNode>> result = new ArrayList<>();
		Map<String, Set<CEGNode>> multiMap = new HashMap<String, Set<CEGNode>>();
		for (IModelNode node : nodes) {
			CEGNode cegNode = (CEGNode) node;
			if (getCondition(node).trim().startsWith("=")) {
				String variable = getVariable(cegNode);
				if (!multiMap.containsKey(variable)) {
					multiMap.put(variable, new HashSet<CEGNode>());
				}
				multiMap.get(variable).add(cegNode);
			}
		}
		for (String key : multiMap.keySet()) {
			result.add(multiMap.get(key));
		}
		return result;
	}

	/** Returns the CEG node for a given variable (given as int) */
	private CEGNode getNodeForVar(int i) {
		return (CEGNode) nodes.get(i - 1);
	}

	/** Returns a variable (usable for SAT4J) for a given CEG node. */
	private int getVarForNode(IModelNode node) {
		return nodes.indexOf(node) + 1;
	}

	/** Returns a variable/value vector for all predeccessors of a node */
	private IVecInt getPredecessorVector(IModelNode node) {
		IVecInt vector = new VecInt();
		for (IModelConnection conn : getIncomingConnections(node, considerLinks)) {
			IModelNode pre = conn.getSource();
			int var = getVarForNode(pre);
			if (((CEGConnection) conn).isNegate()) {
				var *= -1;
			}
			vector.push(var);
		}
		return vector;
	}

	/**
	 * Equality checker that ignores differences in the fields id, name and position
	 */
	@SuppressWarnings("serial")
	private class IdNamePositionIgnoreEqualityHelper extends EqualityHelper {
		@Override
		protected boolean haveEqualFeature(EObject eObject1, EObject eObject2, EStructuralFeature feature) {
			if (feature == BasePackage.Literals.IID__ID) {
				return true;
			}
			if (feature == BasePackage.Literals.INAMED__NAME) {
				return true;
			}
			if (feature == BasePackage.Literals.IPOSITIONABLE__POSITION) {
				return true;
			}
			return super.haveEqualFeature(eObject1, eObject2, feature);
		}
	}

	private class Boundaries {
		public double min, eq, max;

		public Boundaries(double min, double eq, double max) {
			super();
			this.min = min;
			this.eq = eq;
			this.max = max;
		}
	}
}
