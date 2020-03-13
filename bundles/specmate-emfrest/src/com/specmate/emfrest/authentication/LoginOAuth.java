package com.specmate.emfrest.authentication;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.specmate.auth.api.IAuthenticationService;
import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.rest.RestResult;
import com.specmate.usermodel.User;
import com.specmate.usermodel.UserSession;

@Component(service = IRestService.class)
public class LoginOAuth extends RestServiceBase {
	public static final String SERVICE_NAME = "oauth";
	

	private IAuthenticationService authService;
	private LogService logService;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public boolean canGet(Object object) {
		return true;
	}
	
	@Override
	public boolean canPost(Object object2, Object object) {
		return object instanceof User;
	}

	@Override
	public RestResult<?> get(Object object, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		// TODO Auto-generated method stub
		return super.get(object, queryParams, token);
	}
	
	@Override
	public RestResult<?> post(Object object, Object object2, String token) throws SpecmateException {
		User user = (User) object2;

		UserSession session = authService.authenticate(user.getUserName(), user.getPassWord(), user.getProjectName());
		logService.log(LogService.LOG_INFO,
				"Session " + session.getId() + " for user " + user.getUserName() + " created.");
		return new RestResult<>(Response.Status.OK, session);
	}

	@Reference
	public void setAuthService(IAuthenticationService authService) {
		this.authService = authService;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}
}