package com.specmate.connectors.jira.internal.services;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.api.IExportService;
import com.specmate.connectors.api.IProjectConfigService;
import com.specmate.connectors.jira.config.JiraConnectorConfig;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.testspecification.TestProcedure;

@Component(immediate = true, service = IExportService.class, configurationPid = JiraConnectorConfig.EXPORTER_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JiraExportService implements IExportService {

	private LogService logService;
	private JiraRestClient jiraClient;
	private IssueType testType;
	private String id;
	private String url;
	private String projectName;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		id = (String) properties.get(IProjectConfigService.KEY_CONNECTOR_ID);
		url = (String) properties.get(JiraConnectorConfig.KEY_JIRA_URL);
		projectName = (String) properties.get(JiraConnectorConfig.KEY_JIRA_PROJECT);
		String username = (String) properties.get(JiraConnectorConfig.KEY_JIRA_USERNAME);
		String password = (String) properties.get(JiraConnectorConfig.KEY_JIRA_PASSWORD);

		try {
			jiraClient = JiraClientFactory.createJiraRESTClient(url, username, password);
		} catch (URISyntaxException e) {
			throw new SpecmateInternalException(ErrorCode.JIRA, e);
		}

		Iterable<IssueType> issueTypes = jiraClient.getMetadataClient().getIssueTypes().claim();
		Spliterator<IssueType> issueTypesSpliterator = Spliterators.spliteratorUnknownSize(issueTypes.iterator(), 0);

		testType = StreamSupport.stream(issueTypesSpliterator, false)
				.filter(issueType -> issueType.getName().equals("Test")).findFirst().orElseGet(null);
		if (testType == null) {
			logService.log(LogService.LOG_ERROR, "Could not get Issue Type for Tests");
		}
	}

	@Override
	public void export(TestProcedure testProcedure) throws SpecmateException {
		IssueInputBuilder issueBuilder = new IssueInputBuilder(projectName, testType.getId());
		issueBuilder.setSummary("Specmate Exported Test Procedure: " + testProcedure.getName());
		IssueInput issueInput = issueBuilder.build();
		jiraClient.getIssueClient().createIssue(issueInput);
	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		return testType != null;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

}
