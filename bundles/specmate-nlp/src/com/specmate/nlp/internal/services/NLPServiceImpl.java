package com.specmate.nlp.internal.services;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.config.api.IConfigService;
import com.specmate.model.administration.ErrorCode;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.internal.nlpcomponents.Spacy;
import com.specmate.nlp.util.NLPUtil;

import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

/**
 * Service to tag a text with the DKPro NLP-Framework
 *
 * @author Andreas Wehrle
 *
 */
@Component(immediate = true)
public class NLPServiceImpl implements INLPService {

	private static final String KEY_SPACY_URL = "nlp.spacy.url";
	private static final String KEY_SPACY_MODEL = "nlp.spacy.model";
	private Map<String, AnalysisEngine> engines = new HashMap<String, AnalysisEngine>();
	private LogService logService;
	private IConfigService configService;

	/**
	 * Start the NLP Engine
	 *
	 * @throws SpecmateException
	 */
	@Activate
	public void activate() throws SpecmateException {
		createGermanPipeline();
		createEnglishPipeline();

	}

	private void createEnglishPipeline() throws SpecmateInternalException {
		logService.log(LogService.LOG_INFO, "Initializing english NLP pipeline");

		String spacyUrl = configService.getConfigurationProperty(KEY_SPACY_URL);

		if (spacyUrl != null) {
			logService.log(LogService.LOG_INFO,
					"Spacy URL found. Configuring spacy pipeline with spacy at:" + spacyUrl);
			buildSpacyEngine(spacyUrl);
		} else {
			buildEngine();
		}
	}

