package com.specmate.auth.internal;

import org.json.JSONObject;
import org.osgi.service.log.LogService;

import com.specmate.auth.api.AuthData;
import com.specmate.auth.api.EAuthType;
import com.specmate.rest.RestClient;
import com.specmate.rest.RestResult;

public class OAuthCodeAuthenticator {
	
	private LogService logService;
	private AuthData authData;
	
	public OAuthCodeAuthenticator(AuthData oauthData, LogService logService) {
		if(oauthData.getAuthType() != EAuthType.OAUTH) {
			throw new IllegalArgumentException("Can only handle OAuth-Data");
		}
		this.authData = oauthData;
		this.logService = logService;
	}
	
	public boolean authenticateCode(String code) {
		String url = authData.getTokenUrl();
		RestClient client = new RestClient(url, 0, logService);
		JSONObject payload = new JSONObject();
		payload.put("grant_type", "authorization_code");
		payload.put("client_id", authData.getClientId());
		payload.put("client_secret", authData.getClientSecret());
		payload.put("code", code);
		payload.put("redirect_uri", authData.getRedirectUri());
		RestResult<JSONObject> result = client.post("", payload);
		logService.log(LogService.LOG_INFO, payload.toString());
		logService.log(LogService.LOG_INFO, result.getPayload().toString());
		return result.getPayload().has("access_token");
	}
}
