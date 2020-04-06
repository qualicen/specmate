package com.specmate.modelgeneration.stages;

import com.specmate.common.exception.SpecmateException;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.util.EnglishSentenceUnfolder;
import com.specmate.nlp.util.GermanSentenceUnfolder;
import com.specmate.nlp.util.SentenceUnfolderBase;

public class TextPreProcessor {
	private final ELanguage lang;
	private final INLPService tagger;

	public TextPreProcessor(ELanguage language, INLPService nlpService) {
		lang = language;
		tagger = nlpService;
	}


	public String preProcess(String text) throws SpecmateException {
		SentenceUnfolderBase unfolder;
		if (lang == ELanguage.DE) {
			unfolder = new GermanSentenceUnfolder();
		} else {
			unfolder = new EnglishSentenceUnfolder();
		}
		text = generalProcessing(text);
		return unfolder.unfold(tagger, text, lang);
	}

	private String generalProcessing(String text) {
		// Add Space before punctuation.
		return text.replaceAll("[^,.!? ](?=[,.!?])", "$0 ").replaceAll("\\s+", " ");
	}
}
