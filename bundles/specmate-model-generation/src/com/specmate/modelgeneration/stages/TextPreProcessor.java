package com.specmate.modelgeneration.stages;

import java.util.List;

import com.specmate.common.exception.SpecmateException;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.util.EnglishSentenceUnfolder;
import com.specmate.nlp.util.GermanSentenceUnfolder;
import com.specmate.nlp.util.SentenceUnfolderBase;

public class TextPreProcessor {
	private final ELanguage language;
	private final INLPService nlpService;

	public TextPreProcessor(ELanguage language, INLPService nlpService) {
		this.language = language;
		this.nlpService = nlpService;
	}

	public List<String> preProcess(String text) throws SpecmateException {
		SentenceUnfolderBase unfolder;
		if (language == ELanguage.DE) {
			unfolder = new GermanSentenceUnfolder(nlpService);
		} else {
			unfolder = new EnglishSentenceUnfolder(nlpService);
		}
		text = generalProcessing(text);
		return unfolder.unfold(text);
	}

	private String generalProcessing(String text) {
		// Add Space before punctuation.
		return text.replaceAll("[^,.!? ](?=[,.!?])", "$0 ").replaceAll("\\s+", " ");
	}
}
