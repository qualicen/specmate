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
	 * Returns a list of project configs. Key is project id, value is a set of config parameters.
	 */
	Map<String, Map<String, String>> getProjectConfigs() throws SpecmateException;	
}
