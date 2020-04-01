package com.specmate.nlp.internal.services;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.log.LogService;

import com.specmate.common.exception.SpecmateInternalException;
import com.specmate.model.administration.ErrorCode;
import com.specmate.rest.RestClient;
import com.specmate.rest.RestResult;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;

public class DependencyParserAnalysisComponent extends JCasAnnotator_ImplBase {

	public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true)
	protected String language;

	// Variables for REST Call (Spacy API)
	private static final String SPACY_API_BASE_URL = "spacy_docker_url";
	private static final int TIMEOUT = 5000;
	private LogService logService;
	private RestClient restClient;

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String text = jcas.getDocumentText();
		// Call Spacy API
		JSONObject result = null;
		try {
			result = this.accessSpacyAPI(text);
		} catch (SpecmateInternalException e1) {
			e1.printStackTrace();
		}

		// Iterate over all sentences
		for (Sentence curSentence : select(jcas, Sentence.class)) {

			// Generate list of tokens for current sentence (Tokens generated by DKPro)
			List<Token> tokens = selectCovered(Token.class, curSentence);

			// We first need to check whether DKPro and Spacy generate the same tokens. For
			// this purpose, we compare both sets of tokens.
			// allWords = all tokens that have been generated by Spacy.
			JSONArray allWords = result.getJSONArray("words");

			// We can only generate the dependendency tree if both methods generated the
			// same set of tokens.
			try {
				if (checkTokenization(tokens, allWords)) {
					for (int i = 0; i < tokens.size(); i++) {
						// 1. Step: Search for all dependencies in which the current token is included.
						// allArcs = all dependencies that have been generated by Spacy.
						JSONArray allArcs = result.getJSONArray("arcs");

						// Iterate over all dependencies
						for (int j = 1; j < allArcs.length(); j++) {
							Object currentArc = allArcs.get(j - 1);

							// Get description of current dependency
							Object start = ((JSONObject) currentArc).get("start");
							Object end = ((JSONObject) currentArc).get("end");
							Object label = ((JSONObject) currentArc).get("label");
							Object dir = ((JSONObject) currentArc).get("dir");

							// Check whether the token is the "start" of dependency
							// if yes, we create a corresponding Dependency and add it to the JCas
							if ((int) start == i) {
								Dependency dep = new Dependency(jcas);
								dep.setDependencyType((String) label);
								dep.setFlavor(DependencyFlavor.BASIC);

								// Is the current token a dependent or governor?
								// The token is a dependent if the arrow of the dependency points to the token
								// and vice versa.
								if (dir.equals("left")) {
									dep.setDependent(tokens.get(i));
									dep.setGovernor(tokens.get((int) end));
								} else if (dir.equals("right")) {
									dep.setGovernor(tokens.get(i));
									dep.setDependent(tokens.get((int) end));
								}
								dep.setBegin(dep.getDependent().getBegin());
								dep.setEnd(dep.getDependent().getEnd());
								dep.addToIndexes();
							}
						}
					}
				}
			} catch (SpecmateInternalException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean checkTokenization(List<Token> DKProTokens, JSONArray SpacyTokens) throws SpecmateInternalException {

		int sameTokenziationCounter = 0;

		for (int i = 0; i < DKProTokens.size(); i++) {
			// Token of DKPro
			String tokenText = DKProTokens.get(i).getText();
			for (int j = 1; j <= SpacyTokens.length(); j++) {
				Object currentWord = SpacyTokens.get(j - 1);
				// Token of Spacy
				Object token = ((JSONObject) currentWord).get("text");
				if (tokenText.equals((String) token)) {
					sameTokenziationCounter++;
					break;
				}
			}
		}

		// 1. Quantitative Comparison: Did both approaches create the same amount of
		// tokens?
		// 2. Qualitative Comparison: Compare the content of all tokens.
		if (sameTokenziationCounter == DKProTokens.size() && DKProTokens.size() == SpacyTokens.length()) {
			return true;
		} else {
			throw new SpecmateInternalException(ErrorCode.SPACY,
					"DKPro and Spacy generate different set of tokens. Dependency tree can not be created.");
		}
	}

	public JSONObject accessSpacyAPI(String requirement) throws SpecmateInternalException {
		restClient = new RestClient(SPACY_API_BASE_URL, TIMEOUT, logService);

		// Set model parameters
		JSONObject request = new JSONObject();
		request.put("text", requirement);
		request.put("model", this.language);
		request.put("collapse_punctuation", 0);
		request.put("collapse_phrases", 0);

		RestResult<JSONObject> result = this.restClient.post("/dep", request);
		if (result.getResponse().getStatus() == Status.OK.getStatusCode()) {
			result.getResponse().close();
			return result.getPayload();
		} else {
			result.getResponse().close();
			throw new SpecmateInternalException(ErrorCode.SPACY,
					"Could not access Spacy API. Dependencies could not be loaded.");
		}
	}

}
