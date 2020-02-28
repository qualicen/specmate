package com.specmate.auth.api;

import com.specmate.usermodel.UserSession;

/**
 * Interface for listening to session events, such as session creation and
 * delection
 */
public interface ISessionListener {

	/** Called when a new session is created */
	public void sessionCreated(UserSession session, String userName, String password);

	/** Called when a session is deleted */
	public void sessionDeleted(UserSession session);
}
