package com.specmate.modelgeneration.stages;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.specmate.cause_effect_patterns.parse.DependencyParsetree;
import com.specmate.cause_effect_patterns.parse.matcher.MatchResult;
import com.specmate.cause_effect_patterns.parse.matcher.MatchRule;
import com.specmate.cause_effect_patterns.parse.matcher.MatchUtil;
import com.specmate.cause_effect_patterns.resolve.GenerateMatcherUtil;
import com.specmate.common.exception.SpecmateException;
import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.config.api.IConfigService;
import com.specmate.model.administration.ErrorCode;
import com.specmate.modelgeneration.PatternbasedCEGGenerator;
import com.specmate.nlp.api.ELanguage;
import com.specmate.nlp.api.INLPService;
import com.specmate.xtext.XTextException;

public class RuleMatcher {
	private List<MatchRule> rules;
	private ELanguage lang;
	private INLPService tagger;
	private IConfigService configService;


	public RuleMatcher(INLPService nlpService, IConfigService configService, ELanguage language) throws SpecmateInternalException {
		tagger = nlpService;
		lang = language;
		this.configService = configService;
		loadRessources();
	}

	private void loadRessources() throws SpecmateInternalException {
		String[] paths = readURIStringsFromConfig();
		String depPath = paths[0];
		String posPath = paths[1];
		String rulePath = paths[2];
		String langCode = lang.getLanguage().toUpperCase();

		try {
			URI dep = getURI(depPath,  "resources/"+langCode+"/Dep_"+langCode+".spec");
			URI pos = getURI(posPath,  "resources/"+langCode+"/Pos_"+langCode+".spec");
			URI rule = getURI(rulePath,"resources/"+langCode+"/Rule_"+langCode+".spec");

			rules = new GenerateMatcherUtil().loadXTextResources(rule, dep, pos);
		} catch (XTextException e) {
			throw new SpecmateInternalException(ErrorCode.NLP, e);
		} catch (URISyntaxException e) {
			throw new SpecmateInternalException(ErrorCode.INTERNAL_PROBLEM, e);
		}
	}

	/**
	 * Read in the location paths from the configService
	 * @return A String Array of length 3 with the paths to {dependency, POS, rules} in that order.
	 * If there is no data given for any of those paths, the specific element is null.
	 */
	private String[] readURIStringsFromConfig() {
		Set<Entry<Object, Object>> configData = configService.getConfigurationProperties("generation.dsl");
		String depPath = null;
		String posPath = null;
		String rulePath = null;
		String langCode = lang.toString().toUpperCase();

		//generation.dsl.<LANG>.rule
		//generation.dsl.<LANG>.dependency
		//generation.dsl.<LANG>.pos
		for(Entry<Object,Object> entry: configData) {
			String key = (String) entry.getKey();

			if(key.equals("generation.dsl."+langCode+".rule")) {
				rulePath = (String) entry.getValue();
			}

			if(key.equals("generation.dsl."+langCode+".dependency")) {
				depPath = (String) entry.getValue();
			}

			if(key.equals("generation.dsl."+langCode+".pos")) {
				posPath = (String) entry.getValue();
			}
		}

		String[] result = {depPath, posPath, rulePath};
		return result;
	}

	private URI getURI(String path, String localDefault) throws URISyntaxException {
		if(path != null) {
			return URI.createURI(path);
		}
		return getLocalFile(localDefault);
	}

	private URI getLocalFile(String fileName) throws URISyntaxException {
		Bundle bundle = FrameworkUtil.getBundle(PatternbasedCEGGenerator.class);
		return URI.createURI(bundle.getResource(fileName).toURI().toString());
	}

	public List<MatchResult> matchText(String text) throws SpecmateException {
		JCas tagResult = tagger.processText(text, lang);
		DependencyParsetree data = DependencyParsetree.generateFromJCas(tagResult);
		return MatchUtil.evaluateRuleset(rules, data);
	}

}
