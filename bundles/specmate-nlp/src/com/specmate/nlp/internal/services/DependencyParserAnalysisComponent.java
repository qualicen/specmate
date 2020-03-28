package com.specmate.nlp.internal.services;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.DependencyFlavor;

public class DependencyParserAnalysisComponent extends JCasAnnotator_ImplBase {

	public static final java.lang.String PARAM_LANGUAGE = "language";

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		String text = jcas.getDocumentText();
		JSONObject result = new JSONObject();

		// "Fake Data" --> Rest Call muss noch hinzugefügt werden.
		JSONArray arcs = new JSONArray(
				"[{ dir: left, start: 0, end: 1, label: nsubj },{ dir: right, start: 1, end: 2, label: dobj },{ dir: right, start: 1, end: 3, label: prep },{ dir: right, start: 3, end: 4, label: pobj },{ dir: left, start: 2, end: 3, label: prep }]");
		JSONArray words = new JSONArray(
				"[{ tag: PRP, text: They },{ tag: VBD, text: ate },{ tag: NN, text: the},{ tag: IN, text: pizza },{ tag: NNS, text: fastly }]");

		result.put("arcs", arcs);
		result.put("words", words);

		// Iterate over all sentences
		for (Sentence curSentence : select(jcas, Sentence.class)) {

			// Generate list of tokens for current sentence
			List<Token> tokens = selectCovered(Token.class, curSentence);

			for (int i = 0; i < tokens.size(); i++) {

				// 1. Schritt: Suche alle Dependencies in denen der Token eine Rolle spielt.
				// Dazu holen wir uns erst alle Dependencies bzw. Arcs
				JSONArray allArcs = result.getJSONArray("arcs");

				// Wir iterieren über alle Dependencies
				for (int j = 1; j < allArcs.length(); j++) {
					Object currentArc = allArcs.get(j - 1);

					// Get description of current dependency
					Object start = ((JSONObject) currentArc).get("start");
					Object end = ((JSONObject) currentArc).get("end");
					Object label = ((JSONObject) currentArc).get("label");
					Object dir = ((JSONObject) currentArc).get("dir");

					// 1. Fall: Token ist der start einer dependency
					if ((int) start == i) {
						Dependency dep = new Dependency(jcas);
						dep.setDependencyType((String) label);
						dep.setFlavor(DependencyFlavor.BASIC);

						// Ist der Token Governor oder Dependent?
						// Dependent wenn der Pfeil auf den Token zeigt und vice versa
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
	}

}
