package com.specmate.connectors.jira.internal.services;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.specmate.common.exception.SpecmateInternalException;

public interface IJiraClientFactory {
	public JiraRestClient createJiraClient(String serverUrl, String serverUsername, String serverPassword)
			throws SpecmateInternalException;
}