	private void buildEngine() throws SpecmateInternalException {
		AnalysisEngineDescription segmenter = null;
		AnalysisEngineDescription posTagger = null;
		AnalysisEngineDescription parser = null;
		AnalysisEngineDescription chunker = null;
		AnalysisEngineDescription dependencyParser = null;

		String lang = ELanguage.EN.getLanguage();

		try {
			segmenter = createEngineDescription(OpenNlpSegmenter.class, OpenNlpSegmenter.PARAM_LANGUAGE, lang);
			posTagger = createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, lang,
					OpenNlpPosTagger.PARAM_VARIANT, "maxent");
			chunker = createEngineDescription(OpenNlpChunker.class, OpenNlpChunker.PARAM_LANGUAGE, lang);
			dependencyParser = createEngineDescription(MaltParser.class, MaltParser.PARAM_LANGUAGE, lang,
					MaltParser.PARAM_IGNORE_MISSING_FEATURES, true);
			parser = createEngineDescription(OpenNlpParser.class, OpenNlpParser.PARAM_PRINT_TAGSET, true,
					OpenNlpParser.PARAM_LANGUAGE, lang, OpenNlpParser.PARAM_WRITE_PENN_TREE, true,
					OpenNlpParser.PARAM_WRITE_POS, true);

			AnalysisEngine engine = createEngine(
					createEngineDescription(segmenter, posTagger, chunker, dependencyParser, parser));

			engines.put(lang, engine);
		} catch (Throwable e) {
			throw new SpecmateInternalException(ErrorCode.NLP,
					"OpenNLP NLP service failed when starting. Reason: " + e.getMessage());
		}
	}

	private void buildSpacyEngine(String spacyUrl) throws SpecmateInternalException {
		AnalysisEngineDescription parser = null;
		AnalysisEngineDescription chunker = null;
		AnalysisEngineDescription spacy = null;

		String model = configService.getConfigurationProperty(KEY_SPACY_MODEL, "en");
		String lang = ELanguage.EN.getLanguage();

		try {

			spacy = createEngineDescription(Spacy.class, Spacy.PARAM_MODEL, model, Spacy.PARAM_SPACY_URL, spacyUrl);
			chunker = createEngineDescription(OpenNlpChunker.class, OpenNlpChunker.PARAM_LANGUAGE, lang);
			parser = createEngineDescription(OpenNlpParser.class, OpenNlpParser.PARAM_PRINT_TAGSET, true,
					OpenNlpParser.PARAM_LANGUAGE, lang, OpenNlpParser.PARAM_WRITE_PENN_TREE, true,
					OpenNlpParser.PARAM_WRITE_POS, true);

			AnalysisEngine engine = createEngine(createEngineDescription(spacy, chunker, parser));

			engines.put(lang, engine);
		} catch (Throwable e) {
			throw new SpecmateInternalException(ErrorCode.NLP,
					"OpenNLP NLP service failed when starting. Reason: " + e.getMessage());
		}
	}

	private void createGermanPipeline() throws SpecmateInternalException {
		logService.log(LogService.LOG_INFO, "Initializing german NLP pipeline");
		AnalysisEngineDescription segmenter = null;
		AnalysisEngineDescription lemmatizer = null;
		AnalysisEngineDescription posTagger = null;
		AnalysisEngineDescription chunker = null;
		AnalysisEngineDescription parser = null;
		AnalysisEngineDescription dependencyParser = null;

		String lang = ELanguage.DE.getLanguage();

		try {
			segmenter = createEngineDescription(OpenNlpSegmenter.class, OpenNlpSegmenter.PARAM_LANGUAGE, lang);
			lemmatizer = createEngineDescription(OpenNlpLemmatizer.class, OpenNlpLemmatizer.PARAM_LANGUAGE, lang,
					OpenNlpLemmatizer.PARAM_MODEL_LOCATION, "classpath:/models/de-lemmatizer.bin");
			posTagger = createEngineDescription(OpenNlpPosTagger.class, OpenNlpPosTagger.PARAM_LANGUAGE, lang,
					OpenNlpPosTagger.PARAM_MODEL_LOCATION, "classpath:/models/de-pos.bin");
			chunker = createEngineDescription(OpenNlpChunker.class, OpenNlpParser.PARAM_PRINT_TAGSET, true,
					OpenNlpChunker.PARAM_LANGUAGE, lang, OpenNlpChunker.PARAM_MODEL_LOCATION,
					"classpath:/models/de-chunker.bin");
			dependencyParser = createEngineDescription(DependencyParser.class, MaltParser.PARAM_LANGUAGE, lang,
					MaltParser.PARAM_IGNORE_MISSING_FEATURES, true, MaltParser.PARAM_MODEL_LOCATION,
					"classpath:/models/de-dependencies.mco");
			parser = createEngineDescription(OpenNlpParser.class, OpenNlpParser.PARAM_PRINT_TAGSET, true,
					OpenNlpParser.PARAM_LANGUAGE, lang, OpenNlpParser.PARAM_WRITE_PENN_TREE, false,
					OpenNlpParser.PARAM_WRITE_POS, false, OpenNlpParser.PARAM_MODEL_LOCATION,
					"classpath:/models/de-parser-chunking.bin");

			AnalysisEngine engine = createEngine(
					createEngineDescription(segmenter, lemmatizer, posTagger, chunker, parser, dependencyParser));

			engines.put(lang, engine);
		} catch (Throwable e) {
			// logService.log(LogService.LOG_ERROR, "OpenNLP NLP service failed
			// when starting. Reason: " + e.getMessage());
			throw new SpecmateInternalException(ErrorCode.NLP,
					"OpenNLP NLP service failed when starting. Reason: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.specmate.testspecification.internal.services.NLPTagger#tagText(java.
	 * lang. String)
	 */
	@Override
	public synchronized JCas processText(String text, ELanguage language) throws SpecmateException {
		text = text.strip();
		AnalysisEngine engine = engines.get(language.getLanguage());
		if (engine == null) {
			throw new SpecmateInternalException(ErrorCode.NLP,
					"No analysis engine for language " + language.getLanguage() + " available.");
		}
		JCas jcas = null;
		try {
			jcas = JCasFactory.createJCas();
			jcas.setDocumentText(text);
			jcas.setDocumentLanguage(language.getLanguage());
			SimplePipeline.runPipeline(jcas, engine);
		} catch (Throwable e) {
			// Catch any kind of runtime or checked exception
			throw new SpecmateInternalException(ErrorCode.NLP, "NLP: Tagging failed. Reason: " + e.getMessage());
		}
		NLPUtil.refineNpChunks(jcas);
		return jcas;
	}

	@Override
	public ELanguage detectLanguage(String text) {
		// FIXME: detection is only placeholder for real language detection
		String lower = text.toLowerCase();
		if (lower.contains("wenn") || lower.contains("der") || lower.contains("die") || lower.contains("das")
				|| lower.contains("ein")) {
			return ELanguage.DE;
		} else {
			return ELanguage.EN;
		}
	}

	@Reference
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Reference
	public void setConfigurationService(IConfigService configService) {
		this.configService = configService;
	}

}
