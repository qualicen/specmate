package com.specmate.connectors.api;

import java.util.Collection;

import com.specmate.common.exception.SpecmateException;
import com.specmate.model.base.IContainer;
import com.specmate.model.requirements.Requirement;

public interface IRequirementsSource {

	Collection<Requirement> getRequirements() throws SpecmateException;

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

	boolean authenticate(String username, String password) throws SpecmateException;

}
