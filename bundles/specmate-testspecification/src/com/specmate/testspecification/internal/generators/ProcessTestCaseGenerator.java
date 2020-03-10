package com.specmate.testspecification.internal.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.specmate.common.AssertUtil;
import com.specmate.common.exception.SpecmateException;
import com.specmate.model.base.IContainer;
import com.specmate.model.base.IModelConnection;
import com.specmate.model.base.IModelNode;
import com.specmate.model.processes.Process;
import com.specmate.model.processes.ProcessConnection;
import com.specmate.model.processes.ProcessDecision;
import com.specmate.model.processes.ProcessEnd;
import com.specmate.model.processes.ProcessStart;
import com.specmate.model.processes.ProcessStep;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.ParameterAssignment;
import com.specmate.model.testspecification.ParameterType;
import com.specmate.model.testspecification.TestCase;
import com.specmate.model.testspecification.TestParameter;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestStep;
import com.specmate.model.testspecification.TestspecificationFactory;

/** Generates tests from a process model */
public class ProcessTestCaseGenerator extends TestCaseGeneratorBase<Process, IModelNode> {

	/** All connections of the process */
	private List<ProcessConnection> connections;

	/**
	 * Map from parameter names to parameters of the resulting test case
	 * specification
	 */
	private Map<String, TestParameter> testParameters = new HashMap<>();

	/** constructor */
	public ProcessTestCaseGenerator(TestSpecification specification) {
		super(specification, Process.class, IModelNode.class);
		connections = SpecmateEcoreUtil.pickInstancesOf(model.getContents(), ProcessConnection.class);
	}

