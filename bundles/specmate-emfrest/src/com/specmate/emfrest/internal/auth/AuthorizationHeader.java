package com.specmate.emfrest.internal.auth;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

public class AuthorizationHeader {
	public static final String REALM = "specmate";
	public static final String AUTHENTICATION_SCHEME = "Token";

	private static final String SPECMATE_AUTH_COOKIE_BASE = "specmate-auth-";
	private static final String SPECMATE_AUTH_TOKEN_COOKIE_NAME = SPECMATE_AUTH_COOKIE_BASE + "token";
	private static final String SPECMATE_AUTH_PROJECT_COOKIE_NAME = SPECMATE_AUTH_COOKIE_BASE + "project";

	private static String getAuthHeader(ContainerRequestContext requestContext) {
		return requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
	}

	public static boolean isAuthenticationSet(ContainerRequestContext requestContext) {
		return isTokenAuth(requestContext) || isCookieAuth(requestContext);
	}

	private static javax.servlet.http.Cookie getCookie(HttpServletRequest request) {
		javax.servlet.http.Cookie[] cookies = request.getCookies();
		String domain = getDomain(request);
		if (cookies != null) {
			return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(getTokenCookieName(domain)))
					.findAny().orElse(null);
		}
		return null;
	}

	private static Cookie getAuthCookie(ContainerRequestContext requestContext) {
		String domain = getDomain(requestContext);
		return requestContext.getCookies().get(getTokenCookieName(domain));
	}

	private static boolean isTokenAuth(ContainerRequestContext requestContext) {
		return isTokenAuthString(getAuthHeader(requestContext));
	}

	private static boolean isTokenAuthString(String authHeaderValue) {
		if (authHeaderValue == null) {
			return false;
		}
		return authHeaderValue.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
	}

	private static boolean isCookieAuth(ContainerRequestContext requestContext) {
		return getAuthCookie(requestContext) != null;
	}

	private static String extractTokenFrom(String authHeaderValue) {
		if (isTokenAuthString(authHeaderValue)) {
			return authHeaderValue.substring(AUTHENTICATION_SCHEME.length()).trim();
		}
		return null;
	}

	public static String getToken(ContainerRequestContext requestContext) {
		if (isCookieAuth(requestContext)) {
			return getAuthCookie(requestContext).getValue();
		} else if (isTokenAuth(requestContext)) {
			return extractTokenFrom(getAuthHeader(requestContext));
		}
		return null;
	}

	public static String getToken(HttpServletRequest request) {
		javax.servlet.http.Cookie authCookie = getCookie(request);
		if (authCookie != null) {
			return authCookie.getValue();
		}
		String authHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (isTokenAuthString(authHeaderValue)) {
			return extractTokenFrom(authHeaderValue);
		}
		return null;
	}

	public static String getTokenCookieName(String domain) {
		return getCookieName(SPECMATE_AUTH_TOKEN_COOKIE_NAME, domain);
	}

	public static String getProjectCookieName(String domain) {
		return getCookieName(SPECMATE_AUTH_PROJECT_COOKIE_NAME, domain);
	}

	private static String getCookieName(String prefix, String domain) {
		return prefix + "-" + domain;
	}

	private static String getDomain(ContainerRequestContext requestContext) {
		return requestContext.getUriInfo().getRequestUri().getHost();
	}

	private static String getDomain(HttpServletRequest request) {
		return request.getServerName();
	}
}
