package com.specmate.connectors.api;

import com.specmate.common.exception.SpecmateException;

public interface IProjectConfigService {
	/** The prefix for project configuration keys */
	public static final String PROJECT_PREFIX = "project.";

	/** The prefix for multiproject configuration keys */
	public static final String MULTIPROJECT_PREFIX = "multiproject.";

	/** The PID of the project config factory */
	public static final String PROJECT_CONFIG_FACTORY_PID = "com.specmate.connectors.projectconfigfactory";
	
	/** The PID of the multi project config factory */
	public static final String MULTIPROJECT_CONFIG_FACTORY_PID = "com.specmate.connectors.multiprojectconfigfactory";

	/** The configuration key for the id of a connector */
	public static final String KEY_CONNECTOR_ID = "connectorID";

	/** The configuration key for the id of an exporter */
	public static final String KEY_EXPORTER_ID = "exporterID";

	/** the configuration key for the id of a project */
	public static final String KEY_PROJECT_ID = "projectID";

	/** the configuration key for the library folders of a project */
	public static final String KEY_PROJECT_LIBRARY_FOLDERS = "libraryFolders";

	/** The configuration key for the list of projects. */
	public static final String KEY_PROJECT_IDS = PROJECT_PREFIX + "projects";

	/** The configuration key for the list of multiprojects. */
	public static final String KEY_MULTIPROJECT_IDS = MULTIPROJECT_PREFIX + "multiprojects";

	/** The configuration key for the list of top-level library folder ids. */
	public static final String KEY_PROJECT_LIBRARY = ".library";

	/** The configuration key for the library name */
	public static final String KEY_PROJECT_LIBRARY_NAME = ".name";

	/** The configuration key for the library description */
	public static final String KEY_PROJECT_LIBRARY_DESCRIPTION = ".description";

	/**
	 * Configures the given projects based on the configuration data from the
	 * configuration service.
	 */
	public void configureProjects(String[] projectIDs) throws SpecmateException;

	/**
	 * Configures the given multiprojects based on the configuration data from the
	 * configuration service.
	 */
	public void configureMultiProjects(String[] multiProjectIDs) throws SpecmateException;

}
