package com.specmate.auth.internal;

import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.specmate.auth.api.IAuthenticationService;
import com.specmate.auth.api.IGlobalUserService;
import com.specmate.auth.api.ISessionService;
import com.specmate.common.exception.SpecmateAuthorizationException;
import com.specmate.common.exception.SpecmateException;
import com.specmate.connectors.api.IProject;
import com.specmate.connectors.api.IProjectService;
import com.specmate.export.api.IExporter;
import com.specmate.usermodel.AccessRights;
import com.specmate.usermodel.UserSession;

/**
 * Authentication design based on this implementation:
 * https://stackoverflow.com/a/26778123
 */
@Component(immediate = true, service = IAuthenticationService.class)
public class AuthenticationServiceImpl implements IAuthenticationService {

	@Reference
	private ISessionService sessionService;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private IProjectService projectService;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	private IGlobalUserService globalUserService;

	@Override
	public UserSession authenticate(String username, String password, String projectname) throws SpecmateException {
		IProject project = projectService.getProject(projectname);
		if (project == null) {
			throw new SpecmateAuthorizationException("Project " + projectname + " does not exist.");
		}

		// Check if this is a global user
		if (globalUserService != null) {
			if (globalUserService.authenticate(username, password)) {
				Set<String> allProjectIds = projectService.getProjectIds();
				return sessionService.create(AccessRights.ALL, AccessRights.ALL, username, null, allProjectIds);

			}
		}

		if (project.getConnector() != null) {
			// No global user, so check with all projects if a project user
			Set<IProject> authenticatedProjects = project.getConnector().authenticate(username, password);

			if (!authenticatedProjects.contains(project)) {
				throw new SpecmateAuthorizationException("User " + username + " not authenticated.");
			}

			// TODO Access rights (source and target) are not handled yet!!!!
			// This will be removed in future anyways.
			AccessRights targetRights = retrieveTargetAccessRights(project, username, password);

			try {

				Set<String> authenticatedProjectNames = authenticatedProjects.stream().map(p -> p.getID())
						.collect(Collectors.toSet());
				return sessionService.create(AccessRights.ALL, targetRights, username, password,
						authenticatedProjectNames);

			} catch (SpecmateException e) {
				// Something went wrong when creating the session. We act as if the
				// Authorization failed
				throw new SpecmateAuthorizationException("Could not create a user session. Reason: " + e.getMessage(),
						e);
			}
		}
		throw new SpecmateAuthorizationException("User " + username + " not authenticated.");
	}

	/**
	 * Use this method only in tests to create a session that authorizes requests to
	 * all resources.
	 */
	@Override
	public UserSession authenticate(String username, String password) {
		return sessionService.create();
	}

	@Override
	public void deauthenticate(String token) throws SpecmateException {
		sessionService.delete(token);
	}

	@Override
	public void validateToken(String token, String path, boolean refresh) throws SpecmateException {
		if (sessionService.isExpired(token)) {
			sessionService.delete(token);
			throw new SpecmateAuthorizationException("Session " + token + " is expired.");
		}

		if (!sessionService.isAuthorizedPath(token, path)) {
			throw new SpecmateAuthorizationException("Session " + token + " not authorized for " + path + ".");
		}

		if (refresh) {
			sessionService.refresh(token);
		}
	}

	@Override
	public String getUserName(String token) throws SpecmateException {
		return sessionService.getUserName(token);
	}

	@Override
	public AccessRights getSourceAccessRights(String token) throws SpecmateException {
		return sessionService.getSourceAccessRights(token);
	}

	@Override
	public AccessRights getTargetAccessRights(String token) throws SpecmateException {
		return sessionService.getTargetAccessRights(token);
	}

	private AccessRights retrieveTargetAccessRights(IProject project, String username, String password) {
		IExporter exporter = project.getExporter();
		if (exporter == null) {
			return AccessRights.NONE;
		}

		if (exporter.isAuthorizedToExport(username, password)) {
			return AccessRights.WRITE;
		} else {
			return AccessRights.NONE;
		}
	}
}
