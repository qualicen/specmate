package com.specmate.export.internal.exporters;

import static com.specmate.export.internal.exporters.ExportUtil.replaceInvalidChars;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import com.specmate.export.api.IExporter;
import com.specmate.model.testspecification.ParameterAssignment;
import com.specmate.model.testspecification.TestCase;
import com.specmate.model.testspecification.TestParameter;
import com.specmate.model.testspecification.TestSpecification;

/** Exports a test specification as CSV */
@Component(immediate = true, service = IExporter.class)
public class CSVTestSpecificationExporter extends TestSpecificationExporterBase {

	public CSVTestSpecificationExporter() {
		super("CSV");
	}

	@Override
	protected void generateHeader(StringBuilder sb, TestSpecification testSpecification2,
			List<TestParameter> parameters) {
		StringJoiner joiner = new StringJoiner(ExportUtil.CSV_COL_SEP);
		joiner.add("\"TC\"");
		for (TestParameter param : parameters) {
			joiner.add(
					StringUtils.wrap(param.getType().toString() + " - " + param.getName(), ExportUtil.CSV_TEXT_WRAP));
		}
		sb.append(joiner).append(ExportUtil.CSV_LINE_SEP);
	}

	@Override
	protected void generateFooter(StringBuilder sb, TestSpecification testSpecification) {

	}

	@Override
	protected void generateTestCaseFooter(StringBuilder sb, TestCase tc) {
		sb.append(ExportUtil.CSV_LINE_SEP);
	}

	@Override
	protected void generateTestCaseHeader(StringBuilder sb, TestSpecification ts, TestCase tc) {
		sb.append(tc.getName() + ExportUtil.CSV_COL_SEP);
	}

	@Override
	protected void generateTestCaseParameterAssignments(StringBuilder sb, List<ParameterAssignment> assignments,
			List<TestParameter> parameters) {
		StringJoiner joiner = new StringJoiner(ExportUtil.CSV_COL_SEP);
		for (TestParameter parameter : parameters) {
			boolean added = false;
			for (ParameterAssignment assignment : assignments) {
				if (parameter.getName().equals(assignment.getParameter().getName())) {
					added = true;
					String assignmentValue = assignment.getCondition();
					String characterToEscape = "=";
					String escapeString = StringUtils.isEmpty(assignmentValue) ? ""
							: escapeString(assignmentValue, characterToEscape);
					joiner.add(StringUtils.wrap(escapeString + assignmentValue, ExportUtil.CSV_TEXT_WRAP));
					break;
				}
			}
			if (!added) {
				joiner.add(StringUtils.wrap("", ExportUtil.CSV_TEXT_WRAP));
			}
		}
		sb.append(joiner.toString());
	}

	@Override
	protected String generateFileName(TestSpecification testSpecification) {
		return replaceInvalidChars(testSpecification.getName()) + ".csv";
	}

	protected String escapeString(String stringToCheck, String characterToEscape) {
		String escapeCharacter = (stringToCheck.substring(0, 1).equals(characterToEscape)) ? "'" : "";
		return escapeCharacter;
	}

	@Override
	public String getProjectName() {
		return null;
	}

	@Override
	public void setProjectName(String project) {

	}

}
