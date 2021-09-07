package com.specmate.emfrest.authentication;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.auth.api.IAuthenticationService;
import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.usermodel.User;
import com.specmate.usermodel.UserSession;

@Component(service = IRestService.class)
public class Login extends AuthRestServiceBase {
	public static final String SERVICE_NAME = "login";

	private IAuthenticationService authService;

	/** Reference to the log service */
	@Reference(service = LoggerFactory.class)
	private Logger logger;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public boolean canPost(Object object2, Object object) {
		return object instanceof User;
	}

	@Reference
	public void setAuthService(IAuthenticationService authService) {
		this.authService = authService;
	}

	@Override
	protected UserSessionAndUser getUserSessionAndUser(Object parent, Object child, String token)
			throws SpecmateException {
		User user = (User) child;

		UserSession session = authService.authenticate(user.getUserName(), user.getPassWord(), user.getProjectName());
		logger.info("Session " + session.getId() + " for user " + user.getUserName() + " created.");
		return new UserSessionAndUser(user, session);
	}
}