package com.specmate.connectors.api;

public interface IMultiProject {

	/**
	 * @return the id of the multi project
	 */
	String getID();

	/**
	 * @return the defined multi connector for the project, or <code>null</code>.
	 */
	IMultiConnector getConnector();
	
	/**
	 * Creates the name of a specmate project given the technical name of the project.
	 * @param technicalProjectName the technical name of the project (e.g., the name of the corresponding jira project)
	 */
	String createSpecmateProjectName(String technicalProjectName);
}
