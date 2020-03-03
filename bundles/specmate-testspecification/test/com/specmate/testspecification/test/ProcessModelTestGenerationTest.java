package com.specmate.testspecification.test;

import static com.specmate.model.testspecification.ParameterType.INPUT;
import static com.specmate.model.testspecification.ParameterType.OUTPUT;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.processes.Process;
import com.specmate.model.processes.ProcessConnection;
import com.specmate.model.processes.ProcessDecision;
import com.specmate.model.processes.ProcessEnd;
import com.specmate.model.processes.ProcessNode;
import com.specmate.model.processes.ProcessStart;
import com.specmate.model.processes.ProcessStep;
import com.specmate.model.processes.ProcessesFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.ParameterAssignment;
import com.specmate.model.testspecification.ParameterType;
import com.specmate.model.testspecification.TestCase;
import com.specmate.model.testspecification.TestParameter;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestStep;
import com.specmate.model.testspecification.TestspecificationFactory;
import com.specmate.testspecification.internal.generators.ProcessTestCaseGenerator;

public class ProcessModelTestGenerationTest {

	@Test
	public void testModelGeneration() throws SpecmateException {
		TestSpecification ts = generateTestSpecification(getMixedProcess1());
		
		List<TestParameter> parameters = SpecmateEcoreUtil.pickInstancesOf(ts.getContents(), TestParameter.class);
		
		Assert.assertEquals(6, parameters.size());
		
		assertParameter(parameters, "S1", INPUT);
		assertParameter(parameters, "S2", INPUT);
		assertParameter(parameters, "step1 outcome", OUTPUT);
		assertParameter(parameters, "step2 outcome", OUTPUT);
		assertParameter(parameters, "decision", INPUT);
		assertParameter(parameters, "decision 2", INPUT);

		List<TestCase> testCases = SpecmateEcoreUtil.pickInstancesOf(ts.getContents(), TestCase.class);
		
		Assert.assertEquals(3, testCases.size());
		
		assertTestCase(testCases, Arrays.asList(
				Pair.of("S1", "is present"),
				Pair.of("step1 outcome", "present"),
				Pair.of("decision", "B")));

		assertTestCase(testCases,
				Arrays.asList(Pair.of("S2", "is present"),
						Pair.of("step2 outcome", "is present"), Pair.of("decision", "A")));

		assertTestCase(testCases,
				Arrays.asList(Pair.of("S1", "is present"), Pair.of("step1 outcome", "present"), Pair.of("decision", "C"), Pair.of("decision 2", "A")));
	}

	@Test
	public void testSimpleProcess1() throws SpecmateException {
		List<TestCase> testCases = getTestCases(getSimpleProcess1());
		Assert.assertEquals(1, testCases.size());
		List<TestProcedure> testProcs = SpecmateEcoreUtil.pickInstancesOf(testCases.get(0).getContents(),
				TestProcedure.class);
		Assert.assertEquals(1, testProcs.size());
		List<TestStep> testSteps = SpecmateEcoreUtil.pickInstancesOf(testProcs.get(0).getContents(), TestStep.class);
		Assert.assertEquals(1, testSteps.size());
		TestStep testStep = testSteps.get(0);
		Assert.assertEquals("step-1", testStep.getName());
	}

	@Test
	public void testStartforkedProcess() throws SpecmateException {
		List<TestCase> testCases = getTestCases(getStartForkedProcess1());
		Assert.assertEquals(2, testCases.size());
		
		Set<TestProcedure> testProcedures = testCases.stream().flatMap(testCase -> SpecmateEcoreUtil.pickInstancesOf(testCase.getContents(),
				TestProcedure.class).stream()).collect(Collectors.toSet());
		
		Assert.assertEquals(2, testProcedures.size());
		
		Set<TestStep> testSteps = testProcedures.stream().flatMap(testProcedure -> SpecmateEcoreUtil.pickInstancesOf(testProcedure.getContents(), TestStep.class).stream()).collect(Collectors.toSet());
		
		Assert.assertEquals(2, testSteps.size());

		Assert.assertTrue(testSteps.stream().anyMatch(testStep -> testStep.getName().contentEquals("step1")));
		Assert.assertTrue(testSteps.stream().anyMatch(testStep -> testStep.getName().contentEquals("step2")));
	}

	@Test
	public void testDecisionProcess1() throws SpecmateException {
		List<TestCase> testCases = getTestCases(getDecisionProcess1());
		testCases.stream().forEach(testCase -> Assert.assertTrue(isSingleConditionTestCase(testCase)));
		assertConditionCovered(testCases, "A");
		assertConditionCovered(testCases, "B");
	}

