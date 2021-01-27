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

	/** The configured project prefix for generated projects */
	private String projectPrefix;

	/** The associated multiproject */
	private IMultiProject multiProject;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateException {
		validateConfig(properties);

		id = (String) properties.get(IProjectConfigService.KEY_CONNECTOR_ID);		
		url = (String) properties.get(JiraConfigConstants.KEY_JIRA_URL);
		username = (String) properties.get(JiraConfigConstants.KEY_JIRA_USERNAME);
		password = (String) properties.get(JiraConfigConstants.KEY_JIRA_PASSWORD);
		projectPrefix = (String) properties.getOrDefault(JiraConfigConstants.KEY_JIRA_MULTIPROJECT_PREFIX, id);
		
		logService.log(LogService.LOG_DEBUG, "Initialized Jira Multi Connector with " + properties.toString() + ".");
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
		
		for ( String jiraProjectId : JiraUtil.getProjects(url, username, password)) {	
			
			String specmateProjectId = projectPrefix + "_" + jiraProjectId;			
			projectConfigs.put (specmateProjectId, getProjectConfig(specmateProjectId, jiraProjectId) );
		}
		
		return projectConfigs;
	}
	
	private Map<String, String> getProjectConfig(String specmateProjectId, String jiraProjectId) {
		
		HashMap<String, String> projectConfig = new HashMap<>();
		
		String connectorPrefix = IProjectConfigService.PROJECT_PREFIX + specmateProjectId + ".connector.";
		projectConfig.put(connectorPrefix + "pid", JiraConfigConstants.CONNECTOR_PID );
		projectConfig.put(connectorPrefix + JiraConfigConstants.KEY_JIRA_URL, url);		
		projectConfig.put(connectorPrefix + JiraConfigConstants.KEY_JIRA_PROJECT, jiraProjectId);
		projectConfig.put(connectorPrefix + JiraConfigConstants.KEY_JIRA_USERNAME, username);
		projectConfig.put(connectorPrefix + JiraConfigConstants.KEY_JIRA_PASSWORD, password);		
		projectConfig.put(connectorPrefix + JiraConfigConstants.KEY_JIRA_WITH_FOLDERS, "false");
		projectConfig.put(connectorPrefix + JiraConfigConstants.KEY_JIRA_DIRECT_SQL, "%project%");
		
		// TODO: add/use template config fields
		
		return projectConfig;
	}

}
