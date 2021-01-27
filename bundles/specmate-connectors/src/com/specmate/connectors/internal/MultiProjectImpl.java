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
import com.specmate.connectors.config.ProjectConfigService;
import com.specmate.model.administration.ErrorCode;

@Component(immediate = true, service = IMultiProject.class, configurationPid = ProjectConfigService.MULTIPROJECT_CONFIG_FACTORY_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MultiProjectImpl implements IMultiProject {

	/** The project id */
	private String id = null;

	/** The connector of the project */
	private IMultiConnector multiConnector = null;

	@Activate
	public void activate(Map<String, Object> properties) throws SpecmateInternalException {

		Object obj = properties.get(ProjectConfigService.KEY_PROJECT_ID);
		if (obj != null && obj instanceof String) {
			id = (String) properties.get(ProjectConfigService.KEY_PROJECT_ID);
		}

		if (StringUtils.isEmpty(id)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION,
					"Multiproject configured without providing an ID.");
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

}
