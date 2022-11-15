package com.specmate.modelgeneration.internal.legacy.rulebased;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.modelgeneration.api.ICEGModelGenerator;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;

@Component(immediate = true, service = ICEGModelGenerator.class, property = { "service.ranking:Integer=50",
		"languages=en" })
public class EnglishCEGFromRequirementGenerator extends RuleBasedCEGFromRequirementGenerator {

	private INLPService tagger;

	@Override
	protected ICauseEffectPatternMatcher getPatternMatcher() {
		return new EnglishPatternMatcher();
	}

	@Override
	protected IAndOrSplitter getAndOrSplitter() {
		return new EnglishAndOrSplitter();
	}

	@Override
	protected String replaceNegation(String text) {
		if (text.contains("no ")) {
			return text.replace("no ", "a ");
		}
		if (text.contains("cannot ")) {
			return text.replace("cannot ", "can ");
		}
		if (text.contains("not ")) {
			return text.replace("not ", "");
		}
		if (text.contains("n't ")) {
			return text.replace("n't ", " ");
		}
		return null;

	}

	@Override
	protected ELanguage getLanguage() {
		return ELanguage.EN;
	}

	@Reference
	public void setTagger(INLPService tagger) {
		this.tagger = tagger;
	}

	@Override
	protected INLPService getTagger() {
		return tagger;
	}

}
