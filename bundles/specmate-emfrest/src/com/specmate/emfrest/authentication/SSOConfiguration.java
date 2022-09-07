package com.specmate.emfrest.authentication;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.common.exception.SpecmateException;
import com.specmate.config.api.IConfigService;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.rest.RestResult;

@Component(service = IRestService.class)
public class SSOConfiguration extends RestServiceBase {
	private static final String SSOCONFIG_KEY_PREFIX = "ssoconfig.";
	public static final String SERVICE_NAME = "ssoconfig";
	public static final String AUTHORITY_URL_KEY = SERVICE_NAME + ".authority";
	public static final String CLIENT_ID_KEY = SERVICE_NAME + ".clientId";
	public static final String SCOPE_KEY = SERVICE_NAME + ".scope";
	public static final String RESPONSE_TYPE_KEY = SERVICE_NAME + ".responseType";
	
	public static final String[] SSO_KEYS = {AUTHORITY_URL_KEY, CLIENT_ID_KEY, RESPONSE_TYPE_KEY, SCOPE_KEY};
	
	private IConfigService configService;

	/** Reference to the log service */
	@Reference(service = LoggerFactory.class)
	private Logger logger;

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}
	@Override
	public boolean canGet(Object target) {
		return true;
	}

	@Override
	public RestResult<?> get(Object object, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {

		String answer = configService.getConfigurationProperties(SSOCONFIG_KEY_PREFIX).stream()
			.map(e -> new String[] {e.getKey().toString().replace(SSOCONFIG_KEY_PREFIX, ""), e.getValue().toString()})
			.map(t -> "\"" + t[0] + "\": \"" + t[1] + "\"")
			.collect(Collectors.joining(", "));

		return new RestResult<>(Response.Status.OK, "{" + answer + "}");
	}

	/** Service reference for config service */
	@Reference
	public void setConfigurationService(IConfigService configService) {
		this.configService = configService;
	}
}
