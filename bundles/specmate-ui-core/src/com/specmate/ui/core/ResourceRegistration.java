package com.specmate.ui.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.emfrest.api.IRestEndpoint;

@Component(immediate = true)
public class ResourceRegistration {

	private HttpService httpService;
	/** Reference to the log service */
	@Reference(service = LoggerFactory.class)
	private Logger logger;
	@Activate
	public void activate() {
		try {
			this.httpService.registerResources("/", "/webcontent", null);
			registerDocFolder();
		} catch (NamespaceException e) {
			this.logger.error("Could not register frontend with http service: ", e);
		}
	}

	private void registerDocFolder() throws NamespaceException {
		File docFolder = new File("./doc");
		if (docFolder.exists() && docFolder.isDirectory()) {
			this.httpService.registerResources("/doc", "/", new HttpContext() {

				@Override
				public URL getResource(String name) {
					try {
						return new URL(docFolder.toURI().toURL(), name.substring(1));
					} catch (MalformedURLException e) {
						return null;
					}
				}

				@Override
				public String getMimeType(String name) {
					return null;
				}

				@Override
				public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response)
						throws IOException {
					return true;
				}
			});
		}
	}

	@Reference
	public void setHttpService(HttpService http) {
		this.httpService = http;
	}
}
