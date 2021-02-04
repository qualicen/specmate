package com.specmate.connectors.internal;

import java.util.Map;

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

	/** The placeholder for project name patterns **/
	private static final String PATTERN_NAME = "%project%";

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateInternalException {

		Object obj = properties.get(ProjectConfigService.KEY_PROJECT_ID);
		if (obj != null && obj instanceof String) {
			id = (String) properties.get(ProjectConfigService.KEY_PROJECT_ID);
		}

		projectNamePattern = (String) properties.getOrDefault(IProjectConfigService.KEY_MULTIPROJECT_PROJECTNAMEPATTERN,
				id + "_" + PATTERN_NAME);

		if (StringUtils.isEmpty(id)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"Multiproject configured without providing an ID.");
		}
	}

	@Override
	public String createSpecmateProjectName(String technicalProjectName) {
		return projectNamePattern.replace(PATTERN_NAME, technicalProjectName);
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

}