	/** {@inheritDoc} */
	@Override
	protected void generateParameters() {
		List<ProcessDecision> decisions = SpecmateEcoreUtil.pickInstancesOf(model.getContents(), ProcessDecision.class);
		for (ProcessDecision decision : decisions) {
			String testParameterName = decision.getName();
			createAndAddTestParameterIfNecessary(testParameterName, ParameterType.INPUT);
		}

		List<ProcessConnection> connections = SpecmateEcoreUtil.pickInstancesOf(model.getContents(),
				ProcessConnection.class);
		for (ProcessConnection connection : connections) {
			if (connection.getSource() instanceof ProcessDecision || !hasCondition(connection)) {
				// in this case the test parameter is defined by the decision node
				continue;
			}
			String testParameterName = extractVariableAndConditionFromExpression(connection.getCondition()).name;
			createAndAddTestParameterIfNecessary(testParameterName, ParameterType.INPUT);
		}

		List<ProcessStep> steps = SpecmateEcoreUtil.pickInstancesOf(model.getContents(), ProcessStep.class);
		for (ProcessStep step : steps) {
			if (hasExpectedOutcome(step)) {
				String expectedOutcome = step.getExpectedOutcome();
				String variable = extractVariableAndConditionFromExpression(expectedOutcome).name;
				createAndAddTestParameterIfNecessary(variable, ParameterType.OUTPUT);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void generateTestCases() throws SpecmateException {
		Set<IModelNode> startNodes = getStartNodes();
		Set<IModelNode> endNodes = getEndNodes();
		
		List<GraphPath<IModelNode, ProcessConnection>> paths = getAllPathsExact(startNodes, endNodes);

		Set<ProcessConnection> coveredConnections = paths.stream()
				.flatMap(path -> path.getEdgeList().stream())
				.filter(connection -> hasCondition(connection))
				.collect(Collectors.toSet());
		
		Set<ProcessConnection> uncoveredConditionConnections = connections.stream()
				.filter(connection -> !coveredConnections.contains(connection))
				.filter(connection -> hasCondition(connection))
				.collect(Collectors.toSet());
		
		if(coveredConnections.isEmpty()) {
			List<GraphPath<IModelNode, ProcessConnection>> heuristicPaths = getPathsHeuristically(startNodes, endNodes, new HashSet<>(connections));
			paths.addAll(heuristicPaths);
		}
		
		if(!uncoveredConditionConnections.isEmpty()) {
			List<GraphPath<IModelNode, ProcessConnection>> heuristicPaths = getPathsHeuristically(startNodes, endNodes, uncoveredConditionConnections);
			paths.addAll(heuristicPaths);
		}
		
		List<GraphPath<IModelNode, ProcessConnection>> filteredPaths = filterDuplicatePaths(paths);
		createTestCases(filteredPaths);
	}

	/**
	 * Computes all paths 
	 * @return
	 */
	private List<GraphPath<IModelNode, ProcessConnection>> getAllPathsExact(Set<IModelNode> startNodes, Set<IModelNode> endNodes) {
		List<GraphPath<IModelNode, ProcessConnection>> paths = getConditions().stream()
				.flatMap(connection -> getPathsForCondition(connection, startNodes, endNodes).stream())
				.collect(Collectors.toList());
		return paths;
	}

	/**
	 * Looks up a if a test parameter with the given name has already been created.
	 * If yes, returns the parameter. If not, creates a new one and saves it.
	 */
	private TestParameter createAndAddTestParameterIfNecessary(String testParameterName, ParameterType parameterType) {
		TestParameter testParameter = testParameters.get(testParameterName);
		if (testParameter == null) {
			testParameter = createTestParameter(testParameterName, parameterType);
			testParameters.put(testParameterName, testParameter);
			specification.getContents().add(testParameter);
		}
		return testParameter;
	}
	
	/**
	 * @param condition the condition to search for
	 * @return all conditions that should not be taken if the actual condition is selected
	 */
	private Set<String> getConflictingConditions(String condition) {
		Set<IModelNode> decisionNodes = nodes.stream().filter(node -> hasCondition(node, condition)).collect(Collectors.toSet());
		Set<String> conflictingConditions = decisionNodes.stream()
				.flatMap(node -> node.getOutgoingConnections().stream())
				.filter(connection -> !hasCondition(connection, condition))
				.map(connection -> (ProcessConnection) connection)
				.map(connection -> connection.getCondition())
				.collect(Collectors.toSet());
		return conflictingConditions;
	}
	
	private static boolean hasCondition(IModelConnection connection) {
		if(connection instanceof ProcessConnection) {
			return !StringUtils.isEmpty(((ProcessConnection) connection).getCondition());
		}
		return false;
	}
	
	private static boolean hasCondition(IModelNode node, String condition) {
		if(node instanceof ProcessDecision) {
			return ((ProcessDecision) node).getOutgoingConnections().stream()
					.filter(connection -> hasCondition(connection, condition))
					.findAny()
					.isPresent();
		}
		return false;
	}
	
	private static boolean hasCondition(IModelConnection connection, String condition) {
		if(connection instanceof ProcessConnection) {
			return ((ProcessConnection) connection).getCondition().equalsIgnoreCase(condition);
		}
		return false;
	}
	
	/**
	 * Gets the current graph without edges containing conflicting conditions to the given condition.
	 * 
	 * @param condition the condition
	 * @return the graph without conflicting conditions to the given condition
	 */
	private DirectedGraph<IModelNode, ProcessConnection> getConflictFreeGraph(String condition) {
		DirectedGraph<IModelNode, ProcessConnection> graph = getGraph();
		
		Set<String> conflictingConditions = getConflictingConditions(condition);
		Set<ProcessConnection> conflictingConnections = graph.edgeSet().stream()
				.filter(connection -> conflictingConditions.contains(connection.getCondition()))
				.collect(Collectors.toSet());
		graph.removeAllEdges(conflictingConnections);
		return graph;
	}
	
	/**
	 * @return the conditions in the connections of the given path
	 */
	private static Set<String> getConditions(GraphPath<IModelNode, ProcessConnection> path) {
		return path.getEdgeList().stream()
				.filter(connection -> connection.getCondition() != null && !connection.getCondition().isBlank())
				.map(connection -> connection.getCondition())
				.collect(Collectors.toSet());
	}
	
	/**
	 * Computes all conditions in all connections.
	 * 
	 * @return the set of all conditions.
	 */
	private Set<String> getConditions() {
		return connections.stream()
				.filter(connection -> connection.getCondition() != null && !connection.getCondition().isBlank())
				.map(connection -> connection.getCondition())
				.collect(Collectors.toSet());
	}
	
	/**
	 * Checks the given path for conflicting conditions
	 * 
	 * @param path the path to check
	 * @return the set of conflicting conditions (with all condition pairs that are in conflict).
	 */
	private Set<String> getConflicts(GraphPath<IModelNode, ProcessConnection> path) {
		Set<String> conditions = getConditions(path);
		Set<String> conflictingConditions = conditions.stream()
				.flatMap(connection -> getConflictingConditions(connection).stream())
				.collect(Collectors.toSet());
		conflictingConditions.retainAll(conditions);
		return conflictingConditions;
	}
	
	/**
	 * Computes the edges in the graph carrying the given condition.
	 * 
	 * @param condition
	 * @return The set of edges carrying the given condition.
	 */
	private Set<ProcessConnection> getConditionEdges(String condition) {
		return connections.stream()
				.filter(connection -> connection.getCondition() != null && connection.getCondition().equalsIgnoreCase(condition))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Computes the shortest conflict free paths (covering all edges with the given condition).
	 * 
	 * @param condition the condition
	 * @return the list of graph paths
	 */
	private List<GraphPath<IModelNode, ProcessConnection>> getPathsForCondition(String condition, Set<IModelNode> startNodes, Set<IModelNode> endNodes) {
		DirectedGraph<IModelNode, ProcessConnection> graph = getConflictFreeGraph(condition);
		AllDirectedPaths<IModelNode, ProcessConnection> adp = new AllDirectedPaths<IModelNode, ProcessConnection>(graph);
		List<GraphPath<IModelNode, ProcessConnection>> paths = adp.getAllPaths(startNodes, endNodes, true, null);
		paths.sort((path1, path2) -> path1.getLength() - path2.getLength());
		
		Set<ProcessConnection> edgesToCover = this.getConditionEdges(condition);
		Set<ProcessConnection> coveredEdges = new HashSet<>();
		List<GraphPath<IModelNode, ProcessConnection>> selectedPaths = new ArrayList<>();
		
		List<GraphPath<IModelNode, ProcessConnection>> conflictFreePaths = paths.stream()
				.filter(path -> getConflicts(path).isEmpty())
				.collect(Collectors.toList());
		
		for(GraphPath<IModelNode, ProcessConnection> path: conflictFreePaths) {
			if(!coveredEdges.containsAll(path.getEdgeList())) {
				selectedPaths.add(path);
				coveredEdges.addAll(path.getEdgeList());
				edgesToCover.removeAll(path.getEdgeList());
			}
			if(edgesToCover.isEmpty()) {
				break;
			}
		}
		
		return selectedPaths;
	}
	

	/**
	 * Generates paths to the process graph by weighting conflicting high
	 * 
	 * @return graph paths
	 */
	private List<GraphPath<IModelNode, ProcessConnection>> getPathsHeuristically(Set<IModelNode> startNodes, Set<IModelNode> endNodes, Set<ProcessConnection> mandatoryConnections) {
		DirectedGraph<IModelNode, ProcessConnection> graph = getGraph();
		
		WeightedGraph<IModelNode, DefaultWeightedEdge> weightedGraph = new SimpleDirectedWeightedGraph<IModelNode, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		Map<DefaultWeightedEdge, ProcessConnection> connectionMap = new HashMap<>();
		graph.vertexSet().stream().forEach(node -> weightedGraph.addVertex(node));
		graph.edgeSet().stream().forEach(connection -> {
			DefaultWeightedEdge edge = weightedGraph.addEdge(connection.getSource(), connection.getTarget());
			connectionMap.put(edge, connection);
		});

		Set<ProcessConnection> conditionEdges = weightedGraph.edgeSet().stream()
				.map(connection -> connectionMap.get(connection))
				.filter(connection -> connection.getCondition() != null && !connection.getCondition().isBlank())
				.collect(Collectors.toSet());
		Set<String> conditions = conditionEdges.stream().map(connection -> connection.getCondition()).collect(Collectors.toSet());
		
		if(conditions.isEmpty()) {
			conditions = Collections.singleton((String) null);
		}
		
		List<GraphPath<IModelNode, ProcessConnection>> allPaths = new ArrayList<>();
		
		conditions.stream().forEach(c -> {
			if(c != null) {
				weightedGraph.edgeSet().stream().forEach(connection -> {
					ProcessConnection processConnection = connectionMap.get(connection);
					int edges = graph.edgeSet().size();
					float weight = processConnection.getCondition() != null && processConnection.getCondition().equalsIgnoreCase(c) ? 1 : (edges * edges);
					weightedGraph.setEdgeWeight(connection, weight);
				});
			}
			
			Set<ProcessConnection> uncoveredConnections = mandatoryConnections;
			
			IModelNode startNode = startNodes.stream().findAny().get();
			while (uncoveredConnections.stream().findAny().isPresent()) {
				ProcessConnection currentUncoveredConnection = uncoveredConnections.stream().findAny().get();
				IModelNode sourceNode = currentUncoveredConnection.getSource();
				IModelNode targetNode = currentUncoveredConnection.getTarget();
				DijkstraShortestPath<IModelNode, DefaultWeightedEdge> dsp = new DijkstraShortestPath<>(weightedGraph);

				GraphPath<IModelNode, DefaultWeightedEdge> startPath = dsp.getPath(startNode, sourceNode);
				GraphPath<IModelNode, DefaultWeightedEdge> endPath = null;
				IModelNode bestEndNode = null;
				int minimalEndPathLength = Integer.MAX_VALUE;
				for (IModelNode endNode : endNodes) {
					GraphPath<IModelNode, DefaultWeightedEdge> currentEndPath = dsp.getPath(targetNode, endNode);
					if (currentEndPath != null) {
						int currentEndPathLength = currentEndPath.getLength();
						if (currentEndPathLength < minimalEndPathLength) {
							minimalEndPathLength = currentEndPathLength;
							endPath = currentEndPath;
							bestEndNode = endNode;
						}
					}
				}

				AssertUtil.assertNotNull(endPath, "Could not find path to end node!");

				List<ProcessConnection> connections = new ArrayList<>();
				List<ProcessConnection> startPathConnections = startPath.getEdgeList().stream()
						.map(connection -> connectionMap.get(connection))
						.collect(Collectors.toList());
				
				connections.addAll(startPathConnections);
				connections.add(currentUncoveredConnection);
				
				List<ProcessConnection> endPathConnections = endPath.getEdgeList().stream()
						.map(connection -> connectionMap.get(connection))
						.collect(Collectors.toList());
				
				connections.addAll(endPathConnections);
				GraphPath<IModelNode, ProcessConnection> constructedPath = new GraphWalk<>(graph, startNode, bestEndNode,
						connections, 0d);
				allPaths.add(constructedPath);
				uncoveredConnections.removeAll(connections);
			}
		});
		
		return allPaths;
	}

	/** Retrieves the start nodes of the model */
	private Set<IModelNode> getStartNodes() {
		Set<IModelNode> startNodes = SpecmateEcoreUtil.uniqueInstancesOf(nodes, ProcessStart.class).stream()
				.map(node -> (IModelNode) node)
				.collect(Collectors.toSet());
		
		AssertUtil.assertEquals(startNodes.size(), 1, "Number of start nodes in process is different to 1.");
		return startNodes;
	}

	/** Retrieves the end nodes of the model */
	private Set<IModelNode> getEndNodes() {
		Set<IModelNode> endNodes = SpecmateEcoreUtil.uniqueInstancesOf(nodes, ProcessEnd.class).stream()
				.map(node -> (IModelNode) node)
				.collect(Collectors.toSet());
		
		AssertUtil.assertTrue(endNodes.size() > 0, "No end nodes in process were found");
		return endNodes;
	}

	/** Creates a graph representing the model */
	private DirectedGraph<IModelNode, ProcessConnection> getGraph() {
		DirectedGraph<IModelNode, ProcessConnection> graph = new DirectedMultigraph<>(ProcessConnection.class);
		for (IModelNode node : nodes) {
			graph.addVertex(node);
		}

		for (ProcessConnection connection : connections) {
			IModelNode source = connection.getSource();
			IModelNode target = connection.getTarget();
			graph.addEdge(source, target, connection);
		}

		return graph;
	}

	/** Removes duplicate paths from the list */
	private List<GraphPath<IModelNode, ProcessConnection>> filterDuplicatePaths(
			List<GraphPath<IModelNode, ProcessConnection>> paths) {
		Set<GraphPath<IModelNode, ProcessConnection>> obsoletePaths = new HashSet<>();

		for (int i = 0; i < paths.size(); i++) {
			GraphPath<IModelNode, ProcessConnection> path1 = paths.get(i);
			Set<ProcessConnection> connectionSet1 = new HashSet<>(path1.getEdgeList());
			for (int j = 0; j < paths.size(); j++) {
				GraphPath<IModelNode, ProcessConnection> path2 = paths.get(j);

				if (i == j || obsoletePaths.contains(path2)) {
					continue;
				}

				Set<ProcessConnection> connectionSet2 = new HashSet<>(path2.getEdgeList());
				connectionSet1.removeAll(connectionSet2);

				if (connectionSet1.isEmpty()) {
					obsoletePaths.add(path1);
					break;
				}
			}
		}

		List<GraphPath<IModelNode, ProcessConnection>> filteredPaths = paths.stream()
				.filter((GraphPath<IModelNode, ProcessConnection> path) -> !obsoletePaths.contains(path))
				.collect(Collectors.toList());
		return filteredPaths;
	}

	/** Extracts variable and conditoin from a single string by splitting at "=" */
	private AssigmentValues extractVariableAndConditionFromExpression(String outcome) {
		// split only at first occurence of "=", result will have length <= 2
		String[] splitted = outcome.split("=", 2);
		String variable = splitted[0];
		String condition = splitted.length > 1 && !StringUtils.isEmpty(splitted[1]) ? splitted[1] : "is present";
		return new AssigmentValues(variable.trim(), condition.trim());
	}

	/** Creates testcases from a list of paths through the model */
	private void createTestCases(List<GraphPath<IModelNode, ProcessConnection>> paths) {
		for (GraphPath<IModelNode, ProcessConnection> path : paths) {
			List<AssigmentValues> variableConditionList = extractVariablesAndConditionsFromPath(path);
			TestCase tc = createTestCaseFromVariableConditionList(variableConditionList);
			TestProcedure proc = createTestProcedureForPath(tc, path);
			tc.getContents().add(proc);
			specification.getContents().add(tc);
		}
	}

	/** Extracts a list of variable/condition pairs reflecting a certain path */
	private List<AssigmentValues> extractVariablesAndConditionsFromPath(GraphPath<IModelNode, ProcessConnection> path) {
		List<AssigmentValues> variableConditionList = new ArrayList<>();
		iteratePath(path, new PathConsumer() {
			@Override
			public void consumeProcessStart(ProcessStart start, ProcessConnection outgoingConnection,
					int vertexNumber) {
				consumeGenericConnection(outgoingConnection);
			}

			@Override
			public void consumeProcessEnd(ProcessEnd end, int vertexNumber) {
				// nothing to do
			}

			@Override
			public void consumeProcessDecision(ProcessDecision decision, ProcessConnection outgoingConnection,
					int vertexNumber) {
				String name = StringUtils.isEmpty(decision.getName()) ? "Decision" : decision.getName();
				
				String condition = outgoingConnection.getCondition();
				if (StringUtils.isEmpty(condition)) {
					condition = "is present";
				}
				variableConditionList.add(new AssigmentValues(name, condition, ParameterType.INPUT));
			}

			@Override
			public void consumeProcessStep(ProcessStep step, ProcessConnection outgoingConnection, int vertexNumber) {
				if (hasExpectedOutcome(step)) {
					AssigmentValues varCond = extractVariableAndConditionFromExpression(step.getExpectedOutcome());
					varCond.type = ParameterType.OUTPUT;
					variableConditionList.add(varCond);
				}
				consumeGenericConnection(outgoingConnection);
			}

			private void consumeGenericConnection(ProcessConnection outgoingConnection) {
				if (outgoingConnection != null && hasCondition(outgoingConnection)) {
					AssigmentValues varCond = extractVariableAndConditionFromExpression(
							outgoingConnection.getCondition());
					varCond.type = ParameterType.INPUT;
					variableConditionList.add(varCond);
				}
			}
		});
		return variableConditionList;
	}

	/**
	 * Iterates through a graph path and for each node/connection pair calls the
	 * method of the consumer that matches the node type.
	 */
	private void iteratePath(GraphPath<IModelNode, ProcessConnection> path, PathConsumer consumer) {
		for (int i = 0; i < path.getVertexList().size(); i++) {
			IModelNode currentNode = path.getVertexList().get(i);
			ProcessConnection currentConnection = null;
			if (i < path.getEdgeList().size()) {
				currentConnection = path.getEdgeList().get(i);
			}
			if (currentNode instanceof ProcessStep) {
				consumer.consumeProcessStep((ProcessStep) currentNode, currentConnection, i);
			} else if (currentNode instanceof ProcessDecision) {
				consumer.consumeProcessDecision((ProcessDecision) currentNode, currentConnection, i);
			} else if (currentNode instanceof ProcessStart) {
				consumer.consumeProcessStart((ProcessStart) currentNode, currentConnection, i);
			} else if (currentNode instanceof ProcessEnd) {
				consumer.consumeProcessEnd((ProcessEnd) currentNode, i);
			}
		}
	}

	/**
	 * Creates a test case from a a list of variables and conditions reflecting a
	 * certain path through the model
	 */
	private TestCase createTestCaseFromVariableConditionList(List<AssigmentValues> variableConditionList) {
		List<String> seenParameterNames = new ArrayList<>();
		TestCase tc = createTestCase(specification);
		tc.setConsistent(true);
		for (AssigmentValues varCond : variableConditionList) {
			String variable = varCond.name;
			variable = getCountingParameterName(seenParameterNames, variable);
			TestParameter parameter = createAndAddTestParameterIfNecessary(variable, varCond.type);
			ParameterAssignment assignment = createParameterAssignment(tc, parameter, varCond.condition);
			tc.getContents().add(assignment);
		}
		return tc;
	}

	/**
	 * Returns [variable]-X where X is the smallest number such that [Variable]-X is
	 * not in the seenParameterNames list. If X is 0, the "-X" part is omitted.
	 */
	private String getCountingParameterName(List<String> seenParameterNames, String variable) {
		int i = 1;
		while (seenParameterNames.contains(variable)) {
			i++;
			variable = variable + " " + i;
		}
		seenParameterNames.add(variable);
		return variable;
	}

	/** Extracts a list of variable/condition pairs reflecting a certain path */
	private TestProcedure createTestProcedureForPath(TestCase tc, GraphPath<IModelNode, ProcessConnection> path) {
		List<String> seenParameterNames = new ArrayList<>();
		TestProcedure procedure = createTestProcedure(tc);
		iteratePath(path, new PathConsumer() {

			@Override
			public void consumeProcessStart(ProcessStart start, ProcessConnection outgoingConnection,
					int vertexNumber) {
				if (!hasCondition(outgoingConnection)) {
					return;
				}
				String description = start.getDescription();
				AssigmentValues varCond = extractVariableAndConditionFromExpression(outgoingConnection.getCondition());
				String parameterName = getCountingParameterName(seenParameterNames, varCond.name);
				TestParameter testParameter = testParameters.get(parameterName);
				createTestStep(makePrecondition(outgoingConnection), makeExpectedOutcome(outgoingConnection),
						vertexNumber, procedure, testParameter, description);

			}

			@Override
			public void consumeProcessEnd(ProcessEnd end, int vertexNumber) {
				// Nothing to do
			}

			@Override
			public void consumeProcessDecision(ProcessDecision decision, ProcessConnection outgoingConnection,
					int vertexNumber) {
				String description = decision.getDescription();
				String parameterName = getCountingParameterName(seenParameterNames, decision.getName());
				TestParameter testParameter = testParameters.get(parameterName);
				createTestStep(makeAction(decision, outgoingConnection), makeExpectedOutcome(outgoingConnection),
						vertexNumber, procedure, testParameter, description);
			}

			@Override
			public void consumeProcessStep(ProcessStep step, ProcessConnection outgoingConnection, int vertexNumber) {
				String description = step.getDescription();
				String expectedOutcome = "";
				TestParameter testParameter = null;
				if (hasExpectedOutcome(step)) {
					AssigmentValues varCond = extractVariableAndConditionFromExpression(step.getExpectedOutcome());
					String parameterName = getCountingParameterName(seenParameterNames, varCond.name);
					testParameter = testParameters.get(parameterName);
					expectedOutcome = makeExpectedOutcome(step, outgoingConnection);
				}
				createTestStep(makeAction(step), expectedOutcome, vertexNumber, procedure, testParameter, description);
				
			}
		});
		return procedure;
	}

	/** Create a test procedure for the given test case. */
	private TestProcedure createTestProcedure(TestCase testCase) {
		TestProcedure procedure = TestspecificationFactory.eINSTANCE.createTestProcedure();
		procedure.setId(SpecmateEcoreUtil.getIdForChild());
		procedure.setName(SpecmateEcoreUtil.getNameForChild(testCase, procedure.eClass()));
		return procedure;
	}

	/** Creates a precondition text from a connection */
	private String makePrecondition(ProcessConnection connection) {
		return "Establish precondition: " + connection.getCondition();
	}

	/** Creates an action text from a process step */
	private String makeAction(ProcessStep step) {
		return step.getName();
	}

	/** Creates an action text from a process decision and a following connection */
	private String makeAction(ProcessDecision decision, ProcessConnection connection) {
		if (!hasCondition(connection)) {
			return "";
		}
		return "Establish condition: " + decision.getName() + "=" + connection.getCondition();
	}

	/** Creates an expected outcome text from a step and a following connection */
	private String makeExpectedOutcome(ProcessStep step, ProcessConnection connection) {
		List<String> checkParts = new ArrayList<>();

		if (hasExpectedOutcome(step)) {
			checkParts.add(step.getExpectedOutcome());
		}

		if (hasCondition(connection)) {
			checkParts.add(connection.getCondition());
		}
		return StringUtils.join(checkParts, ", ");
	}

	/** Creates an expected outcome text from a connection */
	private String makeExpectedOutcome(ProcessConnection connection) {
		if (hasCondition(connection)) {
			return connection.getCondition();
		}
		return "";
	}

	/** Returns true if the step has a non-empty expected outcome */
	private boolean hasExpectedOutcome(ProcessStep step) {
		return step.getExpectedOutcome() != null && !step.getExpectedOutcome().equals("");
	}

	/**
	 * Creates a new test step and adds it to the procedure at the given position
	 */
	private void createTestStep(String action, String expectedOutcome, int position, IContainer procedure,
			TestParameter testParameter, String description) {
		TestStep testStep = TestspecificationFactory.eINSTANCE.createTestStep();
		testStep.setName(action);
		testStep.setDescription(description);
		testStep.setPosition(position);
		testStep.setExpectedOutcome(expectedOutcome);
		testStep.setId(SpecmateEcoreUtil.getIdForChild());
		if (testParameter != null) {
			testStep.getReferencedTestParameters().add(testParameter);
		}
		procedure.getContents().add(testStep);
	}

	/** Class to store values for a parameter assignment */
	private class AssigmentValues {
		public String name;
		public String condition;
		public ParameterType type;

		public AssigmentValues(String name, String condition, ParameterType type) {
			this.name = name;
			this.condition = condition;
			this.type = type;
		}

		public AssigmentValues(String name, String condition) {
			this.name = name;
			this.condition = condition;
		}

	}

	/** Interface for sequentially processing a graph path */
	private interface PathConsumer {
		void consumeProcessStart(ProcessStart start, ProcessConnection outgoingConnection, int vertexNumber);

		void consumeProcessEnd(ProcessEnd end, int vertexNumber);

		void consumeProcessDecision(ProcessDecision decision, ProcessConnection outgoingConnection, int vertexNumber);

		void consumeProcessStep(ProcessStep step, ProcessConnection outgoingConnection, int vertexNumber);
	}
}
