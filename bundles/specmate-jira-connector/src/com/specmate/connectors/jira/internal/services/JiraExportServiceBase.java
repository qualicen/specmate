package com.specmate.connectors.jira.internal.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.log.LogService;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.jira.config.JiraConfigConstants;
import com.specmate.export.api.ExporterBase;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.export.Export;
import com.specmate.model.testspecification.TestProcedure;
import com.specmate.model.testspecification.TestSpecification;

import io.atlassian.util.concurrent.Promise;

public abstract class JiraExportServiceBase extends ExporterBase {

	/** The issue type for tests */
	protected IssueType testType;

	/** URL to the jira instance */
	protected String url;

	/** Name of the jira project */
	protected String projectName;

	/** User name of the techical user to acess jira */
	protected String username;

	/** Password of the technical user */
	protected String password;

	/** The logging service */
	protected LogService logService;

	public JiraExportServiceBase(String type) {
		super(type);
	}

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		url = (String) properties.get(JiraConfigConstants.KEY_JIRA_URL);
		projectName = (String) properties.get(JiraConfigConstants.KEY_JIRA_PROJECT);
		username = (String) properties.get(JiraConfigConstants.KEY_JIRA_USERNAME);
		password = (String) properties.get(JiraConfigConstants.KEY_JIRA_PASSWORD);

		try {
			new URL(url);
		} catch (MalformedURLException e) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, "Malformed jira URL: " + url);
		}
		if (StringUtils.isBlank(projectName)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"No or empty project name given for jira exporter.");
		}
		if (StringUtils.isBlank(username)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"No or empty client id given for jira exporter.");
		}
		if (StringUtils.isBlank(password)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"No or empty password given for jira exporter.");
		}

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

	public Optional<Export> exportTestProcedure(TestProcedure testProcedure) throws SpecmateException {
		JiraRestClient jiraClient = null;
		try {
			try {
				jiraClient = JiraUtil.createJiraRESTClient(url, username, password);
			} catch (URISyntaxException e) {
				throw new SpecmateInternalException(ErrorCode.JIRA, e);
			}

			IssueInputBuilder issueBuilder = new IssueInputBuilder(projectName, testType.getId());
			issueBuilder.setSummary("Specmate Exported Test Procedure: " + testProcedure.getName());
			exportTestStepsPre(testProcedure, issueBuilder);
			IssueInput issueInput = issueBuilder.build();
			Promise<BasicIssue> result = jiraClient.getIssueClient().createIssue(issueInput);
			try {
				BasicIssue issue = result.get();
				exportTestStepsPost(testProcedure, issue);

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

	protected void exportTestStepsPre(TestProcedure procedure, IssueInputBuilder issueBuilder) {
	};

	protected void exportTestStepsPost(TestProcedure procedure, BasicIssue issue) throws SpecmateException {
	};

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		try {
			return JiraUtil.authenticate(url, projectName, username, password);
		} catch (SpecmateException e) {
			logService.log(LogService.LOG_ERROR, "Exception occured when authorizing for export", e);
			return false;
		}
	}

	@Override
	public boolean canExportTestSpecification() {
		return false;
	}

	@Override
	public boolean canExportTestProcedure() {
		return true;
	}

	@Override
	public Optional<Export> export(Object exportTarget) throws SpecmateException {
		if (exportTarget instanceof TestProcedure) {
			return exportTestProcedure((TestProcedure) exportTarget);
		}
		if (exportTarget instanceof TestSpecification) {
			return exportTestSpecification((TestSpecification) exportTarget);
		}
		throw new SpecmateInternalException(ErrorCode.JIRA,
				"Cannot export object of type " + exportTarget.getClass().getName());
	}

	private Optional<Export> exportTestSpecification(TestSpecification exportTarget) throws SpecmateInternalException {
		throw new SpecmateInternalException(ErrorCode.JIRA, "Test specification export to jira not supported");
	}

}