	@Test
	public void testDecisionProcess2() throws SpecmateException {
		List<TestCase> testCases = getTestCases(getDecisionProcess2());
		testCases.stream().forEach(testCase -> Assert.assertTrue(isSingleConditionTestCase(testCase)));
		assertConditionCovered(testCases, "A");
		assertConditionCovered(testCases, "B");
	}

	@Test
	public void testDecisionProcess3() throws SpecmateException {
		List<TestCase> testCases = getTestCases(getDecisionProcess3());
		Arrays.asList(Arrays.asList(Pair.of("d1", "A"), Pair.of("d2", "A")),
				Arrays.asList(Pair.of("d1", "B"), Pair.of("d2", "C"))).stream()
				.forEach(values -> assertTestCase(testCases, values));

		assertConditionCovered(testCases, "A");
		assertConditionCovered(testCases, "B");
		assertConditionCovered(testCases, "C");
	}

	@Test
	public void testDecisionProcess4() throws SpecmateException {
		List<TestCase> testCases = getTestCases(getDecisionProcess4());
		Arrays.asList(Arrays.asList(Pair.of("d1", "A"), Pair.of("d2", "A")),
				Arrays.asList(Pair.of("d1", "B"), Pair.of("d2", "B"))).stream()
				.forEach(values -> assertTestCase(testCases, values));
		assertConditionCovered(testCases, "A");
		assertConditionCovered(testCases, "B");
		assertConditionCovered(testCases, "C");
		Assert.assertEquals(testCases.size(), 3);
	}

	@Test
	public void testLoopProcess1() throws SpecmateException {
		List<TestCase> testCases = getTestCases(getLoopProcess1());
		Assert.assertEquals(testCases.size(), 1);
		assertConditionCovered(testCases, "A");
		assertConditionCovered(testCases, "B");
	}

	private void assertConditionCovered(List<TestCase> testCases, String condition) {
		Assert.assertTrue(testCases.stream().flatMap(testCase -> getParameterAssignments(testCase).stream())
				.anyMatch(assignment -> assignment.getCondition().equals(condition)));
	}

	private boolean isSingleConditionTestCase(TestCase testCase) {
		List<ParameterAssignment> parameterAssignments = getParameterAssignments(testCase);
		String condition = parameterAssignments.stream()
				.filter(assignment -> !StringUtils.isEmpty(assignment.getCondition())).findFirst().get().getCondition();
		return !parameterAssignments.stream()
				.anyMatch(assignment -> !assignment.getCondition().equals(condition));
	}

	@SuppressWarnings("unused")
	private void printTestCase(TestCase testCase) {
		System.out.println(testCase.getName() + "> "
				+ getParameterAssignments(testCase).stream()
						.map(assignment -> assignment.getParameter().getName() + " = " + assignment.getCondition())
						.collect(Collectors.joining(", ")));
	}

	private List<ParameterAssignment> getParameterAssignments(TestCase testCase) {
		return SpecmateEcoreUtil.pickInstancesOf(testCase.getContents(), ParameterAssignment.class);
	}

	private TestSpecification generateTestSpecification(Process process) throws SpecmateException {
		TestSpecification ts = TestspecificationFactory.eINSTANCE.createTestSpecification();
		ts.setId("testspec");
		ts.setName("testspec");
		process.getContents().add(ts);
		ProcessTestCaseGenerator generator = new ProcessTestCaseGenerator(ts);
		generator.generate();
		return ts;
	}

	private List<TestCase> getTestCases(Process process) throws SpecmateException {
		TestSpecification testSpecification = generateTestSpecification(process);
		return SpecmateEcoreUtil.pickInstancesOf(testSpecification.getContents(), TestCase.class);
	}

	private void assertParameter(List<TestParameter> parameters, String name, ParameterType type) {
		Assert.assertTrue(
				parameters.stream().anyMatch(p -> p.getName().contentEquals(name) && p.getType().equals(type)));
	}

	private void assertTestCase(List<TestCase> testCases, List<Pair<String, String>> values) {
		Assert.assertTrue(testCases.stream().anyMatch(t -> {
			List<ParameterAssignment> assignments = SpecmateEcoreUtil.pickInstancesOf(t.getContents(),
					ParameterAssignment.class);
			return values.stream().allMatch(
					v -> assignments.stream().anyMatch(a -> a.getParameter().getName().contentEquals(v.getLeft())
							&& a.getCondition().contentEquals(v.getRight())));
		}));
	}

	private ProcessConnection connect(ProcessNode n1, ProcessNode n2, String id) {
		return this.connect(n1, n2, id, null);
	}

