package com.specmate.modelgeneration.internal.cira;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.config.api.IConfigService;
import com.specmate.model.administration.ErrorCode;
import com.specmate.model.requirements.CEGModel;
import com.specmate.modelgeneration.api.ICEGModelGenerator;
import com.specmate.modelgeneration.internal.config.CiraConfig;

@Component(immediate = true)
public class CiraBasedModelGenerator implements ICEGModelGenerator {

	public static final String PID = "com.specmate.modelgeneration.CiraBasedModelGenerator";

	CiraClient ciraClient;
	private Logger logger;
	private LoggerFactory loggerFactory;

	private IConfigService configService;

	public void activate() throws SpecmateException {
		String ciraUrl = configService.getConfigurationProperty(CiraConfig.CIRA_URL_KEY);

		if (StringUtils.isEmpty(ciraUrl)) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, "Cira Host is not specified.");
		}

		try {
			new URL(ciraUrl);
		} catch (MalformedURLException mue) {
			throw new SpecmateInternalException(ErrorCode.CONFIGURATION, "Cira URL is not a valid URL.");
		}

		ciraClient = new CiraClient(ciraUrl, loggerFactory);
	}

	@Override
	public void createModel(CEGModel model) throws SpecmateException {
		String text = model.getModelRequirements();
//		boolean causal = ciraClient.isCausal(text);
//		if (causal) {
		List<CiraLabel> labels = ciraClient.getLabels(text);
		CiraLabelToCEGTranslator translator = new CiraLabelToCEGTranslator();
		translator.transform(model, text, labels);
//		} else {
		// TODO: Can we get feedback to the user?
//		}

	}

	@Reference
	public void setLoggerFactory(LoggerFactory loggerFactory) {
		this.loggerFactory = loggerFactory;
		logger = loggerFactory.getLogger(CiraBasedModelGenerator.class);
	}

	@Reference
	public void setConfigService(IConfigService configService) {
		this.configService = configService;
	}

}
