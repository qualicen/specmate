package com.specmate.modelgeneration.internal.legacy.rulebased;

import java.util.Collection;
import java.util.StringJoiner;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.specmate.common.exception.SpecmateException;
import com.specmate.modelgeneration.api.ICEGModelGenerator;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.nlp.util.GermanSentenceUnfolder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

@Component(immediate = true, service = ICEGModelGenerator.class, property = { "service.ranking:Integer=50",
		"languages=de" })
public class GermanCEGFromRequirementGenerator extends RuleBasedCEGFromRequirementGenerator {

	private INLPService tagger;

	@Override
	protected ICauseEffectPatternMatcher getPatternMatcher() {
		return new GermanPatternMatcher();
	}

	@Override
	protected IAndOrSplitter getAndOrSplitter() {
		return new GermanAndOrSplitter();
	}

	@Override
	protected String replaceNegation(String text) {
		if (text.contains("kein ")) {
			return text.replace("kein ", "ein ");
		}
		if (text.contains("keine ")) {
			return text.replace("keine ", "eine ");
		}
		if (text.contains("keinen ")) {
			return text.replace("keinen ", "einen ");
		}
		if (text.contains("nicht ")) {
			return text.replace("nicht ", "");
		}
		return null;
	}

	@Override
	protected String preprocess(String text) throws SpecmateException {
		JCas result = tagger.processText(text, ELanguage.DE);
		Collection<Sentence> sentences = JCasUtil.select(result, Sentence.class);
		StringJoiner joiner = new StringJoiner(" ");
		for (Sentence sen : sentences) {
			joiner.add(new GermanSentenceUnfolder(tagger).unfold(sen.getCoveredText()).get(0));
		}
		return joiner.toString();

	}

	@Override
	protected ELanguage getLanguage() {
		return ELanguage.DE;
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
