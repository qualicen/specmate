package com.specmate.connectors.jira.internal.services;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.jira.config.JiraConfigConstants;
import com.specmate.export.api.IExporter;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestStep;

/** Exporter for Tests to Jira */
@Component(immediate = true, service = IExporter.class, configurationPid = JiraConfigConstants.EXPORTER_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JiraExportService extends JiraExportServiceBase {

	/** Reference to the logging service */
	LogService logService;

	public JiraExportService() {
		super("Atlassian JIRA");
	}

	@Override
	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		super.activate(properties);
	}

	@Override
	protected void exportTestStepsPre(TestProcedure testProcedure, IssueInputBuilder issueBuilder) {
		StringBuilder builder = new StringBuilder();
		builder.append("||Step||Name||Description||Expected Result||\n");
		List<TestStep> steps = SpecmateEcoreUtil.getStepsSorted(testProcedure);
		int stepNum = 0;
		for (TestStep step : steps) {
			stepNum++;
			builder.append("| " + stepNum + " | " + step.getName() + " | " + step.getDescription() + " | "
					+ step.getExpectedOutcome() + " |\n");
		}
		issueBuilder.setDescription(builder.toString());
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

}
