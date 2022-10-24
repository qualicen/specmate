package com.specmate.modelgeneration.internal;

import java.net.URISyntaxException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.metrics.ICounter;
import com.specmate.metrics.IMetricsService;
import com.specmate.model.requirements.CEGModel;
import com.specmate.modelgeneration.api.ICEGModelGenerator;
import com.specmate.nlp.api.INLPService;
import com.specmate.rest.RestResult;
import com.specmate.xtext.XTextException;

/**
 * Service to create automatic a CEGModel from a requirement
 *
 * @author Andreas Wehrle
 *
 */
@Component(immediate = true, service = IRestService.class)
public class GenerateModelFromRequirementService extends RestServiceBase {

	INLPService tagger;
	/** Reference to the log service */
	@Reference(service = LoggerFactory.class)
	private Logger logger;
	private IMetricsService metricsService;
	private ICounter modelGenCounter;
	private ICEGModelGenerator modelGenerator;

	@Activate
	public void activate() throws SpecmateException {
		modelGenCounter = metricsService.createCounter("model_generation_counter", "Total number of generated models");
	}

	@Override
	public String getServiceName() {
		return "generateModel";
	}

	@Override
	public boolean canPost(Object object2, Object object) {
		return object2 instanceof CEGModel;
	}

	@Override
	public RestResult<?> post(Object parent, Object child, MultivaluedMap<String, String> queryParams, String token) {
		CEGModel model = (CEGModel) parent;

		try {
			logger.info("Model Generation STARTED");
			generateModelFromDescription(model);
			logger.info("Model Generation FINISHED");
			modelGenCounter.inc();
		} catch (SpecmateException e) {
			logger.error("Model Generation failed with following error:\n" + e.getMessage());
			return new RestResult<>(Response.Status.INTERNAL_SERVER_ERROR);
		}
		return new RestResult<>(Response.Status.OK);
	}

	/**
	 * Add the nodes and connections to the model extracted from the text
	 *
	 * @param model CEGModel
	 * @return
	 * @throws XTextException
	 * @throws URISyntaxException
	 */
	private void generateModelFromDescription(CEGModel model) throws SpecmateException {
		String text = model.getModelRequirements();
		if (text == null || StringUtils.isEmpty(text)) {
			return;
		}

		modelGenerator.createModel(model);

	}

	@Reference
	void setModelGenerator(ICEGModelGenerator modelGenerator) {
		this.modelGenerator = modelGenerator;
	}

	@Reference
	void setNlptagging(INLPService tagger) {
		this.tagger = tagger;
	}

	@Reference
	public void setMetricsService(IMetricsService metricsService) {
		this.metricsService = metricsService;
	}
}
