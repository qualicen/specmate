package com.specmate.connectors.api;

import java.util.Map;

import com.specmate.common.exception.SpecmateException;

/**
 * A multi connector is meant to generate projects dynamically. For example, a
 * jira multi connector may scan a jira server for existing projects and creates
 * a corresponding specmate project for every existing jira project
 * automatically.
 */
public interface IMultiConnector {

	public static String KEY_PID = "pid";

	public static String KEY_CONNECTOR = "connector.";

	/**
	 * Returns the id of the connector
	 */
	String getId();

	/**
	 * Returns the project the connector is associated with
	 */
	IMultiProject getMultiProject();

	/**
	 * Associcates the connector with a project
	 */
	void setMultiProject(IMultiProject multiProject);

	/**
	 * Returns project configs. Key is project id as it is known in the target
	 * system (e.g., the name of the jira project). Value is a set of parameters for
	 * each project. The project parameters must be without prefix. Meaning, without
	 * 'multiproject.<projectname>.multiconnector'
	 */
	Map<String, Map<String, String>> getProjectConfigs() throws SpecmateException;
}