	private ProcessConnection connect(ProcessNode n1, ProcessNode n2, String id, String condition) {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		ProcessConnection connection = f.createProcessConnection();
		connection.setSource(n1);
		connection.setTarget(n2);
		connection.setId(id);
		connection.setName(id);
		connection.setCondition(condition);
		return connection;
	}

	private ProcessStep createStep(String id) {
		return this.createStep(id, null);
	}

	private ProcessStep createStep(String id, String expectedOutcome) {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		ProcessStep step = f.createProcessStep();
		step.setId(id);
		step.setName(id);
		step.setExpectedOutcome(expectedOutcome);
		return step;
	}

	private ProcessDecision createDecision(String id, String name) {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		ProcessDecision decision = f.createProcessDecision();
		decision.setId(id);
		decision.setName(name);
		return decision;
	}

	private ProcessStart createStart(String id) {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		ProcessStart start = f.createProcessStart();
		start.setId(id);
		start.setName(id);
		return start;
	}

	private ProcessEnd createEnd(String id) {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		ProcessEnd end = f.createProcessEnd();
		end.setId(id);
		end.setName(id);
		return end;
	}

	private Process getDecisionProcess1() {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();

		ProcessStart start = createStart("start");
		ProcessEnd end = createEnd("end");

		ProcessDecision d1 = createDecision("d1", "d1");
		ProcessDecision d2 = createDecision("d2", "d2");

		ProcessStep sA1 = createStep("sA1");
		ProcessStep sA2 = createStep("sA2");
		ProcessStep sB1 = createStep("sB1");
		ProcessStep sB2 = createStep("sB2");

		ProcessConnection c1 = connect(start, d1, "c1");
		ProcessConnection c2 = connect(d1, sA1, "c2", "A");
		ProcessConnection c3 = connect(d1, sB1, "c3", "B");
		ProcessConnection c4 = connect(sA1, d2, "c4");
		ProcessConnection c5 = connect(sB1, d2, "c5");
		ProcessConnection c6 = connect(d2, sA2, "c6", "A");
		ProcessConnection c7 = connect(d2, sB2, "c7", "B");
		ProcessConnection c8 = connect(sA2, end, "c8");
		ProcessConnection c9 = connect(sB2, end, "c9");

		process.getContents()
				.addAll(Arrays.asList(start, end, d1, d2, sA1, sA2, sB1, sB2, c1, c2, c3, c4, c5, c6, c7, c8, c9));

		return process;
	}

	private Process getDecisionProcess2() {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();

		ProcessStart start = createStart("start");
		ProcessEnd end = createEnd("end");

		ProcessDecision d1 = createDecision("d1", "d1");
		ProcessDecision d2 = createDecision("d2", "d2");

		ProcessStep sA1 = createStep("sA1");
		ProcessStep sA2 = createStep("sA2");
		ProcessStep sB1 = createStep("sB1");

		ProcessConnection c1 = connect(start, d1, "c1");
		ProcessConnection c2 = connect(d1, sA1, "c2", "A");
		ProcessConnection c3 = connect(d1, sB1, "c3", "B");
		ProcessConnection c4 = connect(sA1, d2, "c4");
		ProcessConnection c5 = connect(sB1, d2, "c5");
		ProcessConnection c6 = connect(d2, sA2, "c6", "A");
		ProcessConnection c7 = connect(d2, end, "c7", "B");
		ProcessConnection c8 = connect(sA2, end, "c8");

		process.getContents().addAll(Arrays.asList(start, end, d1, d2, sA1, sA2, sB1, c1, c2, c3, c4, c5, c6, c7, c8));

		return process;
	}

	private Process getDecisionProcess3() {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();

		ProcessStart start = createStart("start");
		ProcessEnd end = createEnd("end");

		ProcessDecision d1 = createDecision("d1", "d1");
		ProcessDecision d2 = createDecision("d2", "d2");

		ProcessStep sA1 = createStep("sA1");
		ProcessStep sA2 = createStep("sA2");
		ProcessStep sB1 = createStep("sB1");
		ProcessStep sC2 = createStep("sC2");

		ProcessConnection c1 = connect(start, d1, "c1");
		ProcessConnection c2 = connect(d1, sA1, "c2", "A");
		ProcessConnection c3 = connect(d1, sB1, "c3", "B");
		ProcessConnection c4 = connect(sA1, d2, "c4");
		ProcessConnection c5 = connect(sB1, d2, "c5");
		ProcessConnection c6 = connect(d2, sA2, "c6", "A");
		ProcessConnection c7 = connect(d2, sC2, "c7", "C");
		ProcessConnection c8 = connect(sA2, end, "c8");
		ProcessConnection c9 = connect(sC2, end, "c9");

		process.getContents()
				.addAll(Arrays.asList(start, end, d1, d2, sA1, sA2, sB1, sC2, c1, c2, c3, c4, c5, c6, c7, c8, c9));

		return process;
	}

