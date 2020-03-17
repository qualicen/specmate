package com.specmate.auth.api;

import java.net.URLEncoder;
import java.nio.charset.Charset;

import com.specmate.model.auth.AuthFactory;
import com.specmate.model.auth.IAuthProject;
import com.specmate.model.auth.OAuthProject;

/**
 * The data needed for authorizing a user. Supports different types
 * (OAuth/User-password). Use different constructors to construct different
 * types.
 * 
 * @author eders
 *
 */
public class AuthData {

	private String url;
	private String tokenUrl;
	private String clientId;
	private String clientSecret;
	private String redirectUri;

	private Boolean isAccessible;

	/**
	 * Constructor for user-login auth data (default)
	 */
	public AuthData() {
	}

	/**
	 * Constructor for a no-auth-project
	 * 
	 * @param accessible whether the project is accessible or not.
	 */
	public AuthData(boolean isAccessible) {
		this.isAccessible = isAccessible;
	}

	/**
	 * Constructor for OAuth-Data
	 * 
	 * @param url          the url used for getting the code (where the user has to
	 *                     login)
	 * @param tokenUrl     the url to retrieve the access token with the users's
	 *                     code
	 * @param clientId     the client id of the specmate data access app
	 * @param clientSecret the secret of the specmate data access app
	 * @param redirectUri  the redirection uri (to https://[SPECMATE]/-/oauth)
	 */
	public AuthData(String url, String tokenUrl, String clientId, String clientSecret, String redirectUri) {
		this.url = url;
		this.tokenUrl = tokenUrl;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.redirectUri = redirectUri;
		
		replaceUrlParts();
	}

	public IAuthProject getAuthProject(String projectName) {
		if (getAuthType() == EAuthType.OAUTH) {
			OAuthProject authProject = AuthFactory.eINSTANCE.createOAuthProject();
			authProject.setName(projectName);
			authProject.setOauthUrl(url);
			authProject.setOauthTokenUrl(tokenUrl);
			authProject.setOauthClientId(clientId);
			authProject.setOauthClientSecret(clientSecret);
			authProject.setOauthRedirectUrl(redirectUri);
			return authProject;
		}
		return AuthFactory.eINSTANCE.createUserPasswordAuthProject();
	}

	public EAuthType getAuthType() {
		if (isSet(url) && isSet(tokenUrl) && isSet(clientId) && isSet(clientSecret) && isSet(redirectUri)) {
			return EAuthType.OAUTH;
		} else if (this.isAccessible != null) {
			return EAuthType.NONE;
		}
		return EAuthType.USER_PASSWORD;
	}

	private boolean isSet(String str) {
		return str != null && !str.trim().isEmpty();
	}
	
	private void replaceUrlParts() {
		url = url.replace("${CLIENT_ID}", clientId);
		url = url.replace("${REDIRECT_URL}", URLEncoder.encode(redirectUri, Charset.defaultCharset()));
	}

	public String getUrl() {
		return url;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public Boolean getIsAccessible() {
		return isAccessible;
	}
	
	
}
