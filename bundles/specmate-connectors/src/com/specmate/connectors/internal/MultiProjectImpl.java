package com.specmate.connectors.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.connectors.api.IMultiConnector;
import com.specmate.connectors.api.IMultiProject;
import com.specmate.connectors.api.IProjectConfigService;
import com.specmate.connectors.config.ProjectConfigService;
import com.specmate.model.administration.ErrorCode;

/**
 * The default implementation for multi projects.
 *
 */
@Component(immediate = true, service = IMultiProject.class, configurationPid = ProjectConfigService.MULTIPROJECT_CONFIG_FACTORY_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MultiProjectImpl implements IMultiProject {

	/** The project id */
	private String id = null;

	/** The connector of the project */
	private IMultiConnector multiConnector = null;

	/** The pattern to create the name of the generated projects */
	private String projectNamePattern;

	/** The template config entries for thiis multi project **/
	private Map<String, String> templateConfigEntries;

	/**
	 * The maximum number of projects to handle. Used to keep the number of projects
	 * low. E.g., for testing and demo purposes.
	 **/
	private int maxNumberOfProjects = Integer.MAX_VALUE;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateInternalException {

		Object obj = properties.get(ProjectConfigService.KEY_PROJECT_ID);
		if (obj != null && obj instanceof String) {
			id = (String) properties.get(ProjectConfigService.KEY_PROJECT_ID);
		}

		projectNamePattern = (String) properties.get(IProjectConfigService.KEY_MULTIPROJECT_PROJECTNAMEPATTERN);

		String intString = (String) properties.get(IProjectConfigService.KEY_MULTIPROJECT_MAXNUMBEROFPROJECTS);
		try {
			if (intString != null) {
				maxNumberOfProjects = Integer.parseInt(intString);
			}
		} catch (NumberFormatException nfe) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, "Config value for "
					+ IProjectConfigService.KEY_MULTIPROJECT_MAXNUMBEROFPROJECTS + " not valid: '" + intString + "'");
		}

		retrieveTemplateConfigEntries(properties);

		if (StringUtils.isEmpty(id)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"Multiproject configured without providing an ID.");
		}
	}

	/**
	 * Scans all config properties for template properties and stores them in
	 * 'templateConfigEntries'
	 * 
	 * @param properties all config properties
	 */
	private void retrieveTemplateConfigEntries(Map<String, Object> properties) {

		templateConfigEntries = new HashMap<>();

		String keyPrefix = IProjectConfigService.MULTIPROJECT_PREFIX + id + "."
				+ IProjectConfigService.KEY_MULTIPROJECT_TEMPLATE + ".";
		int keyPrefixLength = keyPrefix.length();

		for (Entry<String, Object> entry : properties.entrySet()) {
			String key = entry.getKey();
			if (key != null && key.startsWith(keyPrefix)) {
				templateConfigEntries.put(key.substring(keyPrefixLength), (String) entry.getValue());
			}
		}
	}

	@Override
	public String getID() {
		return id;
	}

	@Reference(name = "multiconnector")
	public void setMultiConnector(IMultiConnector multiConnector) {
		this.multiConnector = multiConnector;
		multiConnector.setMultiProject(this);
	}

	@Override
	public IMultiConnector getConnector() {
		return multiConnector;
	}

	@Override
	public Map<String, String> getTemplateConfigEntries() {
		return templateConfigEntries;
	}

	@Override
	public String getProjectNamePattern() {
		return projectNamePattern;
	}

	@Override
	public int getMaxNumberOfProjectsConfig() {	
		return maxNumberOfProjects;
	}
}
