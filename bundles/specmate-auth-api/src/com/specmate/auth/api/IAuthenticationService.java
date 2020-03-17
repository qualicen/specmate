package com.specmate.auth.api;

import com.specmate.common.exception.SpecmateAuthorizationException;
import com.specmate.common.exception.SpecmateException;
import com.specmate.usermodel.AccessRights;
import com.specmate.usermodel.UserSession;

public interface IAuthenticationService {
	public UserSession authenticate(String username, String password, String projectname) throws SpecmateException;

	public UserSession authenticate(String username, String password);
	
	public UserSession authenticateOAuth(String sessionId, String code, String projectName) throws SpecmateAuthorizationException;

	public void deauthenticate(String token) throws SpecmateException;

	public void validateToken(String token, String path, boolean refresh) throws SpecmateException;

	public String getUserName(String token) throws SpecmateException;

	public AccessRights getSourceAccessRights(String token) throws SpecmateException;

	public AccessRights getTargetAccessRights(String token) throws SpecmateException;
}
