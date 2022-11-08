package com.specmate.modelgeneration.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.common.exception.SpecmateException;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.metrics.ICounter;
import com.specmate.metrics.IMetricsService;
import com.specmate.model.requirements.CEGModel;
import com.specmate.modelgeneration.api.ICEGModelGenerator;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.util.NLPUtil;
import com.specmate.rest.RestResult;

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
	private Map<String, List<ServiceReference<ICEGModelGenerator>>> modelGenerators = new HashMap<>();

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
	 */
	private void generateModelFromDescription(CEGModel model) throws SpecmateException {
		String text = model.getModelRequirements();
		if (text == null || StringUtils.isEmpty(text)) {
			return;
		}
		ELanguage language = NLPUtil.detectLanguage(text);
		List<ServiceReference<ICEGModelGenerator>> langGeneratorRefs = modelGenerators.get(language.getLanguage());
		BundleContext context = FrameworkUtil.getBundle(GenerateModelFromRequirementService.class).getBundleContext();
		for (ServiceReference<ICEGModelGenerator> modelGeneratorRef : langGeneratorRefs) {
			ICEGModelGenerator modelGenerator = context.getService(modelGeneratorRef);
			if (modelGenerator != null) {
				try {
					boolean success = modelGenerator.createModel(model);
					if (success) {
						break;
					}
				} catch (Exception e) {
				}
			}
		}
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addModelGenerator(ServiceReference<ICEGModelGenerator> modelGeneratorRef) {
		List<String> languages;
		Object languageProps = modelGeneratorRef.getProperty("languages");
		if (languageProps instanceof String[]) {
			languages = Arrays.asList((String) languageProps);
		} else {
			languages = Arrays.asList((String) languageProps);
		}

		for (String lang : languages) {
			List<ServiceReference<ICEGModelGenerator>> langGenerators = modelGenerators.get(lang);
			if (langGenerators == null) {
				langGenerators = new ArrayList<>();
				modelGenerators.put(lang, langGenerators);
			}
			langGenerators.add(modelGeneratorRef);

			// ServiceReferences compare against each other based on the service.ranking
			// property
			Collections.sort(langGenerators, Collections.reverseOrder());
		}
	}

	void removeModelGenerator(ServiceReference<ICEGModelGenerator> modelGeneratorRef) {
		// not implemented - model generator should never be removed
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
