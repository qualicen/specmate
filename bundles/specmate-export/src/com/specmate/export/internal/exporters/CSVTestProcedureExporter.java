package com.specmate.export.internal.exporters;

import static com.specmate.export.internal.exporters.ExportUtil.CSV_COL_SEP;
import static com.specmate.export.internal.exporters.ExportUtil.CSV_LINE_SEP;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.osgi.service.component.annotations.Component;

import com.specmate.export.api.IExporter;
import com.specmate.model.export.Export;
import com.specmate.model.export.ExportFactory;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestStep;

@Component(immediate = true)
public class CSVTestProcedureExporter implements IExporter {
	private static final String HEADER = "Step Name" + CSV_COL_SEP + "Action" + CSV_COL_SEP + "Expected Outcome";

	@Override
	public boolean canExportTestProcedure() {
		return true;
	}

	@Override
	public boolean canExportTestSpecification() {
		return false;
	}

	@Override
	public String getType() {
		return "CSV";
	}

	@Override
	public Optional<Export> export(Object object) {
		TestProcedure testprocedure = (TestProcedure) object;
		StringJoiner joiner = new StringJoiner(CSV_LINE_SEP);
		joiner.add(HEADER);
		List<TestStep> testSteps = SpecmateEcoreUtil.pickInstancesOf(testprocedure.getContents(), TestStep.class);
		for (TestStep step : testSteps) {
			joiner.add(step.getName() + CSV_COL_SEP + step.getDescription() + CSV_COL_SEP + step.getExpectedOutcome());
		}
		Export eport = ExportFactory.eINSTANCE.createExport();
		eport.setName(ExportUtil.replaceInvalidChars(testprocedure.getName()) + ".csv");
		eport.setType(getType());
		eport.setContent(joiner.toString());
		return Optional.of(eport);

	}

	@Override
	public String getProjectName() {
		return null;
	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		return true;
	}

	@Override
	public void setProjectName(String project) {

	}

}
