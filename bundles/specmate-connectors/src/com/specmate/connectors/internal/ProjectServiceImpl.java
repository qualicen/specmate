package com.specmate.connectors.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IProjectService;
import com.specmate.model.auth.AuthProject;

@Component
public class ProjectServiceImpl implements IProjectService {

	Map<String, IProject> projects = new HashMap<>();

	@Override
	public IProject getProject(String projectId) {
		return projects.get(projectId);
	}

	@Override
	public Set<AuthProject> getProjects() {
		return Collections.unmodifiableSet(projects.values().stream().map(p -> p.getAuthProject()).collect(Collectors.toSet()));
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addProject(IProject project) {
		this.projects.put(project.getID(), project);
	}

	public void removeProject(IProject project) {
		this.projects.remove(project.getID());
	}

}
