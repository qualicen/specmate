package com.specmate.connectors.api;

/**
 * Base class for a connector
 *
 * @author junkerm
 *
 */
public abstract class ConnectorBase implements IConnector {

	private IProject project;

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
