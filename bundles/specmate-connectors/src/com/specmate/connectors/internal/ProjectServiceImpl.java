package com.specmate.connectors.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IProjectService;
import com.specmate.model.auth.IAuthProject;

@Component
public class ProjectServiceImpl implements IProjectService {

	Map<String, IProject> projects = new HashMap<>();

	@Override
	public IProject getProject(String projectId) {
		return projects.get(projectId);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addProject(IProject project) {
		this.projects.put(project.getID(), project);
	}

	public void removeProject(IProject project) {
		this.projects.remove(project.getID());
	}

	@Override
	public Set<IProject> getProjects() {
		return new HashSet<IProject>(projects.values());
	}

}
