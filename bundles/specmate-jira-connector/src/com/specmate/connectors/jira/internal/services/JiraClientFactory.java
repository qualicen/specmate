package com.specmate.connectors.jira.internal.services;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

public class JiraClientFactory {

	public static JiraRestClient createJiraRESTClient(String url, String username, String password)
			throws URISyntaxException {
		// curl -s -H "Authorization: Basic ***REMOVED***"
		// https://***REMOVED***/rest/api/2/issuetype | jq '.[] | select(.name ==
		// "Test") | .id'
		return new AsynchronousJiraRestClientFactory().createWithAuthenticationHandler(new URI(url),
				new BasicAuthHandler(username, password));
	}
}
