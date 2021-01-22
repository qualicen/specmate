package com.specmate.connectors.api;

import java.util.Collection;
import java.util.Set;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;

/**
 * A connector is a source for requirements and a means for project
 * authentification
 * 
 * @author junkerm
 *
 */
public interface IConnector {

	/**
	 * Returns the id of the connector
	 *
	 * @return
	 */
	String getId();

	/**
	 * Returns the project the connector is associated with
	 * 
	 * @return
	 */
	IProject getProject();

	/**
	 * Associcates the connector with a project
	 */
	void setProject(IProject project);

	/**
	 * Returns the requirements to import
	 *
	 * @throws SpecmateException
	 */
	Collection<Requirement> getRequirements() throws SpecmateException;

	/**
	 * Returns a requirement with a given id
	 *
	 * @throws SpecmateException
	 */
	Requirement getRequirementById(String id) throws SpecmateException;

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
	 * Returns a set of projects given credentials have access to. Returns an empty set if
	 * credentials are invalid.
	 *
	 * @return Returns a set of projects the credentials can access. If successful,
	 *         this set must contain at least the current project which the user
	 *         tries to login. Furthermore, this set may contain other projects, the
	 *         user can also access. E.g., a user log on to a jira-project and is
	 *         logged in also to other jira-projects his account can also access. If
	 *         credentials do not match (for any project) an empty set must be
	 *         returned.
	 */
	Set<IProject> authenticate(String username, String password)
			throws SpecmateException;

}
