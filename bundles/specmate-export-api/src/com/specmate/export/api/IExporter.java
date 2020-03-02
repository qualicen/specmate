package com.specmate.export.api;

import java.util.Optional;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.export.Export;

public interface IExporter {

	/**
	 * Returns the language of the exporter. The language represents the type of
	 * export and can also be the name of an external system, such as Jira
	 */
	String getType();

	/**
	 * Starts the export of a certain object.
	 *
	 * @return The export representation of the object to be downloaded by a client,
	 *         or Optional.empty if the object has been exported to a backend
	 *         system.
	 * @throws SpecmateException
	 */
	Optional<Export> export(Object object) throws SpecmateException;

	/**
	 * Signals that this exporter can export test specifications
	 */
	boolean canExportTestSpecification();

	/**
	 * Signals that this exporter can export test procedures
	 */
	boolean canExportTestProcedure();

	/**
	 * Query to determine if a user is authorized for an export.
	 */
	boolean isAuthorizedToExport(String username, String password);

	/**
	 * Set the name of the project this exporter is associated with.
	 * 
	 * @param project
	 */
	void setProjectName(String project);

	/**
	 * Returns the name of the project this exporter is associated with.
	 * 
	 * @return
	 */
	String getProjectName();

}