	private Process getDecisionProcess4() {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();

		ProcessStart start = createStart("start");
		ProcessEnd end = createEnd("end");

		ProcessDecision d1 = createDecision("d1", "d1");
		ProcessDecision d2 = createDecision("d2", "d2");

		ProcessStep sA1 = createStep("sA1");
		ProcessStep sA2 = createStep("sA2");
		ProcessStep sB1 = createStep("sB1");
		ProcessStep sB2 = createStep("sB2");
		ProcessStep sC = createStep("sC");

		ProcessConnection c1 = connect(start, d1, "c1");
		ProcessConnection c2 = connect(d1, sA1, "c2", "A");
		ProcessConnection c3 = connect(d1, sB1, "c3", "B");
		ProcessConnection c4 = connect(sA1, d2, "c4");
		ProcessConnection c5 = connect(sB1, d2, "c5");
		ProcessConnection c6 = connect(d2, sA2, "c6", "A");
		ProcessConnection c7 = connect(d2, sB2, "c7", "B");
		ProcessConnection c8 = connect(sA2, end, "c8");
		ProcessConnection c9 = connect(sB2, end, "c9");
		ProcessConnection c10 = connect(d2, sC, "c10", "C");
		ProcessConnection c11 = connect(sC, end, "c11");

		process.getContents().addAll(Arrays.asList(start, end, d1, d2, sA1, sA2, sB1, sB2, sC, c1, c2, c3, c4, c5, c6,
				c7, c8, c9, c10, c11));

		return process;
	}

	private Process getLoopProcess1() {
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();

		ProcessStart start = createStart("start");
		ProcessEnd end = createEnd("end");

		ProcessDecision d1 = createDecision("d1", "d1");

		ProcessStep sA = createStep("sA");

		ProcessConnection c1 = connect(start, d1, "c1");
		ProcessConnection c2 = connect(d1, sA, "c2", "A");
		ProcessConnection c3 = connect(sA, d1, "c3");
		ProcessConnection c4 = connect(d1, end, "c3", "B");

		process.getContents().addAll(Arrays.asList(start, end, d1, sA, c1, c2, c3, c4));

		return process;
	}
	
	private Process getMixedProcess1() {
		
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();
		ProcessStart start = createStart("start");
		ProcessStep step_1 = createStep("step-1", "step1 outcome = present");
		ProcessStep step_2 = createStep("step-2", "step2 outcome");
		ProcessStep step_3 = createStep("step-3");
		ProcessDecision decision = createDecision("dec-4", "decision");
		ProcessStep step_4 = createStep("step-4");
		ProcessEnd end = createEnd("end");
		
		ProcessConnection c1 = connect(start, step_1, "c1", "S1");
		ProcessConnection c2 = connect(start, step_2, "c2", "S2");
		ProcessConnection c3 = connect(step_1, step_3, "c3");
		ProcessConnection c4 = connect(step_2, step_3, "c4");
		ProcessConnection c5 = connect(step_3, decision, "c5");
		ProcessConnection c6 = connect(decision, end, "c6", "A");
		ProcessConnection c7 = connect(decision, step_4, "c7", "B");
		ProcessConnection c8 = connect(step_4, end, "c8");
		ProcessConnection c9 = connect(decision, step_3, "c9", "C");

		process.getContents().addAll(Arrays.asList(start, step_1, c1, step_2, c9, step_3, c2, c3,
				decision, end, c5, step_4, c6, c7, c8, c4));

		return process;
	}

	private Process getSimpleProcess1() {
		
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();
		ProcessStart start = createStart("start");
		ProcessStep step = createStep("step-1");
		ProcessEnd end = createEnd("end");
		
		ProcessConnection c1 = connect(start, step, "c1");
		ProcessConnection c2 = connect(step, end, "c2");

		process.getContents().addAll(Arrays.asList(start, step, end, c1, c2));

		return process;
	}
	
	private Process getStartForkedProcess1() {
		
		ProcessesFactory f = ProcessesFactory.eINSTANCE;
		Process process = f.createProcess();
		ProcessStart start = createStart("start");
		ProcessStep step1 = createStep("step1");
		ProcessStep step2 = createStep("step2");
		ProcessEnd end = createEnd("end");

		ProcessConnection c1 = connect(start, step1, "c1");
		ProcessConnection c2 = connect(start, step2, "c2");
		ProcessConnection c3 = connect(step1, end, "c3");
		ProcessConnection c4 = connect(step2, end, "c3");

		process.getContents().addAll(Arrays.asList(start, step1, step2, end, c1, c2, c3, c4));

		return process;
	}


}
