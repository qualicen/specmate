package com.specmate.connectors.api;

import java.util.Map;

import com.specmate.common.exception.SpecmateException;

public interface IMultiConnector {

	/**
	 * Returns the id of the connector
	 */
	String getId();

	/**
	 * Returns the project the connector is associated with
	 * 
	 * @return
	 */
	IMultiProject getMultiProject();

	/**
	 * Associcates the connector with a project
	 */
	void setMultiProject(IMultiProject multiProject);

	/**
	 * Returns a list of project configs. Key is project id as it is known in the
	 * target system (e.g., the name of the jira project). Value is a set of
	 * parameters for each project. The project parameters must be without prefix.
	 * Meaning, without 'multiproject.<projectname>.multiconnector'
	 */
	Map<String, Map<String, String>> getProjectConfigs() throws SpecmateException;
}
