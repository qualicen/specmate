package com.specmate.modelgeneration;

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
import com.specmate.config.api.IConfigService;
import com.specmate.emfrest.api.IRestService;
import com.specmate.emfrest.api.RestServiceBase;
import com.specmate.metrics.ICounter;
import com.specmate.metrics.IMetricsService;
import com.specmate.model.requirements.CEGModel;
import com.specmate.modelgeneration.legacy.EnglishCEGFromRequirementGenerator;
import com.specmate.modelgeneration.legacy.GermanCEGFromRequirementGenerator;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.util.NLPUtil;
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
	private IConfigService configService;
	private IMetricsService metricsService;
	private ICounter modelGenCounter;

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
			model = generateModelFromDescription(model);
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
	private CEGModel generateModelFromDescription(CEGModel model) throws SpecmateException {
		String text = model.getModelRequirements();
		if (text == null || StringUtils.isEmpty(text)) {
			return model;
		}
		// Fixes some issues with the dkpro/spacy backoff.
		text = text.replaceAll("[^,.!? ](?=[,.!?])", "$0 ").replaceAll("\\s+", " ");
		// text = new PersonalPronounsReplacer(tagger).replacePronouns(text);
		ELanguage lang = NLPUtil.detectLanguage(text);
		ICEGFromRequirementGenerator generator;
		if (lang == ELanguage.PSEUDO) {
			generator = new GenerateModelFromPseudoCode();
		} else {
			generator = new PatternbasedCEGGenerator(lang, tagger, configService, logger);
		}

		try {
			generator.createModel(model, text);
		} catch (SpecmateException e) {
			// Generation Backof
			logger.info("NLP model generation failed with the following error: \"" + e.getMessage() + "\"");
			logger.info("Backing off to rule based generation...");
			if (lang == ELanguage.DE) {
				generator = new GermanCEGFromRequirementGenerator(logger, tagger);
			} else {
				generator = new EnglishCEGFromRequirementGenerator(logger, tagger);
			}
			generator.createModel(model, text);
		}
		return model;
	}

	@Reference
	void setNlptagging(INLPService tagger) {
		this.tagger = tagger;
	}

	/** Service reference for config service */
	@Reference
	public void setConfigurationService(IConfigService configService) {
		this.configService = configService;
	}

	@Reference
	public void setMetricsService(IMetricsService metricsService) {
		this.metricsService = metricsService;
	}
}
