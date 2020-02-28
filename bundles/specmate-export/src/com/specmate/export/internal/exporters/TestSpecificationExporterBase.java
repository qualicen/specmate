package com.specmate.export.internal.exporters;

import static com.specmate.export.internal.exporters.ExportUtil.replaceInvalidChars;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.specmate.export.api.IExporter;
import com.specmate.model.export.Export;
import com.specmate.model.export.ExportFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.ParameterAssignment;
import com.specmate.model.testspecification.TestCase;
import com.specmate.model.testspecification.TestParameter;
import com.specmate.model.testspecification.TestSpecification;

/** Base class for Test Specification Exporters */
public abstract class TestSpecificationExporterBase implements IExporter {

	/** the language for the export */
	protected String language;

	Comparator<TestParameter> parameterComparator = (p1, p2) -> compareParameter(p1, p2);
	Comparator<ParameterAssignment> assignmentComparator = (a1, a2) -> compareParameter(a1.getParameter(),
			a2.getParameter());

	/** constructor */
	public TestSpecificationExporterBase(String language) {
		this.language = language;
	}

	/** getter for language */
	@Override
	public String getType() {
		return language;
	}

	@Override
	public boolean canExportTestProcedure() {
		return false;
	}

	@Override
	public boolean canExportTestSpecification() {
		return true;
	}

	/** Generates an export for the test specification */
	@Override
	public Optional<Export> export(Object obj) {
		TestSpecification testSpecification = (TestSpecification) obj;
		StringBuilder sb = new StringBuilder();

		Export tss = ExportFactory.eINSTANCE.createExport();
		tss.setType(language);
		tss.setName(generateFileName(testSpecification));

		List<TestParameter> parameters = getParameters(testSpecification);

		generateHeader(sb, testSpecification, parameters);
		for (TestCase tc : getTestCases(testSpecification)) {
			generateTestCaseHeader(sb, testSpecification, tc);
			List<ParameterAssignment> assignments = getTestCaseParameterAssignments(tc);
			generateTestCaseParameterAssignments(sb, assignments, parameters);
			generateTestCaseFooter(sb, tc);
		}
		generateFooter(sb, testSpecification);
		tss.setContent(sb.toString());

		return Optional.of(tss);
	}

	private static int compareParameter(TestParameter p1, TestParameter p2) {
		return p1.getType().compareTo(p2.getType()) * 10 + Integer.signum(p1.getName().compareTo(p2.getName()));
	}

	/**
	 * Return Test parameter assignments sorted by type (input before output)
	 */
	private List<ParameterAssignment> getTestCaseParameterAssignments(TestCase tc) {
		return SpecmateEcoreUtil.pickInstancesOf(tc.getContents(), ParameterAssignment.class).stream()
				.sorted(assignmentComparator).collect(Collectors.toList());
	}

	/** Return Test parameters sorted by type (input before output) */
	private List<TestParameter> getParameters(TestSpecification testSpecification) {
		return SpecmateEcoreUtil.pickInstancesOf(testSpecification.getContents(), TestParameter.class).stream()
				.sorted(parameterComparator).collect(Collectors.toList());
	}

	private List<TestCase> getTestCases(TestSpecification testSpecification) {
		return SpecmateEcoreUtil.pickInstancesOf(testSpecification.getContents(), TestCase.class);
	}

	protected void appendParameterValue(StringBuilder sb, ParameterAssignment pa) {
		sb.append("___");
		sb.append(replaceInvalidChars(pa.getParameter().getName()));
		sb.append("__");
		sb.append(replaceInvalidChars(pa.getCondition()));
	}

	protected void appendDateComment(StringBuilder sb) {
		sb.append("/*\n");
		sb.append(" * Datum: ");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date exportDate = new Date();
		sb.append(sdf.format(exportDate));
		sb.append("\n */\n\n");
	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		return true;
	}

	protected abstract void generateHeader(StringBuilder sb, TestSpecification testSpecification,
			List<TestParameter> parameters);

	protected abstract void generateFooter(StringBuilder sb, TestSpecification testSpecification);

	protected abstract void generateTestCaseFooter(StringBuilder sb, TestCase tc);

	protected abstract void generateTestCaseHeader(StringBuilder sb, TestSpecification ts, TestCase tc);

	protected abstract void generateTestCaseParameterAssignments(StringBuilder sb,
			List<ParameterAssignment> assignments, List<TestParameter> parameters);

	protected abstract String generateFileName(TestSpecification testSpecification);
}
