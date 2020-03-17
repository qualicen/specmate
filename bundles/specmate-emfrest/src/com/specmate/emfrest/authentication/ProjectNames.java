package com.specmate.emfrest.authentication;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.emf.ecore.resource.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.connectors.api.IProjectService;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.model.auth.IAuthProject;
import com.specmate.model.auth.OAuthProject;
import com.specmate.rest.RestResult;

@Component(service = IRestService.class)
public class ProjectNames extends RestServiceBase {
	private static final String SESSION_ID_PLACEHOLDER = "${SESSION_ID}";
	public static final String SERVICE_NAME = "projectnames";
	private IProjectService projectService;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public boolean canGet(Object target) {
		return (target instanceof Resource);
	}

	@Override
	public RestResult<?> get(Object object, MultivaluedMap<String, String> queryParams, String token, String sessionId) {
		Set<IAuthProject> projects = projectService.getProjects().stream().map(project -> {
			IAuthProject authProject = project.getAuthProject();
			if(authProject instanceof OAuthProject) {
				OAuthProject oauthProject = (OAuthProject) authProject;
				String oauthUrl = oauthProject.getOauthUrl().replace(SESSION_ID_PLACEHOLDER, authProject.getName() + '|' + sessionId);
				oauthProject.setOauthUrl(oauthUrl);
			}
			return (IAuthProject) authProject;
		}).collect(Collectors.toSet());
		return new RestResult<>(Response.Status.OK, new ArrayList<>(projects));
	}

	@Reference
	public void setProjectService(IProjectService projectService) {
		this.projectService = projectService;
	}
}
