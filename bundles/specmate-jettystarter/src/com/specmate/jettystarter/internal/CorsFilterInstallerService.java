package com.specmate.jettystarter.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Filter;

import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

/** Service to install a cross-origin-filter in the http service */
@Component(immediate = true)
public class CorsFilterInstallerService {

	/** The http service */
	private ExtendedHttpService httpService;

	/** Logging service */
	private LogService logService;

	@Activate
	private void activate() {
		Dictionary<String, String> params = new Hashtable<>();
		params.put(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
				"Authorization,Accept,Origin,DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Content-Range,Range");
		params.put(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,OPTIONS,PUT,DELETE");
		Filter corsFilter = new CrossOriginFilter();
		try {
			httpService.registerFilter("/", corsFilter, params, null);
		} catch (Exception e) {
			logService.log(LogService.LOG_ERROR, "Could not install CORS-Filter");
		}
	}

	@Reference
	public void setHttpService(HttpService service) {
		httpService = (ExtendedHttpService) service;
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}
}
