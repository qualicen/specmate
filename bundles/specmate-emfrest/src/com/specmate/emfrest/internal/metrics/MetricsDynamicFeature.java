package com.specmate.emfrest.internal.metrics;

import javax.inject.Inject;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.osgi.service.log.Logger;

import com.specmate.common.exception.SpecmateException;
import com.specmate.metrics.IMetricsService;

@Provider
public class MetricsDynamicFeature implements DynamicFeature {

	@Inject
	IMetricsService metricsService;
	@Inject
	Logger logger;

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		AMetric annotation = resourceInfo.getResourceClass().getAnnotation(AMetric.class);

		try {
			context.register(new MetricsFilter(metricsService, logger, resourceInfo, annotation));
		} catch (SpecmateException e) {
			logger.error("Could not register metrics filter.", e);
		}
	}
}