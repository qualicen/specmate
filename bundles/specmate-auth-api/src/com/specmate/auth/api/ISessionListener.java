package com.specmate.auth.api;

import com.specmate.usermodel.UserSession;

public interface ISessionListener {
	public void sessionCreated(UserSession session, String userName, String password);

	public void sessionDeleted(UserSession session);
}
