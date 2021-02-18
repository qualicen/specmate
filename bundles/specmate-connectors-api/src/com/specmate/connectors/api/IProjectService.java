package com.specmate.connectors.api;

import java.util.Map;
import java.util.Set;

public interface IProjectService {

	IProject getProject(String projectName);
	Set<String> getProjectNames();
	Map<String, IProject> getProjects();
}
