package com.specmate.connectors.api;

/**
 * Base class for a connector
 *
 * @author junkerm
 *
 */
public abstract class ConnectorBase implements IConnector {

	private IProject project;

	public ConnectorBase(IProject project) {
		this.project = project;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}	

	@Override
	public String getLoginPointName() {
		return "Project " + project.getID();
	}

}
