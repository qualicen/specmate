package com.specmate.connectors.api;

import java.util.Map;
import java.util.Set;

public interface IProjectService {

	/**
	 * Returns a configured project with the given id, or null if no such project
	 * exists
	 */
	IProject getProject(String projectId);

	/**
	 * Gets all project ids
	 *
	 * @return
	 */
	Set<String> getProjectIds();

	/**
	 * Gets all projects
	 *
	 * @return
	 */
	Map<String, IProject> getProjects();
}
