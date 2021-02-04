package com.specmate.connectors.jira.internal.services;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.api.IMultiConnector;
import com.specmate.connectors.api.IMultiProject;
import com.specmate.connectors.api.IProjectConfigService;
import com.specmate.connectors.jira.config.JiraConfigConstants;
import com.specmate.model.administration.ErrorCode;

/**
 * Multi connector for Jira server and cloud.
 *
 */
@Component(immediate = true, service = IMultiConnector.class, configurationPid = JiraConfigConstants.MULTICONNECTOR_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JiraMultiConnector implements IMultiConnector {

	/** The log service reference */
	private LogService logService;

	/** The configured id of this connector */
	private String id;

	/** The configured Url of the jira server */
	private String url;

	/** The username for jira server */
	private String username;

	/** The password for jira server */
	private String password;

	private Map<String, String> templateProperties;

	/** The associated multiproject */
	private IMultiProject multiProject;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		validateConfig(properties);

		id = (String) properties.get(IProjectConfigService.KEY_CONNECTOR_ID);
		url = (String) properties.get(JiraConfigConstants.KEY_JIRA_URL);
		username = (String) properties.get(JiraConfigConstants.KEY_JIRA_USERNAME);
		password = (String) properties.get(JiraConfigConstants.KEY_JIRA_PASSWORD);
		templateProperties = getTemplateProperties(properties);

		logService.log(LogService.LOG_DEBUG, "Initialized Jira Multi Connector with " + properties.toString() + ".");
	}

	/**
	 * Crates a list of all template properties (without prefix).
	 */
	private static Map<String, String> getTemplateProperties(Map<String, Object> properties) {
		int tmplPrefixLenght = JiraConfigConstants.KEY_JIRA_MULTIPROJECT_TEMPLATE_PREFIX.length();
		Map<String, String> templateProperties = new HashMap<>();
		for (String propertyKey : properties.keySet()) {
			if (propertyKey.startsWith(JiraConfigConstants.KEY_JIRA_MULTIPROJECT_TEMPLATE_PREFIX)) {
				templateProperties.put(propertyKey.substring(tmplPrefixLenght), (String) properties.get(propertyKey));

			}
		}
		return templateProperties;
	}

	private void validateConfig(Map<String, Object> properties) throws SpecmateException {
		String aURL = (String) properties.get(JiraConfigConstants.KEY_JIRA_URL);
		String aUsername = (String) properties.get(JiraConfigConstants.KEY_JIRA_USERNAME);
		String aPassword = (String) properties.get(JiraConfigConstants.KEY_JIRA_PASSWORD);

		if (isEmpty(aURL) || isEmpty(aUsername) || isEmpty(aPassword)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, String.format(
					"Jira Multi Connector (%s) is not well configured. Url, username, and password need to be provided.",
					id));
		}
	}

	@Override
	public IMultiProject getMultiProject() {
		return multiProject;
	}

	@Override
	public void setMultiProject(IMultiProject multiProject) {
		this.multiProject = multiProject;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Map<String, Map<String, String>> getProjectConfigs() throws SpecmateException {

		HashMap<String, Map<String, String>> projectConfigs = new HashMap<>();

		for (String jiraProjectId : JiraUtil.getProjects(url, username, password)) {
			projectConfigs.put(jiraProjectId, getProjectConfig(jiraProjectId));
		}

		return projectConfigs;
	}

	private Map<String, String> getProjectConfig(String jiraProjectId) {

		HashMap<String, String> projectConfig = new HashMap<>();

		// generated properties
		projectConfig.put("pid", JiraConfigConstants.CONNECTOR_PID);
		projectConfig.put(JiraConfigConstants.KEY_JIRA_URL, url);
		projectConfig.put(JiraConfigConstants.KEY_JIRA_PROJECT, jiraProjectId);
		projectConfig.put(JiraConfigConstants.KEY_JIRA_USERNAME, username);
		projectConfig.put(JiraConfigConstants.KEY_JIRA_PASSWORD, password);

		// template properties
		for (Map.Entry<String, String> templatePropertyEntry : templateProperties.entrySet()) {
			projectConfig.put("jira." + templatePropertyEntry.getKey(), templatePropertyEntry.getValue());
		}

		return projectConfig;
	}

}
