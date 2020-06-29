package com.specmate.emfrest.authentication;

import com.specmate.usermodel.User;
import com.specmate.usermodel.UserSession;

public class UserSessionAndUser {
	private User user;
	private UserSession session;

	public UserSessionAndUser(User user, UserSession session) {
		super();
		this.user = user;
		this.session = session;
	}

	public User getUser() {
		return user;
	}

	public UserSession getSession() {
		return session;
	}

}
