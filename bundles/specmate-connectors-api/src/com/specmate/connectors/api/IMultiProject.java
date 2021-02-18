package com.specmate.connectors.api;

import java.util.Map;

public interface IMultiProject {

	/** The placeholder for project name patterns **/
	public static final String PATTERN_NAME = "$PROJECT";

	/**
	 * @return the id of the multi project
	 */
	String getID();

	/**
	 * @return the defined multi connector for the project, or <code>null</code>.
	 */
	IMultiConnector getConnector();

	/**
	 * Returns the config value for the projectnamepattern property.
	 */
	String getProjectNamePattern();

	/**
	 * Returns the config entries which are listed as template entries in the
	 * config.
	 */
	Map<String, String> getTemplateConfigEntries();
}
