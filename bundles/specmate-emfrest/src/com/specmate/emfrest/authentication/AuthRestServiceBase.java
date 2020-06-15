package com.specmate.emfrest.authentication;

import java.util.Arrays;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.emfrest.internal.auth.AuthorizationHeader;
import com.specmate.rest.RestResult;
import com.specmate.usermodel.User;
import com.specmate.usermodel.UserSession;

/**
 * Base class for authenticating users. It adds authentication cookies to
 * responses. Use this base class for services that handle login requests.
 */
public abstract class AuthRestServiceBase extends RestServiceBase implements IResponseAlteringService {

	@Override
	public RestResult<?> post(Object parent, Object child, String token) throws SpecmateException {
		UserSessionAndUser userSessionAndUser = getUserSessionAndUser(parent, child, token);
		return new RestResult<>(Response.Status.OK, userSessionAndUser);
	}

	@Override
	public Response getResponse(HttpServletRequest request, RestResult<?> result) {
		UserSessionAndUser payload = (UserSessionAndUser) result.getPayload();
		RestResult<UserSession> alteredResult = new RestResult<UserSession>(Response.Status.OK, payload.getSession());

		NewCookie[] cookies = { getTokenCookie(request, result), getProjectCookie(request, result) };

		ResponseBuilder responseBuilder = Response.fromResponse(alteredResult.getResponse());

		Arrays.stream(cookies).filter(Objects::nonNull).forEach(cookie -> responseBuilder.cookie(cookie));
		return responseBuilder.build();
	}

	/**
	 * This method should return the User and Session object in case the login
	 * attempt is valid and throw an exception otherwise.
	 */
	protected abstract UserSessionAndUser getUserSessionAndUser(Object parent, Object child, String token)
			throws SpecmateException;

	private NewCookie constructCookie(String name, String value, HttpServletRequest request) {
		String path = "/";
		String domain = request.getServerName();
		String comment = "Specmate Auth Cookie (" + name + ")";
		int maxAge = 60 * 60 * 24 * 30;
		boolean secure = false;
		return new NewCookie(name, value, path, domain, comment, maxAge, secure);
	}

	private NewCookie getTokenCookie(HttpServletRequest request, RestResult<?> result) {
		if (result.getPayload() instanceof UserSessionAndUser) {
			UserSession userSession = ((UserSessionAndUser) result.getPayload()).getSession();
			return constructCookie(AuthorizationHeader.getTokenCookieName(request.getServerName()), userSession.getId(),
					request);
		}
		return null;
	}

	private NewCookie getProjectCookie(HttpServletRequest request, RestResult<?> result) {
		if (result.getPayload() instanceof UserSessionAndUser) {
			User user = ((UserSessionAndUser) result.getPayload()).getUser();
			return constructCookie(AuthorizationHeader.getProjectCookieName(request.getServerName()),
					user.getProjectName(), request);
		}
		return null;
	}
}
