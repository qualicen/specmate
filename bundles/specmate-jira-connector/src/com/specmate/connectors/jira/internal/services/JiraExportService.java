package com.specmate.connectors.jira.internal.services;

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
import com.specmate.connectors.api.IExportService;
import com.specmate.connectors.jira.config.JiraConnectorConfig;
import com.specmate.model.testspecification.TestProcedure;

@Component(service = IExportService.class, configurationPid = JiraConnectorConfig.EXPORTER_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JiraExportService implements IExportService {

	private LogService logService;
	private JiraConnector connector;
	private JiraRestClient jiraClient;
	private IssueType testType;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		jiraClient = connector.getJiraClient();
		Iterable<IssueType> issueTypes = jiraClient.getMetadataClient().getIssueTypes().claim();
		Spliterator<IssueType> issueTypesSpliterator = Spliterators.spliteratorUnknownSize(issueTypes.iterator(), 0);
		
		testType = StreamSupport.stream(issueTypesSpliterator, false)
				.filter(issueType -> issueType.getName().equals("Test"))
				.findFirst()
				.orElseGet(null);
		if(testType == null) {
			this.logService.log(LogService.LOG_ERROR, "Could not get Issue Type for Tests");
		}
	}

	@Override
	public void export(TestProcedure testProcedure) throws SpecmateException {
		IssueInputBuilder issueBuilder = new IssueInputBuilder(connector.getProjectName(), testType.getId());
		issueBuilder.setSummary("Specmate Exported Test Procedure: " + testProcedure.getName());
		IssueInput issueInput = issueBuilder.build();
		jiraClient.getIssueClient().createIssue(issueInput);
	}

	@Override
	public boolean isAuthorizedToExport(String username, String password) {
		return this.testType != null;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}


}
