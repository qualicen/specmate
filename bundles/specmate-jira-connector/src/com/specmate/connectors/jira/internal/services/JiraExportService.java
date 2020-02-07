package com.specmate.connectors.jira.internal.services;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.util.concurrent.Promise;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.api.IProject;
import com.specmate.connectors.jira.config.JiraConnectorConfig;
import com.specmate.export.api.ITestExporter;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;
import com.specmate.model.testspecification.TestSpecificationSkeleton;
import com.specmate.model.testspecification.TestStep;

@Component(immediate = true, service = ITestExporter.class, configurationPid = JiraConnectorConfig.EXPORTER_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JiraExportService implements ITestExporter {

	private LogService logService;
	private IssueType testType;
	private String url;
	private String projectName;
	private String username;
	private String password;
	private IProject project;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		url = (String) properties.get(JiraConnectorConfig.KEY_JIRA_URL);
		projectName = (String) properties.get(JiraConnectorConfig.KEY_JIRA_PROJECT);
		username = (String) properties.get(JiraConnectorConfig.KEY_JIRA_USERNAME);
		password = (String) properties.get(JiraConnectorConfig.KEY_JIRA_PASSWORD);

		JiraRestClient jiraClient = null;
		try {
			jiraClient = JiraUtil.createJiraRESTClient(url, username, password);
			Iterable<IssueType> issueTypes = jiraClient.getMetadataClient().getIssueTypes().claim();
			Spliterator<IssueType> issueTypesSpliterator = Spliterators.spliteratorUnknownSize(issueTypes.iterator(),
					0);

			testType = StreamSupport.stream(issueTypesSpliterator, false)
					.filter(issueType -> issueType.getName().equals("Test")).findFirst().orElseGet(null);
			if (testType == null) {
				logService.log(LogService.LOG_ERROR, "Could not get Issue Type for Tests");
			}
		} catch (URISyntaxException e) {
			throw new SpecmateInternalException(ErrorCode.JIRA, e);
		} finally {
			if (jiraClient != null) {
				try {
					jiraClient.close();
				} catch (IOException e) {
					logService.log(LogService.LOG_ERROR, "Could not close jira client");
				}
			}
		}
	}

	@Override
	public String getLanguage() {
		return "Atlassian JIRA";
	}

	@Override
	public boolean canExportTestSpecification() {
		return true;
	}

	@Override
	public boolean canExportTestProcedure() {
		return true;
	}

	@Override
	public Optional<TestSpecificationSkeleton> export(Object exportTarget) throws SpecmateException {
		if (exportTarget instanceof TestProcedure) {
			return exportTestProcedure((TestProcedure) exportTarget);
		}
		if (exportTarget instanceof TestSpecification) {
			return exportTestSpecification((TestSpecification) exportTarget);
		}
		throw new SpecmateInternalException(ErrorCode.JIRA,
				"Cannot export object of type " + exportTarget.getClass().getName());
	}

	private Optional<TestSpecificationSkeleton> exportTestSpecification(TestSpecification exportTarget) {
		// not implemented yet
		return Optional.empty();
	}

	public Optional<TestSpecificationSkeleton> exportTestProcedure(TestProcedure testProcedure)
			throws SpecmateException {
		JiraRestClient jiraClient = null;
		try {
			try {
				jiraClient = JiraUtil.createJiraRESTClient(url, username, password);
			} catch (URISyntaxException e) {
				throw new SpecmateInternalException(ErrorCode.JIRA, e);
			}

			IssueInputBuilder issueBuilder = new IssueInputBuilder(projectName, testType.getId());
			issueBuilder.setSummary("Specmate Exported Test Procedure: " + testProcedure.getName());
			StringBuilder builder = new StringBuilder();
			builder.append("||Step||Name||Description||Expected Result||\n");
			List<TestStep> steps = SpecmateEcoreUtil.pickInstancesOf(testProcedure.getContents(), TestStep.class);
			steps.sort((s1, s2) -> Integer.compare(s1.getPosition(), s2.getPosition()));
			int stepNum = 0;
			for (TestStep step : steps) {
				stepNum++;
				builder.append("| " + stepNum + " | " + step.getName() + " | " + step.getDescription() + " | "
						+ step.getExpectedOutcome() + " |\n");
			}
			issueBuilder.setDescription(builder.toString());
			IssueInput issueInput = issueBuilder.build();
			Promise<BasicIssue> result = jiraClient.getIssueClient().createIssue(issueInput);
			try {
				result.get();
			} catch (Exception e) {
				throw new SpecmateInternalException(ErrorCode.JIRA, e);
			}
			return Optional.empty();
		} finally {
			if (jiraClient != null) {
				try {
					jiraClient.close();
				} catch (IOException e) {
					logService.log(LogService.LOG_ERROR, "Could not close jira client", e);
				}
			}
		}

	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		try {
			return JiraUtil.authenticate(url, projectName, username, password);
		} catch (SpecmateException e) {
			logService.log(LogService.LOG_ERROR, "Exception occured when authorizing for export", e);
			return false;
		}
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Override
	public IProject getProject() {
		return project;
	}

}
