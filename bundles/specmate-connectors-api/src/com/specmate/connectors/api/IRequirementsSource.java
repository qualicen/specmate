package com.specmate.connectors.api;

import java.util.Collection;
import java.util.Set;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;

public interface IRequirementsSource {

	Collection<Requirement> getRequirements() throws SpecmateException;

	Requirement getRequirementById(String id) throws SpecmateException;

	String getId();

	/**
	 * Returns the parent folder for a requirement, or null if the requirement has
	 * no parent folder
	 * 
	 * @param requirement
	 * @return
	 * @throws SpecmateException
	 */
	IContainer getContainerForRequirement(Requirement requirement) throws SpecmateException;

	/**
	 * Returns a set of projects given credentials have access to. Returns null if
	 * credentials are invalid.
	 * 
	 * @param project        The project the user wants to login
	 * @param projectService The project service (helpful to retrieve other
	 *                       projects).
	 * @return Returns a set of projects the credentials can access. If succesfull,
	 *         this set must contain the project the user tries to login.
	 *         Furthermore, this set may contain other projects, the user can also
	 *         access. E.g., a user log on to a jira-project and is logged in also
	 *         to other jira-projects his account can also access.
	 */
	Set<IProject> authenticate(String username, String password, IProject project, IProjectService projectService)
			throws SpecmateException;

}
