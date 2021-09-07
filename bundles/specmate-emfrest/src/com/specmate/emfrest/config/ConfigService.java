package com.specmate.emfrest.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.common.exception.SpecmateException;
import com.specmate.config.api.IConfigService;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.rest.RestResult;

@Component(immediate = true, service = IRestService.class)
public class ConfigService extends RestServiceBase {

	private static final String CONFIG_PREFIX = "uiconfig";

	/** Reference to the log service */
	@Reference(service = LoggerFactory.class)
	private Logger logger;
	private IConfigService configService;

	@Activate
	public void activate(Map<String, Object> properties) {
		this.logger.info("Initialized config service " + properties.toString());
	}

	@Override
	public String getServiceName() {
		return "config";
	}

	@Override
	public boolean canGet(Object target) {
		return true;
	}

	@Override
	public RestResult<?> get(Object target, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		Map<String, String> configValues = new HashMap<>();
		for (Entry<Object, Object> entry : configService.getConfigurationProperties(CONFIG_PREFIX)) {
			String key = entry.getKey().toString().replaceFirst(CONFIG_PREFIX + ".", "");
			String value = entry.getValue().toString();
			configValues.put(key, value);
		}
		return new RestResult<>(Response.Status.OK, configValues);
	}

	@Reference
	public void setConfigService(IConfigService configService) {
		this.configService = configService;
	}
}
