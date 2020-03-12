package com.specmate.connectors.api;

import java.util.Set;

import com.specmate.model.auth.AuthProject;

public interface IProjectService {

	IProject getProject(String projectName);
	Set<AuthProject> getProjects();

}
