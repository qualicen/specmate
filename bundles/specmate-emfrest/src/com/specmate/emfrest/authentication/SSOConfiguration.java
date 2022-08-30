package com.specmate.emfrest.authentication;

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
	public static final String SERVICE_NAME = "ssoconfig";
	public static final String AUTHORITY_URL_KEY = SERVICE_NAME + ".authority";
	public static final String CLIENT_ID_KEY = SERVICE_NAME + ".clientid";
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

		String authority = configService.getConfigurationProperty(AUTHORITY_URL_KEY);
		String clientId = configService.getConfigurationProperty(CLIENT_ID_KEY);
		
		logger.info(">>>>>>> Return SSO config. >>>>>>>>");

		return new RestResult<>(Response.Status.OK, "{\"authority\": \"" + authority + "\", \"clientId\": \"" + clientId + "\"}");
	}

	/** Service reference for config service */
	@Reference
	public void setConfigurationService(IConfigService configService) {
		this.configService = configService;
	}
}
