package com.specmate.auth.api;

/** Service to provide user authentication independent from projects */
public interface IGlobalUserService {

	/** Authenticates a global user **/
	boolean authenticate(String username, String password);

}
