package com.specmate.emfrest.config;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.config.api.IConfigService;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.rest.RestResult;

@Component(immediate = true, service = IRestService.class)
public class ConfigService extends RestServiceBase {

	private static final String[] CONFIG_KEYS = { "uiconfig.enableProjectExplorer" };

	private LogService logService;
	private IConfigService configService;

	@Activate
	public void activate(Map<String, Object> properties) {
		this.logService.log(LogService.LOG_INFO, "Initialized config service " + properties.toString());
	}

	@Override
	public String getServiceName() {
		return "config";
	}

	@Override
	public boolean canGet(Object target) {
		return (target instanceof EObject) && SpecmateEcoreUtil.isProject((EObject) target);
	}

	@Override
	public RestResult<?> get(Object target, MultivaluedMap<String, String> queryParams, String token)
			throws SpecmateException {
		Map<String, String> configValues = new HashMap<>();
		for (String key : CONFIG_KEYS) {
			configValues.put(key, configService.getConfigurationProperty(key));
		}
		return new RestResult<>(Response.Status.OK, configValues);
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Reference
	public void setConfigService(IConfigService configService) {
		this.configService = configService;
	}
}
