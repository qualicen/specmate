package com.specmate.auth.api;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.usermodel.AccessRights;
import com.specmate.usermodel.UserSession;

/** Interface for a service managing user sessions */
public interface ISessionService {

	/** Create a new user session */
	public UserSession create(AccessRights exportRights, AccessRights readingRights, String userName, String password,
			String projectName) throws SpecmateException;

	/** Create empty session - only for testing */
	public UserSession create();

	/**
	 * Determine if a session is expired
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public boolean isExpired(String token) throws SpecmateException;

	/**
	 * Determine if a certain path may be accessed
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public boolean isAuthorizedPath(String token, String path) throws SpecmateException;

	/**
	 * Determine if a certain path may be accessed
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public boolean isAuthorizedPath(UserSession session, String path) throws SpecmateException;

	/**
	 * Determine if a certain project may be accessed
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	boolean isAuthorizedProject(String token, String project) throws SpecmateException;

	/**
	 * Determine if a certain project may be accessed
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	boolean isAuthorizedProject(UserSession session, String project) throws SpecmateException;

	/**
	 * Refresh a session
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public void refresh(String token) throws SpecmateException;

	/**
	 * Get user name for a session
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public String getUserName(String token) throws SpecmateException;

	/**
	 * Gets source access rights of a session
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public AccessRights getSourceAccessRights(String token) throws SpecmateException;

	/**
	 * Gets target access rights of a session
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public AccessRights getTargetAccessRights(String token) throws SpecmateException;

	/**
	 * Delets a session
	 *
	 * @throws SpecmateInternalException if there is no session for the token
	 */
	public void delete(String token) throws SpecmateException;

	/**
	 * Registers a session listener
	 */
	public void registerSessionListener(ISessionListener listener);

	/**
	 * Removes a session listener
	 */
	public void removeSessionListener(ISessionListener listener);

}