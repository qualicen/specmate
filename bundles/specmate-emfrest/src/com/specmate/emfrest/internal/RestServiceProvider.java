package com.specmate.emfrest.internal;

import java.util.SortedSet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.google.common.collect.TreeMultimap;
import com.specmate.emfrest.api.IRestService;

@Component(immediate = true, service = RestServiceProvider.class)
public class RestServiceProvider {
	TreeMultimap<String, IRestService> restServices = TreeMultimap.create();
	/** Reference to the log service */
	@Reference(service = LoggerFactory.class)
	private Logger logger;

	public void activate() {
		this.logger.debug("Activating RestServiceProvider.");
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addRestService(IRestService restService) {
		restServices.put(restService.getServiceName(), restService);
	}

	public void removeRestService(IRestService restService) {
		restServices.remove(restService.getServiceName(), restService);
	}

	public IRestService getRestService(String name) {
		if (restServices.containsKey(name)) {
			return restServices.get(name).first();
		} else {
			return null;
		}
	}

	public SortedSet<IRestService> getAllRestServices(String name) {
		return restServices.get(name);
	}
}
