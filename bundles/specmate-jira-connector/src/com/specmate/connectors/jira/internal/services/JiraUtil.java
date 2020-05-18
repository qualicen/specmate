package com.specmate.connectors.jira.internal.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.specmate.common.exception.SpecmateAuthorizationException;
import com.specmate.common.exception.SpecmateException;
import com.specmate.model.testspecification.TestStep;

public class JiraUtil {

	public static JiraRestClient createJiraRESTClient(String url, String username, String password)
			throws URISyntaxException {
		return new AsynchronousJiraRestClientFactory().createWithAuthenticationHandler(new URI(url),
				new BasicAuthHandler(username, password));
	}

	public static boolean authenticate(String url, String project, String username, String password)
			throws SpecmateException {
		JiraRestClient client = null;
		try {
			client = JiraUtil.createJiraRESTClient(url, username, password);
			client.getProjectClient().getProject(project).claim();
		} catch (URISyntaxException e) {
			throw new SpecmateAuthorizationException("Jira authentication failed.", e);
		} catch (RestClientException e) {
			Integer status = e.getStatusCode().get();
			if (status == 401) {
				return false;
			}
			return false;
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					// an error occured - better return false
					return false;
				}
			}
		}
		return true;
	}

	public static JSONObject stepToJson(TestStep step, boolean isCloud) {
		JSONObject stepObj = new JSONObject();
		String action = step.getDescription();
		String result = step.getExpectedOutcome();
		if (StringUtils.isBlank(action)) {
			action = "(empty)";
		}
		stepObj.put(isCloud ? "action" : "step", action);
		stepObj.put("result", result);
		return stepObj;
	}

}
